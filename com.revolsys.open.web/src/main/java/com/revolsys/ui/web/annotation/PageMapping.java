package com.revolsys.ui.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({
  ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageMapping {
  String[] fieldNames() default {};

  String name() default "";

  boolean secure() default false;

  String title() default "";
}
