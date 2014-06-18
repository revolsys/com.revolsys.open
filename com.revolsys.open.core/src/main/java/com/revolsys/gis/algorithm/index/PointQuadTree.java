package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

public class PointQuadTree<T> extends AbstractPointSpatialIndex<T> {

  private PointQuadTreeNode<T> root;

  private GeometryFactory geometryFactory;

  public PointQuadTree() {
  }

  public PointQuadTree(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public boolean contains(final Point point) {
    if (root == null) {
      return false;
    } else {
      return root.contains(point);
    }
  }

  public List<Entry<Point, T>> findEntriesWithinDistance(final Point from,
    final Point to, final double maxDistance) {
    final BoundingBoxDoubleGf boundingBox = new BoundingBoxDoubleGf(geometryFactory, from, to);
    final List<Entry<Point, T>> entries = new ArrayList<Entry<Point, T>>();
    root.findEntriesWithin(entries, boundingBox);
    for (final Iterator<Entry<Point, T>> iterator = entries.iterator(); iterator.hasNext();) {
      final Entry<Point, T> entry = iterator.next();
      final Point coordinates = entry.getKey();
      final double distance = LineSegmentUtil.distanceLinePoint(from, to,
        coordinates);
      if (distance >= maxDistance) {
        iterator.remove();
      }
    }
    return entries;
  }

  public List<T> findWithin(BoundingBox boundingBox) {
    if (geometryFactory != null) {
      boundingBox = boundingBox.convert(geometryFactory);
    }
    final List<T> results = new ArrayList<T>();
    if (root != null) {
      root.findWithin(results, boundingBox);
    }
    return results;
  }

  public List<T> findWithinDistance(final Point point, final double maxDistance) {
    final double x = point.getX();
    final double y = point.getY();
    BoundingBox envelope = new BoundingBoxDoubleGf(2, x, y);
    envelope = envelope.expand(maxDistance);
    final List<T> results = new ArrayList<T>();
    if (root != null) {
      root.findWithin(results, x, y, maxDistance, envelope);
    }
    return results;
  }

  public List<T> findWithinDistance(final Point from, final Point to,
    final double maxDistance) {
    final List<Entry<Point, T>> entries = findEntriesWithinDistance(from, to,
      maxDistance);
    final List<T> results = new ArrayList<T>();
    for (final Entry<Point, T> entry : entries) {
      final T value = entry.getValue();
      results.add(value);
    }
    return results;
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
  public void put(final Point point, final T value) {
    if (!point.isEmpty()) {
      final double x = point.getX();
      final double y = point.getY();
      put(x, y, value);
    }
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
  public boolean remove(final Point point, final T value) {
    final double x = point.getX();
    final double y = point.getY();
    return remove(x, y, value);
  }

  @Override
  public void visit(final com.revolsys.jts.geom.BoundingBox envelope,
    final Visitor<T> visitor) {
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
