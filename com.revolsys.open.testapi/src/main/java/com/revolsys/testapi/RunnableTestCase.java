package com.revolsys.testapi;

import junit.framework.TestCase;

public class RunnableTestCase extends TestCase {
  private final Runnable action;

  public RunnableTestCase(final String name, final Runnable action) {
    super(name);

    this.action = action;
  }

  @Override
  protected void runTest() {
    this.action.run();
  }
}
