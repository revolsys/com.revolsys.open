package com.revolsys.ui.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.ValueConstants;

@Target({
  ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestAttribute {
  String defaultValue() default ValueConstants.DEFAULT_NONE;

  boolean required() default true;

  String value() default "";
}
