package com.revolsys.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class ValueHolder<R> {

  protected R value;

  public ValueHolder() {
    super();
  }

  protected synchronized R getValue() {
    return this.value;
  }

  public void valueConsume(final Consumer<R> action) {
    final R resource = getValue();
    if (resource != null) {
      action.accept(resource);
    }
  }

  public <V> V valueFunction(final Function<R, V> action) {
    final R resource = getValue();
    if (resource == null) {
      return null;
    } else {
      return action.apply(resource);
    }
  }

  public <V> V valueFunction(final Function<R, V> action, final V defaultValue) {
    final R resource = getValue();
    if (resource == null) {
      return defaultValue;
    } else {
      return action.apply(resource);
    }
  }

}
