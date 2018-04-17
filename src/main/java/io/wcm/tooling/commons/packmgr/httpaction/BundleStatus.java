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

/**
 * Wrapper for Status summary from Web Console Bundles Status info JSON.
 */
public final class BundleStatus {

  private final String statusLine;
  private final int total;
  private final int active;
  private final int activeFragment;
  private final int resolved;
  private final int installed;
  private final Set<String> bundleSymbolicNames;

  BundleStatus(String statusLine, int total, int active, int activeFragment, int resolved, int installed,
      Set<String> bundleSymbolicNames) {
    this.statusLine = statusLine;
    this.total = total;
    this.active = active;
    this.activeFragment = activeFragment;
    this.resolved = resolved;
    this.installed = installed;
    this.bundleSymbolicNames = bundleSymbolicNames;
  }

  public String getStatusLine() {
    return this.statusLine;
  }

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
    return sb.toString();
  }

  public int getTotal() {
    return this.total;
  }

  public int getActive() {
    return this.active;
  }

  public int getActiveFragment() {
    return this.activeFragment;
  }

  public int getResolved() {
    return this.resolved;
  }

  public int getInstalled() {
    return this.installed;
  }

  /**
   * @return true if no bundles are in "installed" or "resolved" state.
   */
  public boolean isAllBundlesRunning() {
    return getInstalled() + getResolved() == 0;
  }

  /**
   * @param symbolicName Bundle symbolic name
   * @return true if the given bundle is contained in the bundle list
   */
  public boolean containsBundle(String symbolicName) {
    return bundleSymbolicNames.contains(symbolicName);
  }

}
