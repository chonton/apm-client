# apm-client
This java client intercepts servlet requests, jax-rs client requests, and bean methods.  The resource
and method names as well as the wall time and duration of the request are recorded in spans.  These
spans are queued and sent as REST messages to a [Datadog APM collector](https://www.datadoghq.com/apm/).

[Javadoc](https://chonton.github.io/apm-client/0.0.6/client/apidocs/index.html) and [build reports](https://chonton.github.io/apm-client/0.0.6/client/project-reports.html) are available.

### Requirements
* Minimal latency in the mainline processing
* Some, but not extreme, buffering of outgoing messages
* Thread-safe sender
* Lack of APM collector will be logged, but not cause failure of mainline processing

### Assumptions
* Minimum of Java 8
* Local (on the same host) APM collector
* [CDI](http://www.cdi-spec.org/) implementation such as [Weld](http://weld.cdi-spec.org/)
* [Slf4J](https://www.slf4j.org/) compliant logging implementation such as [Logback](http://logback.qos.ch/)
* [Jax-Rs](https://jax-rs-spec.java.net/) client for optional support of exporting traces

## Maven Coordinates
To include apm-client in your maven build, use the following fragment in your pom.
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.honton.chas.datadog.apm</groupId>
        <artifactId>client</artifactId>
        <version>0.0.6</version>
      </plugin>
    </plugins>
  </build>
```

## Configuration
To configure apm-client, you must supply a CDI factory method which produces a TraceConfiguration
instance.  Three attributes are configured:
* The service name reported with each span sent to Datadog APM collector.
* The URL of the local Datadog APM collector.  If null or empty, this will prevent sending traces.
* The number of milliseconds to backoff.

After any communication failure, the apm-client logs the failure and will not further attempt to 
send span information for the backoff period.  During this period all spans are dropped.

```java
public class TraceConfigurationFactory {
  
  /**
   * Get the configuration.
   * @return The configuration
   */
  @Produces
  static TraceConfiguration getDefault() {
    return new TraceConfiguration(
      "service-name",       // service name
      "http://localhost:8126",  // apm collector url
      TimeUnit.MINUTES.toMillis(1));  // backoff period
  }
}
```

## Servlet or Container Filter
On the server side, you can either use TraceServletFilter or TraceContainerFilter to trace incoming requests.
TraceServletFilter can trace any servlet request and annotates the trace with the incoming URI.
TraceContainerFilter can trace any jax-rs request and annotates the trace with the serving class and method.

## TraceServletFilter
The TraceServletFilter traces every incoming request.  The http request host/port is
reported as the trace resource and the http request method and url are reported as the trace name. 
If the client request includes the
**x-ddtrace-parent_trace_id** and **x-ddtrace-parent_span_id** headers, that indicated span is used as
the parent trace and span.  Otherwise, a new trace is created.  Once the request is complete, the
new span or trace is closed and sent to Datadog APM.

### Registration in War
Register TraceServletFilter, weld, and Jax-Rs application in the web.xml:

```xml
<?xml version="1.0" encoding="ISO-8859-1"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
  version="3.1">
  
  <!-- servlet filter -->
  <filter>
    <filter-class>org.honton.chas.datadog.apm.servlet.TraceServletFilter</filter-class>
  </filter>
  
  <!-- CDI implementation -->
  <listener>
    <listener-class>org.jboss.weld.environment.servlet.Listener</listener-class>
  </listener>
  
  <!-- Application -->
  <servlet>
    <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
    <init-param>
      <param-name>javax.ws.rs.Application</param-name>
      <param-value>org.honton.chas.datadog.apm.example.server.HelloApplication</param-value>
    </init-param>
  </servlet>
  
</web-app>
```

### or Registration with embedded Jetty
```java
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
```

## TraceContainerFilter
The TraceContainerFilter traces every incoming jax-rs request.  The implementation class and method are
reported as the trace resource and name.  If the client request includes the
**x-ddtrace-parent_trace_id** and **x-ddtrace-parent_span_id** headers, that indicated span is used as
the parent trace and span.  Otherwise, a new trace is created.  Once the request is complete, the
new span or trace is closed and sent to Datadog APM.

### Registration
The TraceContainerFilter must be registered with the jax-rs runtime.  This can be done during startup as
part of the Application class.
```java
@ApplicationPath("/")
public class HellloApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();

    // Register the service endpoint
    classes.add(HelloService.class);

    // the Tracing filter
    classes.add(TraceContainerFilter.class);

    return classes;
  }
}
```

## TraceClientFilter
TraceClientFilter exports outgoing requests.  Prior to sending the request, a new span is created.
The outgoing requests includes **x-ddtrace-parent_trace_id** and **x-ddtrace-parent_span_id** headers
indicating that new span.  Once the request has completed, the span is closed and sent to Datadog APM.

```java
public class ClientProxyFactory {

  private ResteasyClientBuilder clientBuilder;

  @Inject
  void setTraceFilter(TraceClientFilter filter) {
    clientBuilder = new ResteasyClientBuilder();
    clientBuilder.register(filter);
  }

  public <T> T getProxy(String url, Class<T> cls) {
    return clientBuilder.build().target(url).proxy(cls);
  }
}
```

## TraceInterceptor
The TraceOperation annotation instructs TraceInterceptor to create a new span is before entering a
CDI bean operation and close the span once the operation is complete.  Completed spans are sent to
Datadog APM.  The TraceOperation annotation can be placed on the class or the method definition.
Placing the annotation on a method will cause any invocation from outside the class to be traced. 
Placing the annotation on a class will cause all methods in that class to be traced, unless the
method is annotated with **@TraceOperation(false)**.
```java
@TraceOperation(type=TraceOperation.DB)
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
```

## TraceProxyFactory
Occasionally, you will need to intercept operations on non-cdi instances.  In this case, you can 
create a proxy which will create a new span before invoking any interface method and close the span
once the invocation is complete.
```java
public class BeanFactory {
  
  @Inject
  TraceProxyFactory proxyFactory;
  
  @Produces
  public NonCdi factoryMethod() {
    return proxyFactory.createProxy(new NonCdiImpl(), NonCdi.class);
  }
}
```

## Tracer
Similarly, application code can use the Tracer create a new span is before entering a Callable or
Runnable and close the spans once the calls are complete.  Completed spans are immediately
queued to send to Datadog APM.  Inject the Tracer to report spans with application code.
```java
  @Inject
  private Tracer tracer;
  
  public String someMethod() {
    return tracer.executeCallable("resource", "operation", () -> {  
      // code to run inside span
      return "returnValue";
    });
  }
```

# Update Notes

## 0.0.5 to 0.0.6
The URL of the local Datadog APM collector can be set to null or empty; this will prevent sending traces.

## 0.0.4 to 0.0.5
An incompatible change was made in the encoding of numbers in the trace and span headers.  Prior to
0.0.5, the numbers were encoded in hexadecimal.  From 0.0.5 onwards the encoding is in decimal.  Additionally,
prior to version 0.0.5, the parsing of header did not handle invalid numbers.  To upgrade without downtime, 
rebuild and deploy the more dependent services first.

