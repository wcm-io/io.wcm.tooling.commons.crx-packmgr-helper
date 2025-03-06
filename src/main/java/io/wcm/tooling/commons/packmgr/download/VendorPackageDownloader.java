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
package io.wcm.tooling.commons.packmgr.download;

import org.apache.http.client.methods.HttpPost;

/**
 * Interface any Vendor Package Downloader must provide
 */
public interface VendorPackageDownloader {

  /**
   * Http-POST to rebuild a Package
   * @param packagePath Path in jcr
   * @param packageManagerUrl URL of he manager service
   * @return Http-Post to call
   */
  HttpPost createRebuildMethod(String packagePath, String packageManagerUrl);
  
  /**
   * Base URL of the zip-downloads, without the actual contentPackagePath
   * @param packageManagerUrl URL of he manager service
   * @return Http-Post to call
   */
  String createDownloadZipBaseUrl(String packageManagerUrl);

}
