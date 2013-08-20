package com.revolsys.gis.algorithm.index.quadtree;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.filter.InvokeMethodFilter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.SingleObjectVisitor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class QuadTree<T> {
  public static Envelope ensureExtent(final Envelope envelope,
    final double minExtent) {
    double minX = envelope.getMinX();
    double maxX = envelope.getMaxX();
    double minY = envelope.getMinY();
    double maxY = envelope.getMaxY();
    if (minX != maxX && minY != maxY) {
      return envelope;
    }

    if (minX == maxX) {
      minX = minX - minExtent / 2.0;
      maxX = minX + minExtent / 2.0;
    }
    if (minY == maxY) {
      minY = minY - minExtent / 2.0;
      maxY = minY + minExtent / 2.0;
    }
    return new Envelope(minX, maxX, minY, maxY);
  }

  private GeometryFactory geometryFactory;

  private final Root<T> root = new Root<T>();;

  private double minExtent = 1.0;

  private int size = 0;

  public QuadTree() {
  }

  public QuadTree(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  private void collectStats(final Envelope envelope) {
    final double delX = envelope.getWidth();
    if (delX < minExtent && delX > 0.0) {
      minExtent = delX;
    }

    final double delY = envelope.getHeight();
    if (delY < minExtent && delY > 0.0) {
      minExtent = delY;
    }
  }

  public int depth() {
    if (root != null) {
      return root.depth();
    }
    return 0;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public int getSize() {
    return size;
  }

  public void insert(BoundingBox boundingBox, final T item) {
    if (boundingBox == null) {
      throw new IllegalArgumentException("Item envelope must not be null");
    } else {
      if (geometryFactory != null) {
        boundingBox = boundingBox.convert(geometryFactory);
      }
      size++;
      collectStats(boundingBox);
      final Envelope insertEnv = ensureExtent(boundingBox, minExtent);
      root.insert(insertEnv, item);
    }
  }

  public List<T> query(final BoundingBox boundingBox) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    query(boundingBox, visitor);
    return visitor.getList();
  }

  public List<T> query(final BoundingBox boundingBox, final Filter<T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>(filter);
    query(boundingBox, visitor);
    return visitor.getList();
  }

  public List<T> query(final BoundingBox boundingBox, final String methodName,
    final Object... parameters) {
    final InvokeMethodFilter<T> filter = new InvokeMethodFilter<T>(methodName,
      parameters);
    return query(boundingBox, filter);
  }

  public void query(BoundingBox boundingBox, final Visitor<T> visitor) {
    boundingBox = boundingBox.convert(geometryFactory);
    root.visit(boundingBox, visitor);
  }

  public List<T> query(final Envelope envelope) {
    return query(new BoundingBox(envelope));
  }

  public void query(final Envelope envelope, final Visitor<T> visitor) {
    query(new BoundingBox(envelope), visitor);
  }

  public List<T> queryAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    root.visit(visitor);
    return visitor.getList();
  }

  public List<T> queryEnvelope(final BoundingBox boundingBox) {
    return query(boundingBox);
  }

  public List<T> queryEnvelope(final Geometry geometry) {
    final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
    return query(boundingBox);
  }

  public T queryFirst(final BoundingBox boundingBox, final Filter<T> filter) {
    final SingleObjectVisitor<T> visitor = new SingleObjectVisitor<T>(filter);
    query(boundingBox, visitor);
    return visitor.getObject();
  }

  public T queryFirst(final Geometry geometry, final Filter<T> filter) {
    final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
    return queryFirst(boundingBox, filter);
  }

  public boolean remove(final Envelope envelope, final T item) {
    final Envelope posEnv = ensureExtent(envelope, minExtent);
    final boolean removed = root.remove(posEnv, item);
    if (removed) {
      size--;
    }
    return removed;
  }

  public int size() {
    if (root != null) {
      return root.size();
    }
    return 0;
  }

}
