package com.revolsys.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.revolsys.io.BaseCloseable;

public class CloseableValueHolder<R> extends ValueHolder<R> implements BaseCloseable {

  private int referenceCount = 1;

  private Supplier<R> resourceFactory;

  private final Consumer<R> resourceCloseFunction;

  public CloseableValueHolder(final Supplier<R> resourceFactory,
    final Consumer<R> resourceCloseFunction) {
    this.resourceFactory = resourceFactory;
    this.resourceCloseFunction = resourceCloseFunction;
  }

  @Override
  public synchronized void close() {
    final R value = this.value;
    this.resourceFactory = null;
    this.value = null;
    if (value != null) {
      this.resourceCloseFunction.accept(value);
    }
  }

  public synchronized BaseCloseable connect() {
    if (this.resourceFactory == null) {
      throw new IllegalStateException("Resource closed");
    } else {
      this.referenceCount++;
      return () -> disconnect();
    }
  }

  void disconnect() {
    final R resourceToClose;
    synchronized (this) {
      this.referenceCount--;
      if (this.referenceCount <= 0) {
        resourceToClose = this.value;
        this.value = null;
        this.referenceCount = 0;
      } else {
        resourceToClose = null;
      }
    }
    if (resourceToClose != null) {
      this.resourceCloseFunction.accept(resourceToClose);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  protected synchronized R getValue() {
    this.referenceCount++;
    if (this.resourceFactory == null) {
      throw new IllegalStateException("Resource closed");
    } else {
      if (this.value == null) {
        final R resource = this.resourceFactory.get();
        if (resource == null) {
          return null;
        } else {
          this.value = resource;
        }
      }
      return this.value;
    }
  }

  @Override
  public void valueConsume(final Consumer<R> action) {
    try {
      super.valueConsume(action);
    } finally {
      disconnect();
    }
  }

  @Override
  public <V> V valueFunction(final Function<R, V> action) {
    try {
      return super.valueFunction(action);
    } finally {
      disconnect();
    }
  }

  @Override
  public <V> V valueFunction(final Function<R, V> action, final V defaultValue) {
    try {
      return super.valueFunction(action, defaultValue);
    } finally {
      disconnect();
    }
  }

}
