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
package io.wcm.tooling.commons.packmgr.unpack;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class ContentUnpackerTest {

  private static final String[] EXCLUDE_FILES = new String[] {
    ".*/sling-ide-tooling/.*",
    "^META-INF/.*"
  };
  private static final String[] EXCLUDE_NODES = new String[] {
    "^.*/scheduleday_0$"
  };
  private static final String[] EXCLUDE_PROPERTIES = new String[] {
    "jcr\\:created",
    "jcr\\:createdBy",
    "jcr\\:lastModified",
    "jcr\\:lastModifiedBy",
    "cq\\:lastModified",
    "cq\\:lastModifiedBy"
  };

  private ContentUnpacker underTest;

  @Before
  public void setUp() throws Exception {
    ContentUnpackerProperties props = new ContentUnpackerProperties();
    props.setExcludeFiles(EXCLUDE_FILES);
    props.setExcludeNodes(EXCLUDE_NODES);
    props.setExcludeProperties(EXCLUDE_PROPERTIES);
    underTest = new ContentUnpacker(props);
  }

  @Test
  public void testUnpack() throws Exception {
    File contentPackage = new File("src/test/resources/content-package-test.zip");
    File outputDirectory = new File("target/unpacktest");
    outputDirectory.mkdirs();
    underTest.unpack(contentPackage, outputDirectory);
  }

}
