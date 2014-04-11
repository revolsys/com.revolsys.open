package com.revolsys.gis.algorithm.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;

public class PointQuadTree<T> extends AbstractPointSpatialIndex<T> {

  private PointQuadTreeNode<T> root;

  private GeometryFactory geometryFactory;

  public PointQuadTree() {
  }

  public PointQuadTree(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public boolean contains(final Coordinates point) {
    if (root == null) {
      return false;
    } else {
      return root.contains(point);
    }
  }

  public List<Entry<Coordinates, T>> findEntriesWithinDistance(
    final Coordinates from, final Coordinates to, final double maxDistance) {
    final BoundingBox boundingBox = new BoundingBox(geometryFactory, from, to);
    final List<Entry<Coordinates, T>> entries = new ArrayList<Entry<Coordinates, T>>();
    root.findEntriesWithin(entries, boundingBox);
    for (final Iterator<Entry<Coordinates, T>> iterator = entries.iterator(); iterator.hasNext();) {
      final Entry<Coordinates, T> entry = iterator.next();
      final Coordinates coordinates = entry.getKey();
      final double distance = LineSegmentUtil.distance(from, to, coordinates);
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
    return findWithin((Envelope)boundingBox);
  }

  public List<T> findWithin(final Envelope envelope) {
    final List<T> results = new ArrayList<T>();
    if (root != null) {
      root.findWithin(results, envelope);
    }
    return results;
  }

  public List<T> findWithinDistance(final Coordinates from,
    final Coordinates to, final double maxDistance) {
    final List<Entry<Coordinates, T>> entries = findEntriesWithinDistance(from,
      to, maxDistance);
    final List<T> results = new ArrayList<T>();
    for (final Entry<Coordinates, T> entry : entries) {
      final T value = entry.getValue();
      results.add(value);
    }
    return results;
  }

  public List<T> findWithinDistance(final Coordinates point,
    final double maxDistance) {
    final double x = point.getX();
    final double y = point.getY();
    BoundingBox envelope = new BoundingBox(x, y);
    envelope = envelope.expand(maxDistance);
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
