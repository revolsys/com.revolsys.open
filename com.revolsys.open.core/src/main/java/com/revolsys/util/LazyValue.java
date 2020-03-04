package com.revolsys.util;

import java.util.function.Supplier;

public class LazyValue<V> extends ValueHolder<V> {

  public static <T> LazyValue<T> newValue(final Supplier<T> supplier) {
    return new LazyValue<>(supplier);
  }

  private Supplier<V> supplier;

  public LazyValue(final Supplier<V> supplier) {
    this.supplier = supplier;
  }

  @Override
  public synchronized void close() {
    super.close();
    this.supplier = null;
  }

  @Override
  public synchronized V getValue() {
    if (this.value == null && this.supplier != null) {
      this.value = this.supplier.get();
    }
    return this.value;
  }

}
