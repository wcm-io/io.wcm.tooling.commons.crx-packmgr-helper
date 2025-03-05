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

import io.wcm.tooling.commons.packmgr.download.VendorPackageDownloader;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 * Vendor Downloader  for AEM's CRX Package Manager
 */
public class ComposumPackageDownloader implements VendorPackageDownloader {

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
  public String downloadBaseUrl(String packageManagerUrl) {
    return packageManagerUrl + "package.download.zip";
  }
  
}
