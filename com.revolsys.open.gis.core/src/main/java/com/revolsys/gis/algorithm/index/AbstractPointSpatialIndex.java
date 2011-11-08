package com.revolsys.gis.algorithm.index;

import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.vividsolutions.jts.geom.Envelope;

public abstract class AbstractPointSpatialIndex<T> implements PointSpatialIndex<T> {

  public List<T> find(Envelope envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(envelope, visitor);
    return visitor.getList();
  }

  public List<T> findAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(visitor);
    return visitor.getList();
  }
  

  public Iterator<T> iterator() {
    return findAll().iterator();
  }

}
