/*
 * Copyright 2025 wcm.io.
 *
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
 */
package io.wcm.tooling.commons.packmgr.download.composum;

import java.io.File;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.download.VendorPackageDownloader;

/**
 * Vendor Downloader for AEM's CRX Package Manager
 */
public class ComposumPackageDownloader implements VendorPackageDownloader {
  
  private static final Logger log = LoggerFactory.getLogger(ComposumPackageDownloader.class);

  @Override
  public String uploadPackageDefinition(String packageManagerUrl, File file, PackageManagerHelper pkgmgr) {
    // prepare post method
    String url = packageManagerUrl;
    int index = url.indexOf("/bin/cpm/");
    String baseUrl = url.substring(0, index) + "/bin/cpm/package.";
    String uploadUrl = baseUrl + "upload.json";
    HttpPost post = new HttpPost(uploadUrl);
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
        .addBinaryBody("file", file)
        .addTextBody("force", "true");
    post.setEntity(entityBuilder.build());
    // execute post
    JSONObject jsonResponse = pkgmgr.executePackageManagerMethodJson(pkgmgr.getHttpClient(), pkgmgr.getPackageManagerHttpClientContext(), post);

    String status = jsonResponse.optString("status", "not-found");
    boolean success = "successful".equals(status);
    String packagePath = jsonResponse.optString("path", null);
    if (!success) {
      throw new PackageManagerException("Package path detection failed: " + success);
    }
    return packagePath;
  }
  

  @Override
  public HttpPost createRebuildMethod(String packagePath, String packageManagerUrl) {
    HttpPost buildMethod = new HttpPost(packageManagerUrl + "core/jobcontrol.job.json");
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
    entityBuilder.addTextBody("event.job.topic", "com/composum/sling/core/pckgmgr/PackageJobExecutor");
    entityBuilder.addTextBody("reference", packagePath);
    entityBuilder.addTextBody("_charset_", "UTF-8");
    entityBuilder.addTextBody("operation", "assemble");
    entityBuilder.addTextBody("event.job.topic", packagePath);
    buildMethod.setEntity(entityBuilder.build());
    return buildMethod;
  }

  @Override
  public String createDownloadZipBaseUrl(String packageManagerUrl) {
    delay(3); // needed when PackageDefinition was upload
    return packageManagerUrl + "package.download.zip";
  }
  
  @SuppressWarnings("PMD.GuardLogStatement")
  private void delay(int seconds) {
    if (seconds > 0) {
      log.info("Wait {} seconds after rebuilding package...", seconds);
      try {
        Thread.sleep(seconds * 1000L);
      }
      catch (InterruptedException ex) {
        // ignore
      }
    }
  }

}
