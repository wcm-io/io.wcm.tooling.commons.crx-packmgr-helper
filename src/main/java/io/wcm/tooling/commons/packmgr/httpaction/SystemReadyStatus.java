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

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO to map system ready JSON response to via Jackson.
 * @param overallResult overall result
 * @param results list of individual results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SystemReadyStatus(@JsonProperty("overallResult") String overallResult,
    @JsonProperty("results") List<Result> results) {

  private static final String STATUS_OK = "OK";

  /**
   * Checks if the overall system ready status is OK.
   * @return true if overall status is OK
   */
  public boolean isSystemReadyOK() {
    return StringUtils.isBlank(overallResult) || Strings.CI.equals(overallResult, STATUS_OK);
  }

  /**
   * Returns a human-readable string with failure information if the system is not ready.
   * If the system is ready, returns null.
   * @return failure information string or null
   */
  public String getFailureInfoString() {
    if (isSystemReadyOK()) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (Result result : Optional.ofNullable(results).orElse(List.of())) {
      if (!Strings.CI.equals(result.status(), STATUS_OK)) {
        sb.append("- ").append(result.status()).append(": ").append(result.name()).append("\n");
        for (Message message : Optional.ofNullable(result.messages()).orElse(List.of())) {
          String notOkStatus = "";
          if (!Strings.CI.equals(message.status(), STATUS_OK)) {
            notOkStatus = message.status() + ": ";
          }
          sb.append("  * ").append(notOkStatus).append(message.message()).append("\n");
        }
      }
    }
    return sb.toString();
  }

  /**
   * Represents a single health check result.
   * @param name name
   * @param status status
   * @param timeInMs time in ms
   * @param finishedAt finished at
   * @param tags tags
   * @param messages messages
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Result(@JsonProperty("name") String name,
      @JsonProperty("status") String status,
      @JsonProperty("timeInMs") long timeInMs,
      @JsonProperty("finishedAt") String finishedAt,
      @JsonProperty("tags") List<String> tags,
      @JsonProperty("messages") List<Message> messages) {

  }

  /**
   * Represents a message within a health check result.
   * @param status status
   * @param message message
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Message(@JsonProperty("status") String status,
      @JsonProperty("message") String message) {

  }

}