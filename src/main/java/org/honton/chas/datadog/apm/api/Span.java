package org.honton.chas.datadog.apm.api;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

/**
 * A span sent to the APM collector.
 */
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Span {

  /**
   * The service name.
   */
  private final String service;

  /**
   * The resource name.
   */
  private final String resource;

  /**
   * The operation name.
   */
  @JsonProperty("name")
  private final String operation;

  /**
   * The id of the trace's root span.
   */
  @JsonProperty("trace_id")
  private final long traceId;

  /**
   * The id of the span's direct parent span.
   */
  @JsonProperty("parent_id")
  private final Long parentId;

  /**
   * The id of the span.
   */
  @JsonProperty("span_id")
  private final long spanId;

  /**
   * The type of the span. e.g. http, sql
   */
  private final String type;

  /**
   * The tags in the span.
   */
  private final Map<String,String> meta;

  /**
   * The metrics in the span.
   */
  private final Map<String,Number> metrics;

  /**
   * An error code for the span
   */
  private final int error;

  /**
   * The span start wall time since epoch in nanoseconds
   */
  private final long start;

  /**
   * The span duration in nanoseconds
   */
  private final long duration;
}
