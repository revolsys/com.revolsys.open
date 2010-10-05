package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.Map;

import com.revolsys.io.ObjectWithProperties;

public class IteratorReader<T> extends AbstractReader<T> {

  private Iterator<T> iterator;

  private ObjectWithProperties object;

  public IteratorReader(
    final Iterator<T> iterator) {
    this.iterator = iterator;
    if (iterator instanceof ObjectWithProperties) {
      object = (ObjectWithProperties)iterator;
    }
  }

  public void close() {
    iterator = null;
  }

  public Iterator<T> iterator() {
    return iterator;
  }

  @Override
  public void setProperty(
    String name,
    Object value) {
    if (object == null) {
      super.setProperty(name, value);
    } else {
      object.setProperty(name, value);
    }
  }

  @Override
  public Map<String, Object> getProperties() {
    if (object == null) {
      return super.getProperties();
    } else {
      return object.getProperties();
    }
  }

  @Override
  public <C> C getProperty(
    String name) {
    if (object == null) {
      return (C)super.getProperty(name);
    } else {
      return (C)object.getProperty(name);
    }
  }
}
