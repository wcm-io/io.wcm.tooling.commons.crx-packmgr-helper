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

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.output.support.AbstractXMLOutputProcessor;
import org.jdom2.output.support.FormatStack;

/**
 * XML output processor that renders one attribute per line for easier diff-ing on content changes.
 */
class OneAttributePerLineXmlProcessor extends AbstractXMLOutputProcessor {

  @Override
  protected void printAttribute(Writer out, FormatStack fstack, Attribute attribute) throws IOException {
    if (!attribute.isSpecified() && fstack.isSpecifiedAttributesOnly()) {
      return;
    }

    write(out, StringUtils.defaultString(fstack.getLineSeparator()));
    write(out, StringUtils.defaultString(fstack.getLevelIndent()));
    write(out, StringUtils.defaultString(fstack.getIndent()));
    write(out, StringUtils.defaultString(fstack.getIndent()));

    write(out, attribute.getQualifiedName());
    write(out, "=");

    write(out, "\"");
    attributeEscapedEntitiesFilter(out, fstack, attribute.getValue());
    write(out, "\"");
  }

}
