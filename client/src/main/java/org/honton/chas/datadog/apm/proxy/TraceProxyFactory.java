package org.honton.chas.datadog.apm.proxy;

import lombok.SneakyThrows;
import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.TraceOperation;
import org.honton.chas.datadog.apm.Tracer;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Factory that creates instance wrappers which report spans.  Create proxies for those instances
 * that are not constructed by CDI.
 */
public class TraceProxyFactory {

  @Inject
  Tracer tracer;

  /**
   * Get a proxy to an instance.  The proxy will create spans for every invocation of interface methods.
   * Methods not part of the interface or self-invocations will not be intercepted.
   *
   * @param <T> The interface to intercept
   * @param instance A reference to a non-CDI object which needs interception
   * @param iface The interface which will be intercepted
   * @return A proxy which creates spans on every invoked method
   */
  @SneakyThrows
  public <T> T createProxy(final T instance, Class<T> iface) {
    return (T)createProxy(instance, new Class[]{iface});
  }

  /**
   * Get a proxy to an instance.  The proxy will create spans for every invocation of interfaces methods.
   * Methods not part of the interfaces or self-invocations will not be intercepted.
   *
   * @param instance A reference to a non-CDI object which needs interception
   * @param ifaces The interfaces which will be intercepted
   * @return A reference which may be cast to any of the specified interfaces.
   */
  @SneakyThrows
  public Object createProxy(final Object instance, Class<?>... ifaces) {
    InvocationHandler handler = createInvocationHandler(instance);
    return Proxy.newProxyInstance(instance.getClass().getClassLoader(), ifaces, handler);
  }

  private static boolean shouldTrace(Method method) {
    TraceOperation traceOperation = method.getAnnotation(TraceOperation.class);
    if (traceOperation == null) {
      traceOperation = method.getDeclaringClass().getAnnotation(TraceOperation.class);
      if (traceOperation == null) {
        return true;
      }
    }
    return traceOperation.value();
  }

  private InvocationHandler createInvocationHandler(final Object instance) {
    return new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          if(!shouldTrace(method)) {
            return method.invoke(instance, args);
          }

          SpanBuilder span = tracer.createSpan();
          try {
            return method.invoke(instance, args);
          }
          catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            span.exception(cause);
            throw cause;
          }
          finally {
            span.resource(method.getDeclaringClass().getCanonicalName()).operation(method.getName());
            tracer.closeSpan(span);
          }
        }
      };
  }
}

