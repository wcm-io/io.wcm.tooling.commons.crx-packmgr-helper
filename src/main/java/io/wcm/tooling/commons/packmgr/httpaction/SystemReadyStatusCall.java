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
package io.wcm.tooling.commons.packmgr.httpaction;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.wcm.tooling.commons.packmgr.PackageManagerHttpActionException;

/**
 * Get bundle status from web console.
 */
public final class SystemReadyStatusCall implements HttpCall<SystemReadyStatus> {

  private final CloseableHttpClient httpClient;
  private final HttpClientContext context;
  private final String systemReadyURL;

  private static final Logger log = LoggerFactory.getLogger(SystemReadyStatusCall.class);
  private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

  /**
   * @param httpClient HTTP client
   * @param context HTTP client context
   * @param systemReadyURL System ready URL
   */
  public SystemReadyStatusCall(CloseableHttpClient httpClient, HttpClientContext context, String systemReadyURL) {
    this.httpClient = httpClient;
    this.context = context;
    this.systemReadyURL = systemReadyURL;
  }

  @Override
  public SystemReadyStatus execute() {
    log.debug("Call URL: {}", systemReadyURL);

    HttpGet method = new HttpGet(systemReadyURL);
    try (CloseableHttpResponse response = httpClient.execute(method, context)) {

      String responseString = EntityUtils.toString(response.getEntity());
      switch (response.getStatusLine().getStatusCode()) {
        case HttpStatus.SC_OK:
        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
        case HttpStatus.SC_SERVICE_UNAVAILABLE:
          return toSystemReadyStatus(responseString, systemReadyURL);
        case HttpStatus.SC_NOT_FOUND:
          // AEM version that does not support system ready endpoint - accept as valid response
          return new SystemReadyStatus(null, null);
        default:
          throw PackageManagerHttpActionException.forHttpError(systemReadyURL, response.getStatusLine(), responseString);
      }
    }
    catch (IOException ex) {
      throw PackageManagerHttpActionException.forIOException(systemReadyURL, ex);
    }
  }

  static SystemReadyStatus toSystemReadyStatus(String jsonString, String systemReadyURL) {
    try {
      return JSON_MAPPER.readValue(jsonString, SystemReadyStatus.class);
    }
    catch (JsonProcessingException ex) {
      throw PackageManagerHttpActionException.forJSONException(systemReadyURL, jsonString, ex);
    }
  }

}