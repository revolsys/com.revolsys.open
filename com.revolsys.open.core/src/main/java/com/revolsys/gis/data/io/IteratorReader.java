package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.Map;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.ObjectWithProperties;

public class IteratorReader<T> extends AbstractReader<T> {

  private Iterator<T> iterator;

  private ObjectWithProperties object;

  public IteratorReader() {
  }

  public IteratorReader(final Iterator<T> iterator) {
    this.iterator = iterator;
    if (iterator instanceof ObjectWithProperties) {
      object = (ObjectWithProperties)iterator;
    }
  }

  @Override
  public void close() {
    try {
      if (iterator instanceof AbstractIterator) {
        final AbstractIterator<T> i = (AbstractIterator<T>)iterator;
        i.close();
      }
    } finally {
      iterator = null;
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

  @SuppressWarnings("unchecked")
  @Override
  public <C> C getProperty(final String name) {
    if (object == null) {
      return (C)super.getProperty(name);
    } else {
      return (C)object.getProperty(name);
    }
  }

  @Override
  public Iterator<T> iterator() {
    return iterator;
  }

  @Override
  public void open() {
    iterator.hasNext();
  }

  protected void setIterator(final Iterator<T> iterator) {
    this.iterator = iterator;
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if (object == null) {
      super.setProperty(name, value);
    } else {
      object.setProperty(name, value);
    }
  }
}
