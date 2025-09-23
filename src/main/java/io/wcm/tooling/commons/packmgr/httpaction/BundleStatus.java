/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Wrapper for Status summary from Web Console Bundles Status info JSON.
 * @param statusLine Status Line from JSON string
 * @param total Total bundles
 * @param active Active bundles
 * @param activeFragment Active fragment bundles
 * @param resolved Resolved bundles
 * @param installed Installed bundles
 * @param ignored Ignored bundles
 * @param bundleSymbolicNames Bundle symbolic names
 */
public record BundleStatus(String statusLine, int total, int active, int activeFragment,
    int resolved, int installed, int ignored,
    Set<String> bundleSymbolicNames) {

  /**
   * @return Compact version of status line.
   */
  public String getStatusLineCompact() {
    StringBuilder sb = new StringBuilder();
    sb.append(total).append(" total");
    if (active > 0) {
      sb.append(", ").append(active).append(" active");
    }
    if (activeFragment > 0) {
      sb.append(", ").append(activeFragment).append(" fragment");
    }
    if (resolved > 0) {
      sb.append(", ").append(resolved).append(" resolved");
    }
    if (installed > 0) {
      sb.append(", ").append(installed).append(" installed");
    }
    if (ignored > 0) {
      sb.append(", ").append(ignored).append(" ignored");
    }
    return sb.toString();
  }

  /**
   * @return true if no bundles are in "installed" or "resolved" state.
   */
  public boolean isAllBundlesRunning() {
    return installed() + resolved() == 0;
  }

  /**
   * @param symbolicName Bundle symbolic name
   * @return true if the given bundle is contained in the bundle list
   */
  public boolean containsBundle(String symbolicName) {
    return bundleSymbolicNames.contains(symbolicName);
  }

  /**
   * Checks if a bundle with the given pattern exists in the bundle list.
   * @param symbolicNamePattern Bundle symbolic name pattern
   * @return Bundle name if a bundle was found, null otherwise
   */
  public String getMatchingBundle(Pattern symbolicNamePattern) {
    for (String bundleSymbolicName : bundleSymbolicNames) {
      if (symbolicNamePattern.matcher(bundleSymbolicName).matches()) {
        return bundleSymbolicName;
      }
    }
    return null;
  }

}
