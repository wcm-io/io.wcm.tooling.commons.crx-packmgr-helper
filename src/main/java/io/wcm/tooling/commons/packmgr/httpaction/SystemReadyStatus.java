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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO to map systemready JSON response to via Jackson.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class SystemReadyStatus {

  private final String overallResult;
  private final List<Result> results;

  /**
   * Constructor.
   * @param overallResult overall result
   * @param results list of individual results
   */
  public SystemReadyStatus(@JsonProperty("overallResult") String overallResult,
      @JsonProperty("results") List<Result> results) {
    this.overallResult = overallResult;
    this.results = results;
  }

  public String getOverallResult() {
    return overallResult;
  }

  public List<Result> getResults() {
    return results;
  }

  /**
   * Represents a single health check result.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Result {

    private final String name;
    private final String status;
    private final long timeInMs;
    private final String finishedAt;
    private final List<String> tags;
    private final List<Message> messages;

    /**
     * Constructor.
     * @param name name
     * @param status status
     * @param timeInMs time in ms
     * @param finishedAt finished at
     * @param tags tags
     * @param messages messages
     */
    public Result(@JsonProperty("name") String name,
        @JsonProperty("status") String status,
        @JsonProperty("timeInMs") long timeInMs,
        @JsonProperty("finishedAt") String finishedAt,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("messages") List<Message> messages) {
      this.name = name;
      this.status = status;
      this.timeInMs = timeInMs;
      this.finishedAt = finishedAt;
      this.tags = tags;
      this.messages = messages;
    }

    public String getName() {
      return name;
    }

    public String getStatus() {
      return status;
    }

    public long getTimeInMs() {
      return timeInMs;
    }

    public String getFinishedAt() {
      return finishedAt;
    }

    public List<String> getTags() {
      return tags;
    }

    public List<Message> getMessages() {
      return messages;
    }

  }

  /**
   * Represents a message within a health check result.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Message {

    private final String status;
    private final String message;

    /**
     * Constructor.
     * @param status status
     * @param message message
     */
    public Message(@JsonProperty("status") String status,
        @JsonProperty("message") String message) {
      this.status = status;
      this.message = message;
    }

    public String getStatus() {
      return status;
    }

    public String getMessage() {
      return message;
    }

  }

}