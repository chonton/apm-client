package org.honton.chas.datadog.apm.example.server;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Endpoint for network infrastructure to determine health.
 */
@ApplicationScoped
@Path("/health")
public class HealthService {

  /**
   * Get a health response in plain text
   * @return The health response
   */
  @Produces(MediaType.TEXT_PLAIN)
  @GET
  public String health() {
    return "OK";
  }
}
