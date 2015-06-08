package com.revolsys.gis.algorithm.index.quadtree;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.filter.InvokeMethodFilter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.index.SpatialIndex;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.SingleObjectVisitor;

public class QuadTree<T> implements SpatialIndex<T>, Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static double[] ensureExtent(final double[] bounds, final double minExtent) {
    double minX = bounds[0];
    double maxX = bounds[2];
    double minY = bounds[1];
    double maxY = bounds[3];
    if (minX != maxX && minY != maxY) {
      return bounds;
    } else {
      if (minX == maxX) {
        minX = minX - minExtent / 2.0;
        maxX = minX + minExtent / 2.0;
      }
      if (minY == maxY) {
        minY = minY - minExtent / 2.0;
        maxY = minY + minExtent / 2.0;
      }
      return new double[] {
        minX, minY, maxX, maxY
      };
    }
  }

  private GeometryFactory geometryFactory;

  private AbstractNode<T> root;

  private double minExtent = 1.0;

  private int size = 0;

  public QuadTree() {
    this.root = new Node<T>();
  }

  public QuadTree(final GeometryFactory geometryFactory) {
    this();
    this.geometryFactory = geometryFactory;
  }

  protected QuadTree(final IdObjectNode<T> root) {
    this.root = root;
  }

  public void clear() {
    this.root.clear();
    this.minExtent = 1.0;
    this.size = 0;
  }

  private void collectStats(final BoundingBox envelope) {
    final double delX = envelope.getWidth();
    if (delX < this.minExtent && delX > 0.0) {
      this.minExtent = delX;
    }

    final double delY = envelope.getHeight();
    if (delY < this.minExtent && delY > 0.0) {
      this.minExtent = delY;
    }
  }

  protected double[] convert(BoundingBox boundingBox) {
    if (this.geometryFactory != null) {
      boundingBox = boundingBox.convert(this.geometryFactory);
    }
    return boundingBox.getBounds(2);
  }

  public int depth() {
    return this.root.depth();
  }

  public List<T> getAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    this.root.visit(this, visitor);
    return visitor.getList();
  }

  public T getFirst(final BoundingBox boundingBox, final Filter<T> filter) {
    final SingleObjectVisitor<T> visitor = new SingleObjectVisitor<T>(filter);
    visit(boundingBox, visitor);
    return visitor.getObject();
  }

  public T getFirstBoundingBox(final Geometry geometry, final Filter<T> filter) {
    if (geometry == null) {
      return null;
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return getFirst(boundingBox, filter);
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public int getSize() {
    return this.size;
  }

  @Override
  public void insert(final BoundingBox boundingBox, final T item) {
    if (boundingBox == null) {
      throw new IllegalArgumentException("Item envelope must not be null");
    } else {
      double[] bounds = convert(boundingBox);
      if (bounds != null) {
        this.size++;
        collectStats(boundingBox);
        bounds = ensureExtent(bounds, this.minExtent);
        this.root.insertRoot(this, bounds, item);
      }
    }
  }

  @Override
  public List<T> query(final BoundingBox boundingBox) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(boundingBox, visitor);
    return visitor.getList();
  }

  public List<T> query(final BoundingBox boundingBox, final Filter<T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>(filter);
    visit(boundingBox, visitor);
    return visitor.getList();
  }

  public List<T> query(final BoundingBox boundingBox, final String methodName,
    final Object... parameters) {
    final InvokeMethodFilter<T> filter = new InvokeMethodFilter<T>(methodName, parameters);
    return query(boundingBox, filter);
  }

  public List<T> queryBoundingBox(final Geometry geometry) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return query(boundingBox);
    }
  }

  @Override
  public boolean remove(final BoundingBox boundingBox, final T item) {
    double[] bounds = convert(boundingBox);
    bounds = ensureExtent(bounds, this.minExtent);
    final boolean removed = this.root.remove(this, bounds, item);
    if (removed) {
      this.size--;
    }
    return removed;
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public int size() {
    return getSize();
  }

  public void visit(final BoundingBox boundingBox, final Visitor<T> visitor) {
    final double[] bounds = convert(boundingBox);
    this.root.visit(this, bounds, visitor);
  }

  public void visitAll(final Visitor<T> visitor) {
    this.root.visit(this, visitor);
  }
}
