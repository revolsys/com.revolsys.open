package com.revolsys.gis.algorithm.index;

import java.util.Collection;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public abstract class AbstractIdObjectPointQuadTree<T> extends
  AbstractPointSpatialIndex<T> implements IdObjectIndex<T> {

  private PointSpatialIndex<Integer> index = new PointQuadTree<Integer>();

  public void add(final Collection<Integer> ids) {
    for (final Integer id : ids) {
      final T object = getObject(id);
      add(object);
    }
  }

  public T add(final T object) {
    final Coordinates point = getCoordinates(object);
    put(point, object);
    return object;
  }

  public void put(final Coordinates point, final T object) {
    final int id = getId(object);
    index.put(point, id);
  }

  public abstract Coordinates getCoordinates(T object);

  public void visit(final Envelope envelope, final Visitor<T> visitor) {
    final IdObjectIndexEnvelopeVisitor<T> itemVisitor = new IdObjectIndexEnvelopeVisitor<T>(
      this, envelope, visitor);
    index.visit(envelope, itemVisitor);
  }

  public void visit(final Visitor<T> visitor) {
    final IdObjectIndexVisitor<T> itemVisitor = new IdObjectIndexVisitor<T>(
      this, visitor);
    index.visit(itemVisitor);
  }

  public boolean remove(final T object) {
    final Coordinates point = getCoordinates(object);
    return remove(point, object);
  }

  public boolean remove(final Coordinates point, final T object) {
    final int id = getId(object);
    return index.remove(point, id);
  }

  public void removeAll(final Collection<T> objects) {
    for (final T object : objects) {
      remove(object);
    }
  }

  
}
