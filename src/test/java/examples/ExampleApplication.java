package examples;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.honton.chas.datadog.apm.TraceContainerFilter;

/**
 * JaxRs Application
 */
@ApplicationPath("/")
public class ExampleApplication extends Application {

  /**
   * Register provider instances
   */
  @Override
  public Set<Object> getSingletons() {
    Set<Object> singletons = new HashSet<>();
    // ... other work

    // Register the Tracing filter
    singletons.add(new TraceContainerFilter());
    return singletons;
  }

}
