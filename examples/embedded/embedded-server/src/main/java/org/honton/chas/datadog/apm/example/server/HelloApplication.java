package org.honton.chas.datadog.apm.example.server;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.jackson.JacksonFeature;

@ApplicationPath("/")
public class HelloApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();

    // Use Jackson for media types
    classes.add(JacksonFeature.class);

    // Register the service endpoint
    classes.add(HelloService.class);

    // Register the infrastructure endpoint
    classes.add(HealthService.class);

    // the Tracing filter is added in HelloMain

    return classes;
  }
}