package org.honton.chas.datadog.apm.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Version 0.2 of APM
 */
@Consumes(MediaType.APPLICATION_JSON)
@Path("/v0.2")
public interface ApmApi0_2 extends ApmApi {
}
