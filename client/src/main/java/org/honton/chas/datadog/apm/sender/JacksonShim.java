package org.honton.chas.datadog.apm.sender;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Minimize values send in json message
 */
@Slf4j
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonShim implements ContextResolver<ObjectMapper> {

  private final static ObjectMapper MINIMAL_OBJECT_MAPPER = createMinimalObjectMapper();

  // see MsgPackProvider.createMinimalObjectMapper()
  private static ObjectMapper createMinimalObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    return mapper;
  }

  @Override
  public ObjectMapper getContext(final Class<?> type) {
    return MINIMAL_OBJECT_MAPPER;
  }
}
