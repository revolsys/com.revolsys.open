package com.revolsys.gis.algorithm.index;

import java.util.LinkedList;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;

public abstract class RTreeNode<T> extends Envelope {

  /**
   * 
   */
  private static final long serialVersionUID = -8110404083135361671L;

  public RTreeNode() {
  }

  public abstract boolean remove(LinkedList<RTreeNode<T>> path,
    BoundingBox envelope, T object);

  @Override
  public String toString() {
    return toPolygon(1).toString();
  }

  protected abstract void updateEnvelope();

  public abstract boolean visit(BoundingBox envelope, Filter<T> filter,
    Visitor<T> visitor);

  public abstract boolean visit(BoundingBox envelope, Visitor<T> visitor);

  public abstract boolean visit(Visitor<T> visitor);
}
