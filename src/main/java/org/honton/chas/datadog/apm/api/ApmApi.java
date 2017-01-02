package org.honton.chas.datadog.apm.api;

import java.util.List;
import java.util.Map;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The APM operations.
 */
@Produces(MediaType.TEXT_PLAIN)
public interface ApmApi {

  /**
   * The response when invocation succeeds.
   */
  String SUCCESS = "OK\n";

  /**
   * Report a map of service definitions
   *
   * @param apps A map of service name to service information
   * @return result of PUT, usually {@link #SUCCESS}
   */
  @PUT
  @Path("/services")
  String reportServices(Map<String, ServiceData> apps);

  /**
   * Report an ordered collection of traces.
   * 
   * @param traces
   * @return result of PUT, usually {@link #SUCCESS}
   */
  @PUT
  @Path("/traces")
  String reportTraces(List<Trace> traces);
}
