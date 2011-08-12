package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.DelegatingIterator;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class DelegatingReader<T> extends AbstractReader<T> {
  private Reader<T> reader;

  private List<T> objects = new ArrayList<T>();

  private Iterator<T> iterator;

  public DelegatingReader() {
  }

  public DelegatingReader(final Reader<T> reader) {
    this.reader = reader;
  }

  public final void close() {
    try {
      if (reader != null) {
        reader.close();
      }
    } finally {
      doClose();
    }
  }

  protected void doClose() {
    if (iterator instanceof AbstractIterator) {
      final AbstractIterator<T> iter = (AbstractIterator<T>)iterator;
      iter.close();
    }
  }

  public List<T> getObjects() {
    return objects;
  }

  @Override
  public Map<String, Object> getProperties() {
    return reader.getProperties();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> C getProperty(final String name) {
    return (C)reader.getProperty(name);
  }

  public Reader<T> getReader() {
    return reader;
  }

  public Iterator<T> iterator() {
    if (iterator == null) {
      iterator = reader.iterator();
      if (objects != null && !objects.isEmpty()) {
        iterator = new DelegatingIterator<T>(iterator, objects);
      }
    }
    return iterator;
  }

  public void open() {
    reader.open();
  }

  public void setObjects(final Collection<T> objects) {
    this.objects = new ArrayList<T>(objects);
  }

  public void setObjects(final List<T> objects) {
    this.objects = new ArrayList<T>(objects);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    reader.setProperty(name, value);
  }

  public void setReader(final Reader<T> reader) {
    this.reader = reader;
  }
}
