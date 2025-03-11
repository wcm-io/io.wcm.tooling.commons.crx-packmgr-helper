/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.tooling.commons.packmgr.unpack;

import static org.apache.jackrabbit.vault.util.Constants.DOT_CONTENT_XML;
import static org.apache.jackrabbit.vault.util.Constants.ROOT_DIR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.jcr.PropertyType;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.vault.util.PlatformNameFormat;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.tooling.commons.packmgr.PackageManagerException;

/**
 * Manages unpacking ZIP file content applying exclude patterns.
 */
public final class ContentUnpacker {

  private static final String MIXINS_PROPERTY = "jcr:mixinTypes";
  private static final String PRIMARYTYPE_PROPERTY = "jcr:primaryType";
  private static final Namespace JCR_NAMESPACE = Namespace.getNamespace("jcr", "http://www.jcp.org/jcr/1.0");
  private static final Namespace CQ_NAMESPACE = Namespace.getNamespace("cq", "http://www.day.com/jcr/cq/1.0");
  private static final Pattern FILENAME_NAMESPACE_PATTERN = Pattern.compile("^([^:]+):(.+)$");

  private static final SAXParserFactory SAX_PARSER_FACTORY;
  static {
    SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
    SAX_PARSER_FACTORY.setNamespaceAware(true);
  }

  private final Pattern[] excludeFiles;
  private final Pattern[] excludeNodes;
  private final Pattern[] excludeProperties;
  private final Pattern[] excludeMixins;
  private final boolean markReplicationActivated;
  private final Pattern[] markReplicationActivatedIncludeNodes;
  private final String dateLastReplicated;

  /**
   * @param properties Configuration properties
   */
  public ContentUnpacker(ContentUnpackerProperties properties) {
    this.excludeFiles = toPatternArray(properties.getExcludeFiles());
    this.excludeNodes = toPatternArray(properties.getExcludeNodes());
    this.excludeProperties = toPatternArray(properties.getExcludeProperties());
    this.excludeMixins = toPatternArray(properties.getExcludeMixins());
    this.markReplicationActivated = properties.isMarkReplicationActivated();
    this.markReplicationActivatedIncludeNodes = toPatternArray(properties.getMarkReplicationActivatedIncludeNodes());

    if (StringUtils.isNotBlank(properties.getDateLastReplicated())) {
      this.dateLastReplicated = properties.getDateLastReplicated();
    }
    else {
      // set to current date
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      this.dateLastReplicated = ISO8601.format(cal);
    }
  }

  private static Pattern[] toPatternArray(String[] patternStrings) {
    if (patternStrings == null) {
      return new Pattern[0];
    }
    Pattern[] patterns = new Pattern[patternStrings.length];
    for (int i = 0; i < patternStrings.length; i++) {
      try {
        patterns[i] = Pattern.compile(patternStrings[i]);
      }
      catch (PatternSyntaxException ex) {
        throw new PackageManagerException("Invalid regexp pattern: " + patternStrings[i], ex);
      }
    }
    return patterns;
  }

  private static boolean matches(String name, Pattern[] patterns, boolean defaultIfNotPatternsDefined) {
    if (patterns.length == 0) {
      return defaultIfNotPatternsDefined;
    }
    for (Pattern pattern : patterns) {
      if (pattern.matcher(name).matches()) {
        return true;
      }
    }
    return false;
  }

  private boolean applyXmlExcludes(String name) {
    if (this.excludeNodes.length == 0 && this.excludeProperties.length == 0) {
      return false;
    }
    return StringUtils.endsWith(name, ".xml");
  }

