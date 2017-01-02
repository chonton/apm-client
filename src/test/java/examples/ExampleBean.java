package examples;

import org.honton.chas.datadog.apm.TraceOperation;

/**
 * CDI bean
 */
@TraceOperation
public class ExampleBean {

  /**
   * Trace turned on at class level
   */
  public void methodToTrace() {
    // ...
  }

  /**
   * Trace turned off at method level
   */
  @TraceOperation(false)
  public void dontTrace() {
    // ...
  }
}
