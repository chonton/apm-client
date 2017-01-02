# apm-client
A java client to push TCP messages to a datadog APM collector.

### Requirements
* Minimal latency in the mainline processing
* Some, but not extreme buffering of outgoing messages
* Thread-safe sender
* Lack of APM collector will be noted, but not cause failure of mainline processing

### Assumptions
* A local (on the same host) APM collector

## Maven Coordinates
To include dogstatd-client in your maven build, use the following fragment in your pom.
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.honton.chas.datadog</groupId>
        <artifactId>apm-client</artifactId>
        <version>0.0.1-SNAPSHOT</version>
      </plugin>
    </plugins>
  </build>
```

## Use with CDI
Use CDI to inject the Tracer and intercept bean invocations.

#### Access the Tracer
```java

  @Inject
  private Tracer tracer;

```

### Use TraceOperation annotation to report bean invocation.
```java
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
```

## Typical Jax-RS Use

### Register TraceContainerFilter with JaxRS to trace incoming requests
```java
  @ApplicationPath("/")
  public class ExampleApplication extends Application {
    /**
     * Register provider instances
     */
    @Override
    public Set<Object> getSingletons() {
      Set<Object> singletons = new HashSet<>();
      // Register the Tracing filter
      singletons.add(new TraceContainerFilter());
      return singletons;
    }
  }
```

### Register TraceClientFilter with JaxRS to trace outgoing requests
```java
  /**
   * Create a JaxRs Client with registered TraceClientFilter
   */
  public Client createClient() {
    return ClientBuilder.newBuilder()
        .register(TraceClientFilter.class)
        .build();
  }
```

## Functional Java Use

```java

  String rc = tracer.executeCallable("resource", "operation", () -> {
    // code to run inside span
    return "returnValue";
  });

```
