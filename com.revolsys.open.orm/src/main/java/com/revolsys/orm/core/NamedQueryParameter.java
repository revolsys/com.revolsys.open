package com.revolsys.orm.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NamedQueryParameter {
  String value();
}
