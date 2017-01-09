package org.honton.chas.datadog.apm.example.server;

import javax.enterprise.context.ApplicationScoped;
import org.honton.chas.datadog.apm.TraceOperation;

/**
 * The inner implementation of the greeting
 */
@ApplicationScoped
@TraceOperation
public class Greeting {

  public String kr() {
     return "Hello World!";
  }
}
