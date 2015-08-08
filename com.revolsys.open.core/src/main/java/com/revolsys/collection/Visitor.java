package com.revolsys.collection;

import java.util.function.Consumer;

public interface Visitor<T> extends Consumer<T> {
  @Override
  default void accept(final T value) {
    visit(value);
  }

  /**
   * Visit an value of type T, performing some operation on the value. The method
   * must return true if further values are to be processed or false if no
   * further values are to be processed.
   *
   * @param value The value to process.
   * @return True if further items are to be processed, false otherwise.
   */
  default boolean visit(final T value) {
    accept(value);
    return true;
  }
}
