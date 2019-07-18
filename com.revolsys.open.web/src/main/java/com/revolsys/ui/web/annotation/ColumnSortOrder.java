package com.revolsys.ui.web.annotation;

public @interface ColumnSortOrder {
  public boolean ascending() default true;

  public String value() default "";
}
