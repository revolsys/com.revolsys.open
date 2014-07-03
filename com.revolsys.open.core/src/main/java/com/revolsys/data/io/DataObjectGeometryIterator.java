package com.revolsys.data.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.data.record.Record;
import com.revolsys.io.DelegatingObjectWithProperties;
import com.revolsys.jts.geom.Geometry;

public class DataObjectGeometryIterator extends DelegatingObjectWithProperties
  implements Iterator<Geometry> {
  private Iterator<Record> iterator;

  public DataObjectGeometryIterator(final Iterator<Record> iterator) {
    super(iterator);
    this.iterator = iterator;
  }

  @Override
  public void close() {
    super.close();
    iterator = null;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Geometry next() {
    if (iterator.hasNext()) {
      final Record dataObject = iterator.next();
      return dataObject.getGeometryValue();
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    iterator.remove();
  }
}
