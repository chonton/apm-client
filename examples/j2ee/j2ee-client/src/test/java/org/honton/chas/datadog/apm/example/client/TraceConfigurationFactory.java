package org.honton.chas.datadog.apm.example.client;

import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.Produces;
import org.honton.chas.datadog.apm.TraceConfiguration;

public class TraceConfigurationFactory {

  /**
   * Get the configuration.
   * @return The configuration
   */
  @Produces
  static TraceConfiguration getDefault() {
    return new TraceConfiguration(
      "greetings-client",
      "http://localhost:8126",
      TimeUnit.MINUTES.toMillis(1));
  }
}
