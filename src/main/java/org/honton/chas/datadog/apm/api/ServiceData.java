package org.honton.chas.datadog.apm.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceData {

  /**
   * The application name.
   */
  private final String app;

  /**
   * The application type.
   */
  @JsonProperty("app_type")
  private final String appType;
}