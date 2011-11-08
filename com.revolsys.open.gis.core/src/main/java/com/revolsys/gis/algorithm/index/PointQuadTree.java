package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Envelope;

public class PointQuadTree<T> {

  private PointQuadTreeNode<T> root;

  public List<T> findWithin(final Coordinates point, final double distance) {
    final double x = point.getX();
    final double y = point.getY();
    final Envelope envelope = new Envelope(x, x, y, y);
    final List<T> results = new ArrayList<T>();
    root.findWithin(results, x, y, distance, envelope);
    return results;
  }

  public List<T> findWithin(final Envelope envelope) {
    final List<T> results = new ArrayList<T>();
    root.findWithin(results, envelope);
    return results;
  }

  public void insert(final T value, final Coordinates point) {
    final double x = point.getX();
    final double y = point.getY();
    insert(value, x, y);
  }

  public void insert(final T value, final double x, final double y) {
    final PointQuadTreeNode<T> node = new PointQuadTreeNode<T>(value, x, y);
    if (root == null) {
      root = node;
    } else {
      root.insert(node, x, y);
    }
  }

  public void remove(final T value, final Coordinates point) {
    final double x = point.getX();
    final double y = point.getY();
    remove(value, x, y);
  }

  public void remove(final T value, final double x, final double y) {
    if (root != null) {
      root = root.remove(value, x, y);
    }
  }

}
