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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import io.wcm.tooling.commons.packmgr.PackageManagerHttpActionException;

class SystemReadyStatusCallTest {

  @Test
  void testToSystemReadyStatus() throws IOException {
    // Load test JSON file
    try (InputStream is = getClass().getResourceAsStream("/packmgr/systemreadyresponse.json")) {
      String jsonContent = IOUtils.toString(is, StandardCharsets.UTF_8);

      // Parse JSON using the static method
      SystemReadyStatus status = SystemReadyStatusCall.toSystemReadyStatus(jsonContent, null);

      // Verify overall result
      assertNotNull(status);
      assertEquals("CRITICAL", status.getOverallResult());

      // Verify results list (do not check further details here)
      assertNotNull(status.getResults());
      assertEquals(7, status.getResults().size());
    }
  }

  @Test
  void testToSystemReadyStatus_InvalidJson() {
    String invalidJson = "{ invalid json }";

    assertThrows(
        PackageManagerHttpActionException.class,
        () -> SystemReadyStatusCall.toSystemReadyStatus(invalidJson, null)
    );
  }

  @Test
  void testToSystemReadyStatus_EmptyJson() {
    String emptyJson = "{}";

    // Should not throw exception, but fields should be null
    SystemReadyStatus status = SystemReadyStatusCall.toSystemReadyStatus(emptyJson, null);
    assertNotNull(status);
    // Fields will be null since they're not present in JSON
  }

}
