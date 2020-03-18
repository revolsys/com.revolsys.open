package com.revolsys.testapi;

import java.util.function.Consumer;

import junit.framework.TestCase;

public class ConsumerTestCase<T> extends TestCase {

  private final Consumer<T> action;

  private final T value;

  public ConsumerTestCase(final String name, final Consumer<T> action, final T value) {
    super(name);
    this.action = action;
    this.value = value;
  }

  @Override
  protected void runTest() {
    this.action.accept(this.value);
  }
}
