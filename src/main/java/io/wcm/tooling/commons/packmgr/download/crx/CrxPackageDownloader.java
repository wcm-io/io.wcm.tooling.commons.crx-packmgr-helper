/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2025 wcm.io
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
package io.wcm.tooling.commons.packmgr.download.crx;

import static io.wcm.tooling.commons.packmgr.PackageManagerHelper.CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;

import io.wcm.tooling.commons.packmgr.PackageManagerException;
import io.wcm.tooling.commons.packmgr.PackageManagerHelper;
import io.wcm.tooling.commons.packmgr.download.VendorPackageDownloader;
import io.wcm.tooling.commons.packmgr.install.VendorInstallerFactory;

/**
 * Vendor Downloader for AEM's CRX Package Manager
 */
public class CrxPackageDownloader implements VendorPackageDownloader {

  @Override
  public String uploadPackageDefinition(String packageManagerUrl, File file, PackageManagerHelper pkgmgr) {
    HttpPost post = new HttpPost(packageManagerUrl + "/.json?cmd=upload");
    MultipartEntityBuilder entity = MultipartEntityBuilder.create()
        .addBinaryBody("package", file)
        .addTextBody("force", "true");
    post.setEntity(entity.build());
    JSONObject jsonResponse = pkgmgr.executePackageManagerMethodJson(pkgmgr.getHttpClient(), pkgmgr.getPackageManagerHttpClientContext(), post);
    boolean success = jsonResponse.optBoolean("success", false);
    String msg = jsonResponse.optString("msg", null);
    String packagePath = jsonResponse.optString("path", null);
    // package already exists - get path from error message and continue
    if (!success && StringUtils.startsWith(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX) && StringUtils.isEmpty(packagePath)) {
      packagePath = StringUtils.substringAfter(msg, CRX_PACKAGE_EXISTS_ERROR_MESSAGE_PREFIX);
      success = true;
    }
    if (!success) {
      throw new PackageManagerException("Package path detection failed: " + msg);
    }

    return packagePath;
  }

  @Override
  public HttpPost createRebuildMethod(String packagePath, String packageManagerUrl) {
    return new HttpPost(packageManagerUrl + "/console.html" + packagePath + "?cmd=build");
  }

  @Override
  public String createDownloadZipBaseUrl(String packageManagerUrl) {
    return VendorInstallerFactory.getBaseUrl(packageManagerUrl);
  }

}
