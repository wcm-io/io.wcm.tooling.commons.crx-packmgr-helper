/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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
package io.wcm.tooling.commons.packmgr.httpaction;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.tooling.commons.packmgr.PackageManagerHttpActionException;

/**
 * Get install status from package manager.
 */
public final class PackageManagerInstallStatusCall implements HttpCall<PackageManagerInstallStatus> {

  private final CloseableHttpClient httpClient;
  private final HttpClientContext context;
  private final String packageManagerInstallStatusURL;

  private static final Logger log = LoggerFactory.getLogger(PackageManagerInstallStatusCall.class);

  /**
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param packageManagerInstallStatusURL Bundle status URL
   */
  public PackageManagerInstallStatusCall(CloseableHttpClient httpClient, HttpClientContext context,
      String packageManagerInstallStatusURL) {
    this.httpClient = httpClient;
    this.context = context;
    this.packageManagerInstallStatusURL = packageManagerInstallStatusURL;
  }

  @Override
  public PackageManagerInstallStatus execute() {
    log.debug("Call URL: {}", packageManagerInstallStatusURL);

    HttpGet method = new HttpGet(packageManagerInstallStatusURL);
    try (CloseableHttpResponse response = httpClient.execute(method, context)) {

      String responseString = EntityUtils.toString(response.getEntity());
      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw PackageManagerHttpActionException.forHttpError(packageManagerInstallStatusURL, response.getStatusLine(), responseString);
      }

      return toPackageManagerInstallStatus(responseString);
    }
    catch (IOException ex) {
      throw PackageManagerHttpActionException.forIOException(packageManagerInstallStatusURL, ex);
    }
  }

  private PackageManagerInstallStatus toPackageManagerInstallStatus(String jsonString) {
    boolean finished = false;
    int itemCount = 0;

    try {
      JSONObject json = new JSONObject(jsonString);
      JSONObject status = json.optJSONObject("status");
      if (status != null) {
        finished = status.optBoolean("finished");
        itemCount = status.optInt("itemCount");
      }
    }
    catch (JSONException ex) {
      throw PackageManagerHttpActionException.forJSONException(packageManagerInstallStatusURL, jsonString, ex);
    }

    return new PackageManagerInstallStatus(finished, itemCount);
  }

}
