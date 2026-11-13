/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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
package io.wcm.tooling.commons.packmgr.install.crx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jackrabbit.vault.packaging.DependencyHandling;
import org.junit.jupiter.api.Test;

import io.wcm.tooling.commons.packmgr.install.PackageFile;

class CrxPackageInstallerTest {

  private static final String URL = "http://localhost:4502/crx/packmgr/service";
  private static final String PATH = "/etc/packages/mygroup/my package.zip";

  private final CrxPackageInstaller underTest = new CrxPackageInstaller(URL);

  @Test
  void testBuildInstallUrl_Default() throws Exception {
    PackageFile packageFile = new PackageFile();
    assertEquals(URL + "/console.html/etc/packages/mygroup/my%20package.zip?cmd=install&recursive=true",
        underTest.buildInstallUrl(PATH, packageFile));
  }

  @Test
  void testBuildInstallUrl_DependencyHandling() throws Exception {
    PackageFile packageFile = new PackageFile();
    packageFile.setRecursive(false);
    packageFile.setDependencyHandling(DependencyHandling.BEST_EFFORT);
    assertEquals(URL + "/console.html/etc/packages/mygroup/my%20package.zip?cmd=install&dependencyHandling=best_effort",
        underTest.buildInstallUrl(PATH, packageFile));
  }

}
