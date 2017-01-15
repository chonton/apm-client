package org.honton.chas.datadog.apm;

import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.Priorities;

/**
 * CDI interceptor that reports invocations of methods annotated with {@link TraceOperation} with value == true
 */
@TraceOperation
@Interceptor
@Priority(Priorities.AUTHENTICATION-1)
public class TraceInterceptor {

  @Inject
  Tracer tracer;

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
      span.resource(method.getDeclaringClass().getCanonicalName()).operation(method.getName());
      tracer.closeSpan(span);
    }
  }
}
