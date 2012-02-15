package com.revolsys.io;

import java.util.Map;

public abstract class AbstractMapWriter extends
  AbstractWriter<Map<String, ? extends Object>> implements MapWriter {

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }
}
