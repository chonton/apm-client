package org.honton.chas.datadog.apm.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * Provider for "application/msgpack" MessageReader / MessageWriter
 */
@Provider
@Produces("application/msgpack")
@Consumes("application/msgpack")
public class MsgPackProvider extends JacksonJsonProvider {

  private final static ObjectMapper MINIMAL_OBJECT_MAPPER = createMinimalObjectMapper();

  // see JacksonShim.createMinimalObjectMapper()
  private static ObjectMapper createMinimalObjectMapper() {
    ObjectMapper mapper = new ObjectMapper(new MessagePackFactory());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    return mapper;
  }

  /**
   * Create the provider
   */
  public MsgPackProvider() {
    super(MINIMAL_OBJECT_MAPPER);
  }

  @Override
  protected boolean hasMatchingMediaType(MediaType mediaType) {
    return "application".equals(mediaType.getType()) && "msgpack".equals(mediaType.getSubtype());
  }
}
