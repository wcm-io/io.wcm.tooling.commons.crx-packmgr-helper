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
package io.wcm.tooling.commons.packmgr;

import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Proxy definition - e.g. for maven proxy settings.
 * @param id Proxy identifier
 * @param protocol Protocol
 * @param host Host
 * @param port Port
 * @param username User name
 * @param password Password
 * @param nonProxyHosts List of non-proxy hosts
 */
public record Proxy(String id, String protocol, String host, int port, String username, String password, String nonProxyHosts) {

  boolean useAuthentication() {
    return username != null && !username.isEmpty();
  }

  boolean isNonProxyHost(String givenHost) {
    if (givenHost != null && !StringUtils.isEmpty(nonProxyHosts)) {
      for (StringTokenizer tokenizer = new StringTokenizer(nonProxyHosts, "|"); tokenizer.hasMoreTokens();) {
        String pattern = tokenizer.nextToken();
        pattern = pattern.replace(".", "\\.").replace("*", ".*");
        if (givenHost.matches(pattern)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return id + "{" +
        "protocol='" + protocol + '\'' +
        ", host='" + host + '\'' +
        ", port=" + port +
        (useAuthentication() ? ", with username/passport authentication" : "") +
        '}';
  }

}
