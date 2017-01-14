package org.honton.chas.datadog.apm.example.server;

import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import org.honton.chas.datadog.apm.TraceConfiguration;

@ApplicationScoped
public class HelloServiceConfiguration {

  /**
   * Get the configuration.
   * @return The configuration
   */
  @Produces
  static TraceConfiguration getDefault() {
    return new TraceConfiguration(
      "greetings-server",
      "http://localhost:7777",
      TimeUnit.MINUTES.toMillis(1));
  }
}
