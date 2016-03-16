package com.revolsys.ui.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.RequestMethod;

@Target({
  ElementType.METHOD
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
  public ColumnSortOrder[] columnSortOrder() default {};

  String[] fieldNames() default {};

  RequestMethod[] method() default {};

  String name() default "";

  String permission() default "";

  boolean secure() default false;

  String title() default "";

  String[] value() default {};
}
