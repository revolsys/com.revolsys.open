package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.DelegatingObjectWithProperties;
import com.revolsys.io.ObjectWithProperties;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectGeometryIterator extends DelegatingObjectWithProperties
  implements Iterator<Geometry> {
  private Iterator<DataObject> iterator;

  public void close() {
    super.close();
    iterator = null;
  }

  public DataObjectGeometryIterator(
    Iterator<DataObject> iterator) {
    super(iterator);
    this.iterator = iterator;
  }

  public boolean hasNext() {
    return iterator.hasNext();
  }

  public Geometry next() {
    if (iterator.hasNext()) {
      final DataObject dataObject = iterator.next();
      return dataObject.getGeometryValue();
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    iterator.remove();
  }
}
