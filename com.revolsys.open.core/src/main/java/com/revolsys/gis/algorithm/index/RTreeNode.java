package com.revolsys.gis.algorithm.index;

import java.util.LinkedList;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.jts.geom.Envelope;

public abstract class RTreeNode<T> extends Envelope {

  /**
   * 
   */
  private static final long serialVersionUID = -8110404083135361671L;

  public RTreeNode() {
  }

  public abstract boolean remove(LinkedList<RTreeNode<T>> path,
    Envelope envelope, T object);

  @Override
  public String toString() {
    return new BoundingBox(GeometryFactory.getFactory(), this).toPolygon(1)
      .toString();
  }

  protected abstract void updateEnvelope();

  public abstract boolean visit(Envelope envelope, Filter<T> filter,
    Visitor<T> visitor);

  public abstract boolean visit(Envelope envelope, Visitor<T> visitor);

  public abstract boolean visit(Visitor<T> visitor);
}
