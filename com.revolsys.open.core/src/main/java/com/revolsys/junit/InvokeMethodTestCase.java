package com.revolsys.junit;

import junit.framework.TestCase;

import com.revolsys.util.Property;

public class InvokeMethodTestCase extends TestCase {

  private final Object object;

  private final String methodName;

  private final Object[] parameters;

  public InvokeMethodTestCase(final String name, final Object object,
    final String methodName, final Object... parameters) {
    super(name);
    this.object = object;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  @Override
  protected void runTest() throws Throwable {
    Property.invoke(object, methodName, parameters);
  }
}
