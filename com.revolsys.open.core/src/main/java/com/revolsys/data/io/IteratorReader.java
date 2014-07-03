package com.revolsys.data.io;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.ObjectWithProperties;

public class IteratorReader<T> extends AbstractReader<T> {

  private Iterator<T> iterator;

  private ObjectWithProperties object;

  public IteratorReader() {
    setIterator(null);
  }

  public IteratorReader(final Iterator<T> iterator) {
    setIterator(iterator);
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
      setIterator(null);
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
    if (iterator == null) {
      this.iterator = Collections.<T> emptyList().iterator();
    } else {
      this.iterator = iterator;
    }
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
