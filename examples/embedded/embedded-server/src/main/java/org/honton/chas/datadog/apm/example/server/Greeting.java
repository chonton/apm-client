package org.honton.chas.datadog.apm.example.server;

import org.honton.chas.datadog.apm.TraceOperation;

/**
 * The inner implementation of the greeting
 */
@TraceOperation
public class Greeting {

  public String kr() {
     return "Hello World!";
  }
}
