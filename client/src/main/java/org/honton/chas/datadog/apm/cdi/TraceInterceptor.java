package org.honton.chas.datadog.apm.cdi;

import org.honton.chas.datadog.apm.SpanBuilder;
import org.honton.chas.datadog.apm.TraceOperation;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.Priorities;
import java.lang.reflect.Method;

/**
 * CDI interceptor that reports invocations of methods annotated with {@link TraceOperation} with value == true
 */
@TraceOperation
@Interceptor
@Priority(Priorities.AUTHENTICATION-1)
public class TraceInterceptor {

  @Inject
  TracerImpl tracer;

  @AroundInvoke
  public Object invokeWithReporting(InvocationContext ctx) throws Exception {
    SpanBuilder span = tracer.createSpan();
    try {
      return ctx.proceed();
    } catch (Exception e) {
      span.exception(e);
      throw e;
    } finally {
      Method method = ctx.getMethod();
      span
        .resource(method.getDeclaringClass().getSimpleName())
        .operation(method.getName())
        .type(getType(method));
      tracer.closeSpan(span);
    }
  }

  private static String getType(Method method) {
    TraceOperation traceOperation = method.getAnnotation(TraceOperation.class);
    if(traceOperation == null) {
      traceOperation = method.getDeclaringClass().getAnnotation(TraceOperation.class);
      if (traceOperation == null) {
        return null;
      }
    }
    return traceOperation.type().isEmpty() ?TraceOperation.UNKNOWN :traceOperation.type();
  }
}
