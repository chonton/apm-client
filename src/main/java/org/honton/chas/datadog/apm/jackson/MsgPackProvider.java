package org.honton.chas.datadog.apm.jackson;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.msgpack.jackson.dataformat.MessagePackFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Provider for "application/msgpack" MessageReader / MessageWriter
 */
@Provider
@Produces("application/msgpack")
@Consumes("application/msgpack")
public class MsgPackProvider extends JacksonJsonProvider {
  /**
   * Create the provider
   */
  public MsgPackProvider() {
    super(new ObjectMapper(new MessagePackFactory()));
  }

  @Override
  protected boolean hasMatchingMediaType(MediaType mediaType) {
    return "application".equals(mediaType.getType()) && "msgpack".equals(mediaType.getSubtype());
  }
}
