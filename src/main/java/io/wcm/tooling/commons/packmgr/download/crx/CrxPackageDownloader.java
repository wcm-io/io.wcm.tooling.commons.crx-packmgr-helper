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
package io.wcm.tooling.commons.packmgr.download.crx;

import io.wcm.tooling.commons.packmgr.download.VendorPackageDownloader;
import io.wcm.tooling.commons.packmgr.install.VendorInstallerFactory;
import org.apache.http.client.methods.HttpPost;

/**
 * Vendor Downloader for AEM's CRX Package Manager
 */
public class CrxPackageDownloader implements VendorPackageDownloader {

  @Override
  public HttpPost createRebuildMethod(String packagePath, String packageManagerUrl) {
    return new HttpPost(packageManagerUrl + "/console.html" + packagePath + "?cmd=build");
  }

  @Override
  public String createDownloadZipBaseUrl(String packageManagerUrl) {
    return VendorInstallerFactory.getBaseUrl(packageManagerUrl);
  }
  
}
