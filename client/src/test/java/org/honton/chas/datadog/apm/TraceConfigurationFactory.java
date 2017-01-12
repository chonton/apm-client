package org.honton.chas.datadog.apm;

import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.Produces;

/**
 * Trace server configuration values for testing
 */
public class TraceConfigurationFactory {

  public static final TraceConfiguration DEFAULTS = new TraceConfiguration(
        "service",
          "http://localhost:7777",
    TimeUnit.MINUTES.toMillis(15));

  /**
   * Get the default configuration.  Only called if CDI does not find any other Producer.
   * @return The default configuration
   */
  @Produces
  static public TraceConfiguration getDefault() {
    return DEFAULTS;
  }
}
