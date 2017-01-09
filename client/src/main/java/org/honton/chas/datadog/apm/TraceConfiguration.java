package org.honton.chas.datadog.apm;

import javax.annotation.Priority;
import lombok.Value;

/**
 * The trace server configuration values.
 * Usually provided by a cdi @Produces factory method in application code.
 */
@Value
@Priority(0)
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
   * The number of milliseconds that collection is quenched after collection failure
   */
  private long backoffDuration;
}
