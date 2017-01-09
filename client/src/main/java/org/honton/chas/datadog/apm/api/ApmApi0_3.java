package org.honton.chas.datadog.apm.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;

/**
 * Version 0.3 of APM
 */
@Consumes("application/msgpack")
@Path("/v0.3")
public interface ApmApi0_3 extends ApmApi {
}
