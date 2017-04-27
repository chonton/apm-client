package org.honton.chas.datadog.apm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Marker annotation to indicate method or type should be traced.
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceOperation {
  boolean value() default true;

  /**
   * Type of span to create.  Usually something like 'web' or 'db'
   * @return The type of span.
   */
  @Nonbinding
  String type() default "";

  /**
   * The predefined type for http/https spans
   */
  String WEB = "web";

  /**
   * The predefined type for datastore spans
   */
  String DB = "db";

  /**
   * The predefined type for unknown type spans
   */
  String UNKNOWN = "unknown";
}
