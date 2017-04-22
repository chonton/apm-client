package org.honton.chas.datadog.apm;

import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * The trace server configuration values.
 * Usually provided by a cdi @Produces factory method in application code.
 */
@Value
@RequiredArgsConstructor
public class TraceConfiguration {

  /**
   * The service name reported to the collector
   */
  private String service;

  /**
   * The collector url
   */
  private String collectorUrl;

  /**
   * The number of milliseconds that collection is squelched after communication failure
   */
  private long backoffDuration;

  /**
   * Invoked whenever a root span is created
   */
  private SpanBuilder.Augmenter rootAugmenter;

  public TraceConfiguration(String service, String collectorUrl, long backoffDuration) {
    this(service, collectorUrl, backoffDuration, null);
  }
}
