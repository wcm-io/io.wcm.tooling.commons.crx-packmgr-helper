/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.tooling.commons.packmgr.download;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.PackageManagerProperties;
import io.wcm.tooling.commons.packmgr.install.VendorInstallerFactory;

/**
 * Downloads a single AEM content package.
 */
public final class PackageDownloader implements Closeable {

  private final PackageManagerProperties props;
  private final PackageManagerHelper pkgmgr;
  private final CloseableHttpClient httpClient;

  private static final Logger log = LoggerFactory.getLogger(PackageDownloader.class);

  /**
   * @param props Package manager configuration properties.
   */
  public PackageDownloader(PackageManagerProperties props) {
    this.props = props;
    this.pkgmgr = new PackageManagerHelper(props);
    this.httpClient = pkgmgr.getHttpClient();
  }

  /**
   * Upload the given local package definition (without actually installing it).
   * @param file Package definition file
   * @return Package path
   */
  public String uploadPackageDefinition(File file) {

    if (!file.exists()) {
      throw new PackageManagerException("File not found: " + file.getAbsolutePath());
    }
    String packageManagerUrl = props.getPackageManagerUrl();

    // try upload to get path of package - or otherwise make sure package def exists (no install!)
    log.info("Upload package definition for {} to {} ...", file.getName(), packageManagerUrl);
    VendorPackageDownloader downloader = VendorInstallerFactory.getPackageDownloader(packageManagerUrl);
    return downloader.uploadPackageDefinition(packageManagerUrl, file, pkgmgr);
  }

  /**
   * Download content package from CRX package manager.
   * @param packagePath Content Package path in AEM instance.
   * @param ouputFilePath Path to download package from AEM instance to.
   * @param rebuildPackage Whether to rebuild the package within the CRX package manager before downloading it to
   *          include the latest content from repository.
   * @return Downloaded content package file
   */
  @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
  public File downloadContentPackage(String packagePath, String ouputFilePath, boolean rebuildPackage) {
    try {
      HttpClientContext httpClientContext = pkgmgr.getPackageManagerHttpClientContext();
      String packageManagerUrl = props.getPackageManagerUrl();
      VendorPackageDownloader downloader = VendorInstallerFactory.getPackageDownloader(packageManagerUrl);

      // (Re-)build package
      if (rebuildPackage) {
        log.info("Rebuilding package {} ...", packagePath);
        HttpPost buildMethod = downloader.createRebuildMethod(packagePath, packageManagerUrl);
        pkgmgr.executePackageManagerMethodHtmlOutputResponse(httpClient, httpClientContext, buildMethod);
      }

      // Download package
      String baseUrl = downloader.createDownloadZipBaseUrl(packageManagerUrl);
      log.info("Downloading package {} from {} ...", packagePath, baseUrl);

      HttpGet downloadMethod = new HttpGet(baseUrl + packagePath);

      // execute download
      CloseableHttpResponse response = httpClient.execute(downloadMethod, httpClientContext);
      try (response) {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

          // get response stream
          InputStream responseStream = response.getEntity().getContent();

          // delete existing file
          File outputFileObject = new File(ouputFilePath);
          if (outputFileObject.exists()) {
            outputFileObject.delete();
          }

          // write response file
          try (FileOutputStream fos = new FileOutputStream(outputFileObject)) {
            IOUtils.copy(responseStream, fos);
            fos.flush();
            responseStream.close();
          }

          log.info("Package downloaded to {}", outputFileObject.getAbsolutePath());

          return outputFileObject;
        }
        else {
          throw new PackageManagerException("Package download failed:\n"
              + EntityUtils.toString(response.getEntity()));
        }
      }
      finally {
        EntityUtils.consumeQuietly(response.getEntity());
      }
    }
    catch (IOException ex) {
      throw new PackageManagerException("Download operation failed.", ex);
    }
  }

  @Override
  public void close() throws IOException {
    httpClient.close();
  }

}
