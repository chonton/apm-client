package org.honton.chas.datadog.apm;

import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import lombok.Value;

/**
 * The trace client configuration values.
 * Usually provided by a cdi @Produces factory method in application code.
 */
@Value
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

  /**
   * Get the default configuration.  Only called if CDI does not find any other Producer.
   * @return The default configuration
   */
  @Alternative
  @Produces
  static TraceConfiguration getDefault() {
    return new TraceConfiguration(
        "service",
        "http://localhost:7777",
        TimeUnit.MINUTES.toMillis(15));
  }
}
