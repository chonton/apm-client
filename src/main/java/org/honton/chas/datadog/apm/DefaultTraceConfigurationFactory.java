package org.honton.chas.datadog.apm;

import java.util.concurrent.TimeUnit;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

/**
 * Factory default trace client configuration values.
 * Any non-@Alternative cdi @Produces factory method in application code.
 */
@Priority(0)
public class DefaultTraceConfigurationFactory {
  /**
   * Get the default configuration.  Only called if CDI does not find any other Producer.
   * @return The default configuration
   */
  @Alternative
  @Produces
  public TraceConfiguration getDefault() {
    return new TraceConfiguration(
        "service",
        "http://localhost:7777",
        TimeUnit.MINUTES.toMillis(15));
  }
}
