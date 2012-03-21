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
  private Object filterObject;

  private Object returnObject;

  public final String read = "read";

  public final String write = "write";

  public final String create = "create";

  public final String delete = "delete";

  public final String admin = "administration";

  public MethodSecurityExpressionRoot(final Authentication a) {
    super(a);
  }

  public Object getFilterObject() {
    return filterObject;
  }

  public Object getReturnObject() {
    return returnObject;
  }

  public void setFilterObject(final Object filterObject) {
    this.filterObject = filterObject;
  }

  public void setReturnObject(final Object returnObject) {
    this.returnObject = returnObject;
  }
}
