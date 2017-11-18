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
   * The service name reported to the collector.
   */
  private String service;

  /**
   * The collector url.   If null or empty, will disable the collector.
   */
  private String collectorUrl;

  /**
   * The number of milliseconds that collection is squelched after communication failure.
   */
  private long backoffDuration;
}
