package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public class PointQuadTree<T> extends AbstractPointSpatialIndex<T> {

  private PointQuadTreeNode<T> root;

  public boolean contains(final Coordinates point) {
    if (root == null) {
      return false;
    } else {
      return root.contains(point);
    }
  }

  public List<T> findWithin(final Envelope envelope) {
    final List<T> results = new ArrayList<T>();
    if (root != null) {
      root.findWithin(results, envelope);
    }
    return results;
  }

  public List<T> findWithinDistance(final Coordinates point,
    final double maxDistance) {
    final double x = point.getX();
    final double y = point.getY();
    final Envelope envelope = new Envelope(x, x, y, y);
    envelope.expandBy(maxDistance);
    final List<T> results = new ArrayList<T>();
    if (root != null) {
      root.findWithin(results, x, y, maxDistance, envelope);
    }
    return results;
  }

  @Override
  public void put(final Coordinates point, final T value) {
    final double x = point.getX();
    final double y = point.getY();
    put(x, y, value);
  }

  public void put(final double x, final double y, final T value) {
    final PointQuadTreeNode<T> node = new PointQuadTreeNode<T>(value, x, y);
    if (root == null) {
      root = node;
    } else {
      root.put(x, y, node);
    }
  }

  @Override
  public boolean remove(final Coordinates point, final T value) {
    final double x = point.getX();
    final double y = point.getY();
    return remove(x, y, value);
  }

  public boolean remove(final double x, final double y, final T value) {
    if (root == null) {
      return false;
    } else {
      root = root.remove(x, y, value);
      // TODO change so it returns if the item was removed
      return true;
    }
  }

  @Override
  public void visit(final Envelope envelope, final Visitor<T> visitor) {
    if (root != null) {
      root.visit(envelope, visitor);
    }
  }

  @Override
  public void visit(final Visitor<T> visitor) {
    if (root != null) {
      root.visit(visitor);
    }
  }
}
