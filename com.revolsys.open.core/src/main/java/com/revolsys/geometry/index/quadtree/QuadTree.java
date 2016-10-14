package com.revolsys.geometry.index.quadtree;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.ExitLoopException;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.SingleObjectVisitor;

public class QuadTree<T> implements SpatialIndex<T>, Serializable {
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

  private double minExtent = 1.0;

  private AbstractNode<T> root;

  private int size = 0;

  private boolean useEquals = false;

  public QuadTree() {
    this.root = new QuadTreeNode<>();
  }

  protected QuadTree(final AbstractNode<T> root) {
    this.root = root;
  }

  public QuadTree(final GeometryFactory geometryFactory) {
    this();
    this.geometryFactory = geometryFactory;
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
    return boundingBox.getMinMaxValues(2);
  }

  public int depth() {
    return this.root.depth();
  }

  protected boolean equalsItem(final T item1, final T item2) {
    if (item1 == item2) {
      return true;
    } else if (this.useEquals) {
      return item1.equals(item2);
    } else {
      return false;
    }
  }

  @Override
  public void forEach(final BoundingBox boundingBox, final Consumer<? super T> action) {
    final double[] bounds = convert(boundingBox);
    this.root.forEach(this, bounds, action);
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    try {
      this.root.forEach(this, action);
    } catch (final ExitLoopException e) {
    }
  }

  @Override
  public void forEach(final double x, final double y, final Consumer<? super T> action) {
    final double[] bounds = new double[] {
      x, y, x, y
    };
    this.root.forEach(this, bounds, action);
  }

  public List<T> getAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(visitor);
    return visitor.getList();
  }

  public T getFirst(final BoundingBox boundingBox, final Predicate<T> filter) {
    final SingleObjectVisitor<T> visitor = new SingleObjectVisitor<>(filter);
    forEach(boundingBox, visitor);
    return visitor.getObject();
  }

  public T getFirstBoundingBox(final Geometry geometry, final Predicate<T> filter) {
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

  @Override
  public List<T> getItems(final BoundingBox boundingBox) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(boundingBox, visitor);
    return visitor.getList();
  }

  @Override
  public int getSize() {
    return this.size;
  }

  @Override
  public void insertItem(final BoundingBox boundingBox, final T item) {
    if (boundingBox == null) {
      throw new IllegalArgumentException("Item envelope must not be null");
    } else {
      double[] bounds = convert(boundingBox);
      if (bounds != null) {
        this.size++;
        collectStats(boundingBox);
        bounds = ensureExtent(bounds, this.minExtent);
        final double minX = bounds[0];
        final double minY = bounds[1];
        final double maxX = bounds[2];
        final double maxY = bounds[3];
        this.root.insertRoot(this, minX, minY, maxX, maxY, item);
      }
    }
  }

  public List<T> query(final BoundingBox boundingBox, final Predicate<T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>(filter);
    forEach(boundingBox, visitor);
    return visitor.getList();
  }

  public List<T> queryBoundingBox(final Geometry geometry) {
    if (geometry == null) {
      return Collections.emptyList();
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return getItems(boundingBox);
    }
  }

  @Override
  public boolean removeItem(final BoundingBox boundingBox, final T item) {
    double[] bounds = convert(boundingBox);
    if (bounds != null) {
      bounds = ensureExtent(bounds, this.minExtent);
      final double minX = bounds[0];
      final double minY = bounds[1];
      final double maxX = bounds[2];
      final double maxY = bounds[3];

      final boolean removed = this.root.removeItem(this, minX, minY, maxX, maxY, item);
      if (removed) {
        this.size--;
      }
      return removed;
    } else {
      return false;
    }
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setUseEquals(final boolean useEquals) {
    this.useEquals = useEquals;
  }

  public int size() {
    return getSize();
  }
}
