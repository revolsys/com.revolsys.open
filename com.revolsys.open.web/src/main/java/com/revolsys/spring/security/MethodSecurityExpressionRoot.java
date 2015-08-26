package com.revolsys.spring.security;

import org.springframework.security.core.Authentication;

/**
 * Extended expression root object which contains extra method-specific
 * functionality.
 *
 * @author Luke Taylor
 * @since 3.0
 */
public class MethodSecurityExpressionRoot extends SecurityExpressionRoot {
  public final String admin = "administration";

  public final String create = "create";

  public final String delete = "delete";

  private Object filterObject;

  public final String read = "read";

  private Object returnObject;

  public final String write = "write";

  public MethodSecurityExpressionRoot(final Authentication a) {
    super(a);
  }

  public Object getFilterObject() {
    return this.filterObject;
  }

  public Object getReturnObject() {
    return this.returnObject;
  }

  public void setFilterObject(final Object filterObject) {
    this.filterObject = filterObject;
  }

  public void setReturnObject(final Object returnObject) {
    this.returnObject = returnObject;
  }
}
