package org.honton.chas.datadog.apm.example.hello;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Endpoint to retrieve greetings
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public interface Hello {

  /**
   * Get the greeting in plain text
   * @return The greeting
   */
  @GET
  @Path("/greetings")
  String greeting();
}
