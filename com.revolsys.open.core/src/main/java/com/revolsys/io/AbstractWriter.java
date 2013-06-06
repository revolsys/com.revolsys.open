package com.revolsys.io;

public abstract class AbstractWriter<T> extends AbstractObjectWithProperties
  implements Writer<T> {

  public static <V> Writer<V> close(final Writer<V> writer) {
    if (writer != null) {
      writer.close();
    }
    return null;
  }

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }
}
