package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.DelegatingObjectWithProperties;
import com.revolsys.jts.geom.Geometry;

public class DataObjectGeometryIterator extends DelegatingObjectWithProperties
  implements Iterator<Geometry> {
  private Iterator<DataObject> iterator;

  public DataObjectGeometryIterator(final Iterator<DataObject> iterator) {
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
      final DataObject dataObject = iterator.next();
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
