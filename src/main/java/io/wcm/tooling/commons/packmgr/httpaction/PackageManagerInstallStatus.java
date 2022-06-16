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

/**
 * Wrapper for Status summary from package manager install status
 */
public final class PackageManagerInstallStatus {

  private final boolean finished;
  private final int itemCount;

  PackageManagerInstallStatus(boolean finished, int itemCount) {
    this.finished = finished;
    this.itemCount = itemCount;
  }

  /**
   * @return true when installation of packages is finished.
   */
  public boolean isFinished() {
    return this.finished;
  }

  /**
   * @return Number of packages which are not yet fully installed.
   */
  public int getItemCount() {
    return this.itemCount;
  }

}
