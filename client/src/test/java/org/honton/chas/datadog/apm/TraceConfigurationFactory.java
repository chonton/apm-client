package org.honton.chas.datadog.apm;

import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.Produces;

/**
 * Trace server configuration values for testing
 */
public class TraceConfigurationFactory {

  public static final TraceConfiguration DEFAULTS = new TraceConfiguration(
        "service",
          "http://localhost:1", // no-one should be listening to this port
    TimeUnit.MINUTES.toMillis(15));

  /**
   * Get the default configuration.
   * @return The default configuration
   */
  @Produces
  static public TraceConfiguration getDefault() {
    return DEFAULTS;
  }
}
