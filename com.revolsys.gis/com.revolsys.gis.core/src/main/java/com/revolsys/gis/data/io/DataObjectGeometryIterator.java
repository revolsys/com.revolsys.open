package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.gis.data.model.DataObject;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectGeometryIterator implements Iterator<Geometry> {
  private Iterator<DataObject> iterator;

  public DataObjectGeometryIterator(
    Iterator<DataObject> iterator) {
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
