package com.revolsys.junit;

import com.revolsys.util.Debug;

import junit.framework.TestCase;

public class RunnableTestCase extends TestCase {
  private final Runnable action;

  public RunnableTestCase(final String name, final Runnable action) {
    super(name);

    this.action = action;
  }

  @Override
  protected void runTest() {
    if (getName().startsWith("XBase Point")) {
      Debug.noOp();
    }
    this.action.run();
  }
}
