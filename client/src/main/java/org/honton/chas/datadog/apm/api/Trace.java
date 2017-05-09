package org.honton.chas.datadog.apm.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

/**
 * A ordered collection of spans.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
public class Trace {
  private final Span[] spans;

  @JsonValue
  public Span[] getSpans() {
    return spans;
  }

  @JsonCreator
  public Trace(Span... span) {
    spans = span;
  }
}
