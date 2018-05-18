/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
package io.wcm.tooling.commons.packmgr.util;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;

import io.wcm.tooling.commons.packmgr.install.PackageFile;

/**
 * Utility methods for HTTP client.
 */
public final class HttpClientUtil {

  private HttpClientUtil() {
    // static methods only
  }

  /**
   * Apply timeout configurations that are defined specific for this package file.
   * @param httpRequest Http request
   * @param packageFile Package file
   */
  public static void applyRequestConfig(HttpRequestBase httpRequest, PackageFile packageFile) {
    Integer httpSocketTimeoutSec = packageFile.getHttpSocketTimeoutSec();
    if (httpSocketTimeoutSec == null) {
      return;
    }

    // apply specific timeout settings configured for this package file
    httpRequest.setConfig(RequestConfig.copy(httpRequest.getConfig())
        .setSocketTimeout(httpSocketTimeoutSec * (int)DateUtils.MILLIS_PER_SECOND)
        .build());
  }

}
