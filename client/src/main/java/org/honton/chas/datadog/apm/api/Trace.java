package org.honton.chas.datadog.apm.api;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A ordered collection of spans.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trace {
  private final List<Span> spans;

  @JsonValue
  public List<Span> getSpans() {
    return spans;
  }

  @JsonCreator
  public Trace(List<Span> spans) {
    this.spans = spans;
  }

  public Trace(Span... span) {
    this(Arrays.asList(span));
  }
}