  /**
   * Unpacks file
   * @param file File
   * @param outputDirectory Output directory
   */
  public void unpack(File file, File outputDirectory) {
    try (ZipFile zipFile = new ZipFile.Builder().setFile(file).get()) {
      Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
      while (entries.hasMoreElements()) {
        ZipArchiveEntry entry = entries.nextElement();
        if (!matches(entry.getName(), excludeFiles, false)) {
          unpackEntry(zipFile, entry, outputDirectory);
        }
      }
    }
    catch (IOException ex) {
      throw new PackageManagerException("Error reading content package " + file.getAbsolutePath(), ex);
    }
  }

  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  private void unpackEntry(ZipFile zipFile, ZipArchiveEntry entry, File outputDirectory) throws IOException {
    if (entry.isDirectory()) {
      File directory = FileUtils.getFile(outputDirectory, entry.getName());
      directory.mkdirs();
    }
    else {
      Set<String> namespacePrefixes = null;
      if (applyXmlExcludes(entry.getName())) {
        namespacePrefixes = getNamespacePrefixes(zipFile, entry);
      }

      try (InputStream entryStream = zipFile.getInputStream(entry)) {
        File outputFile = FileUtils.getFile(outputDirectory, entry.getName());
        if (outputFile.exists()) {
          outputFile.delete();
        }
        File directory = outputFile.getParentFile();
        directory.mkdirs();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
          if (applyXmlExcludes(entry.getName()) && namespacePrefixes != null) {
            // write file with XML filtering
            try {
              writeXmlWithExcludes(entry, entryStream, fos, namespacePrefixes);
            }
            catch (JDOMException ex) {
              throw new PackageManagerException("Unable to parse XML file: " + entry.getName(), ex);
            }
          }
          else {
            // write file directly without XML filtering
            IOUtils.copy(entryStream, fos);
          }
        }
      }
    }
  }

  /**
   * Parses XML file with namespace-aware SAX parser to get defined namespaces prefixes in order of appearance
   * (to keep the same order when outputting the XML file again).
   * @param zipFile ZIP file
   * @param entry ZIP entry
   * @return Ordered set with namespace prefixes in correct order.
   *         Returns null if given XML file does not contain FileVault XML content.
   */
  private Set<String> getNamespacePrefixes(ZipFile zipFile, ZipArchiveEntry entry) throws IOException {
    try (InputStream entryStream = zipFile.getInputStream(entry)) {
      SAXParser parser = SAX_PARSER_FACTORY.newSAXParser();
      final Set<String> prefixes = new LinkedHashSet<>();

      final AtomicBoolean foundRootElement = new AtomicBoolean(false);
      DefaultHandler handler = new DefaultHandler() {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
          // validate that XML file contains FileVault XML content
          if (StringUtils.equals(uri, JCR_NAMESPACE.getURI()) && StringUtils.equals(localName, "root")) {
            foundRootElement.set(true);
          }
        }
        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
          if (StringUtils.isNotBlank(prefix)) {
            prefixes.add(prefix);
          }
        }
      };
      parser.parse(entryStream, handler);

      if (!foundRootElement.get()) {
        return null;
      }
      else {
        return prefixes;
      }
    }
    catch (IOException | SAXException | ParserConfigurationException ex) {
      throw new IOException("Error parsing " + entry.getName(), ex);
    }
  }

  private void writeXmlWithExcludes(ZipArchiveEntry entry, InputStream inputStream, OutputStream outputStream, Set<String> namespacePrefixes)
      throws IOException, JDOMException {
    SAXBuilder saxBuilder = new SAXBuilder();
    saxBuilder.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    saxBuilder.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    Document doc = saxBuilder.build(inputStream);

    Set<String> namespacePrefixesActuallyUsed = new HashSet<>();

    // check for namespace prefix in file name
    String namespacePrefix = getNamespacePrefix(entry.getName());
    if (namespacePrefix != null) {
      namespacePrefixesActuallyUsed.add(namespacePrefix);
    }

    applyXmlExcludes(doc.getRootElement(), getParentPath(entry), namespacePrefixesActuallyUsed, false);

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat()
        .setIndent("    ")
        .setLineSeparator(LineSeparator.UNIX));
    outputter.setXMLOutputProcessor(new OneAttributePerLineXmlProcessor(namespacePrefixes, namespacePrefixesActuallyUsed));
    outputter.output(doc, outputStream);
    outputStream.flush();
  }

  static String getNamespacePrefix(String path) {
    String fileName = FilenameUtils.getName(path);
    if (StringUtils.equals(DOT_CONTENT_XML, fileName)) {
      String parentFolderName = FilenameUtils.getName(FilenameUtils.getPathNoEndSeparator(path));
      if (parentFolderName != null) {
        String nodeName = PlatformNameFormat.getRepositoryName(parentFolderName);
        Matcher matcher = FILENAME_NAMESPACE_PATTERN.matcher(nodeName);
        if (matcher.matches()) {
          return matcher.group(1);
        }
      }
    }
    return null;
  }

  private String getParentPath(ZipArchiveEntry entry) {
    return StringUtils.removeEnd(StringUtils.removeStart(entry.getName(), ROOT_DIR), "/" + DOT_CONTENT_XML);
  }

  private String buildElementPath(Element element, String parentPath) {
    StringBuilder path = new StringBuilder(parentPath);
    if (!StringUtils.equals(element.getQualifiedName(), "jcr:root")) {
      path.append("/").append(element.getQualifiedName());
    }
    return path.toString();
  }

  @SuppressWarnings("PMD.EmptyControlStatement")
  private void applyXmlExcludes(Element element, String parentPath, Set<String> namespacePrefixesActuallyUsed,
      boolean insideReplicationElement) {
    String path = buildElementPath(element, parentPath);
    if (matches(path, this.excludeNodes, false)) {
      element.detach();
      return;
    }
    collectNamespacePrefix(namespacePrefixesActuallyUsed, element.getNamespacePrefix());

    String jcrPrimaryType = element.getAttributeValue("primaryType", JCR_NAMESPACE);
    boolean isRepositoryUserGroup = StringUtils.equals(jcrPrimaryType, "rep:User") || StringUtils.equals(jcrPrimaryType, "rep:Group");
    boolean isReplicationElement = StringUtils.equals(jcrPrimaryType, "cq:Page")
        || StringUtils.equals(jcrPrimaryType, "dam:Asset")
        || StringUtils.equals(jcrPrimaryType, "cq:Template");
    boolean isContent = insideReplicationElement && StringUtils.equals(element.getQualifiedName(), "jcr:content");
    boolean setReplicationAttributes = isContent && markReplicationActivated;

    List<Attribute> attributes = new ArrayList<>(element.getAttributes());
    for (Attribute attribute : attributes) {
      boolean excluded = false;
      if (matches(attribute.getQualifiedName(), this.excludeProperties, false)) {
        if (isRepositoryUserGroup && StringUtils.equals(attribute.getQualifiedName(), JcrConstants.JCR_UUID)) {
          // keep jcr:uuid property for groups and users, otherwise they cannot be imported again
        }
        else {
          attribute.detach();
          excluded = true;
        }
      }
      else if (StringUtils.equals(attribute.getQualifiedName(), PRIMARYTYPE_PROPERTY)) {
        String namespacePrefix = StringUtils.substringBefore(attribute.getValue(), ":");
        collectNamespacePrefix(namespacePrefixesActuallyUsed, namespacePrefix);
      }
      else if (StringUtils.equals(attribute.getQualifiedName(), MIXINS_PROPERTY)) {
        String filteredValue = filterMixinsPropertyValue(attribute.getValue(), namespacePrefixesActuallyUsed);
        if (StringUtils.isBlank(filteredValue)) {
          attribute.detach();
        }
        else {
          attribute.setValue(filteredValue);
        }
      }
      else if (StringUtils.startsWith(attribute.getValue(), "{Name}")) {
        collectNamespacePrefixNameArray(namespacePrefixesActuallyUsed, attribute.getValue());
        // alphabetically sort name values
        attribute.setValue(sortReferenceValues(attribute.getValue(), PropertyType.NAME));
      }
      else if (StringUtils.startsWith(attribute.getValue(), "{WeakReference}")) {
        // alphabetically sort weak reference values
        attribute.setValue(sortReferenceValues(attribute.getValue(), PropertyType.WEAKREFERENCE));
      }
      if (!excluded) {
        collectNamespacePrefix(namespacePrefixesActuallyUsed, attribute.getNamespacePrefix());
      }
    }

    // set replication status for jcr:content nodes inside cq:Page nodes
    if (setReplicationAttributes && matches(path, markReplicationActivatedIncludeNodes, true)) {
      addMixin(element, "cq:ReplicationStatus");
      element.setAttribute("lastReplicated", "{Date}" + dateLastReplicated, CQ_NAMESPACE);
      element.setAttribute("lastReplicationAction", "Activate", CQ_NAMESPACE);
      collectNamespacePrefix(namespacePrefixesActuallyUsed, CQ_NAMESPACE.getPrefix());
    }

    // if current element is a replication element, but the jcr:content node to set the replication attributes to is missing, add it
    if (isReplicationElement && element.getChild("content", JCR_NAMESPACE) == null
        && matches(path + "/jcr:content", markReplicationActivatedIncludeNodes, true)) {
      Element contentNode = new Element("content", JCR_NAMESPACE);
      String jcrContentPrimaryType = StringUtils.equals(jcrPrimaryType, "cq:Template") ? "cq:PageContent" : jcrPrimaryType + "Content";
      contentNode.setAttribute("primaryType", jcrContentPrimaryType, JCR_NAMESPACE);
      element.addContent(contentNode);
    }

    List<Element> children = new ArrayList<>(element.getChildren());
    for (Element child : children) {
      applyXmlExcludes(child, path, namespacePrefixesActuallyUsed, (insideReplicationElement || isReplicationElement) && !isContent);
    }
  }

  private String filterMixinsPropertyValue(String value, Set<String> namespacePrefixesActuallyUsed) {
    if (this.excludeMixins.length == 0 || StringUtils.isBlank(value)) {
      return value;
    }

    List<String> mixins = new ArrayList<>();
    for (String mixin : DocViewUtil.parseValues(value)) {
      if (!matches(mixin, this.excludeMixins, false)) {
        String namespacePrefix = StringUtils.substringBefore(mixin, ":");
        collectNamespacePrefix(namespacePrefixesActuallyUsed, namespacePrefix);
        mixins.add(mixin);
      }
    }

    if (mixins.isEmpty()) {
      return null;
    }

    return DocViewUtil.formatValues(mixins);
  }

  private void addMixin(Element element, String mixin) {
    String mixinsString = element.getAttributeValue("mixinTypes", JCR_NAMESPACE);

    List<String> mixins = new ArrayList<>();
    if (!StringUtils.isBlank(mixinsString)) {
      for (String item : DocViewUtil.parseValues(mixinsString)) {
        mixins.add(item);
      }
    }
    if (!mixins.contains(mixin)) {
      mixins.add(mixin);
    }

    element.setAttribute("mixinTypes", DocViewUtil.formatValues(mixins), JCR_NAMESPACE);
  }

  private void collectNamespacePrefix(Set<String> prefixes, String prefix) {
    if (StringUtils.isNotBlank(prefix)) {
      prefixes.add(prefix);
    }
  }

  private void collectNamespacePrefixNameArray(Set<String> prefixes, String value) {
    for (String item : DocViewUtil.parseValues(value)) {
      String namespacePrefix = StringUtils.substringBefore(item, ":");
      collectNamespacePrefix(prefixes, namespacePrefix);
    }
  }

  /**
   * Sort weak reference values alphabetically to ensure consistent ordering.
   * @param value Property value
   * @param propertyType Property type from {@link PropertyType}
   * @return Property value with sorted references
   */
  private String sortReferenceValues(String value, int propertyType) {
    Set<String> refs = new TreeSet<>();
    for (String item : DocViewUtil.parseValues(value)) {
      refs.add(item);
    }
    return DocViewUtil.formatValues(new ArrayList<>(refs), propertyType);
  }

}
