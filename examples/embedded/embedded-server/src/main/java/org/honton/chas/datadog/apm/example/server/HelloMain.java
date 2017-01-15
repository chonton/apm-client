package org.honton.chas.datadog.apm.example.server;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.honton.chas.datadog.apm.servlet.TraceServletFilter;

/**
 * Start an embedded Jetty server
 * This is in place of a web.xml or a war
 */
@Slf4j
public class HelloMain {

  private final int port;
  private final Server jettyServer;
  private final ServletContextHandler context;

  public HelloMain(int port) {
    this.port = port;

    context = new ServletContextHandler();
    context.setContextPath("/");

    // Use Weld to inject into servlets
    context.addEventListener(new org.jboss.weld.environment.servlet.Listener());

    // Add the Tracing Filter
    context.addFilter(TraceServletFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

    jettyServer = new Server(port);
    jettyServer.setHandler(context);

    addJaxRsApplication(HelloApplication.class);
    start();
  }

  private void addJaxRsApplication(Class<? extends Application> appClass) {
    String pathSpec = getPathSpec(appClass);
    ServletHolder jerseyHolder = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, pathSpec);
    jerseyHolder.setInitParameter("javax.ws.rs.Application", appClass.getCanonicalName());
    jerseyHolder.setInitOrder(0);
  }

  private String getPathSpec(Class<? extends Application> appClass) {
    ApplicationPath applicationPath = appClass.getAnnotation(ApplicationPath.class);
    StringBuilder pathSpec = new StringBuilder(applicationPath.value());
    if(pathSpec.length()==0 || pathSpec.charAt(pathSpec.length()-1)!='/') {
      pathSpec.append('/');
    }
    pathSpec.append('*');
    return pathSpec.toString();
  }

  private void start() {
    try {
      jettyServer.start();
      log.info("Started service on port {}", port);
      jettyServer.join();
    } catch (Throwable t) {
      log.error("failed to start server ", t);
    } finally {
      stop();
    }
  }

  void stop() {
    try {
      if (!jettyServer.isStopped()) {
        jettyServer.stop();
      }
    } catch (Exception e) {
      log.error("exception stopping server ", e);
    }
    try {
      jettyServer.destroy();
    } catch (Exception e) {
      log.error("exception destroying server ", e);
    }
  }

  /**
   * Main entry point, start the embedded jetty server
   */
  public static void main(String[] args) {
    new HelloMain(5555);
  }
}