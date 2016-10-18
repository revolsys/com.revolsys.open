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

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT;

  private double minExtent;

  private final double absoluteMinExtent;

  private double minExtentTimes2;

  private AbstractQuadTreeNode<T> root;

  private int size = 0;

  private boolean useEquals = false;

  public QuadTree(final GeometryFactory geometryFactory) {
    this(geometryFactory, new QuadTreeNode<>());
  }

  protected QuadTree(final GeometryFactory geometryFactory, final AbstractQuadTreeNode<T> root) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT;
    } else {
      this.geometryFactory = geometryFactory;
    }
    if (geometryFactory.isFloating()) {
      this.absoluteMinExtent = 0.00000001;
    } else {
      this.absoluteMinExtent = geometryFactory.getResolutionXy();
    }
    if (this.absoluteMinExtent < 0.5) {
      this.minExtent = this.absoluteMinExtent;
    } else {
      this.minExtent = 0.5;
    }
    this.minExtentTimes2 = this.minExtent * 2;
    this.root = root;
  }

  public void clear() {
    this.root.clear();
    this.size = 0;
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
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    this.root.forEach(this, minX, minY, maxX, maxY, action);
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
    this.root.forEach(this, x, y, action);
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
    if (boundingBox == null || boundingBox.isEmpty()) {
      throw new IllegalArgumentException("Item envelope must not be null or empty");
    } else {
      final double[] bounds = convert(boundingBox);
      if (bounds != null) {

        final double minX = bounds[0];
        final double minY = bounds[1];
        final double maxX = bounds[2];
        final double maxY = bounds[3];

        insertItem(minX, minY, maxX, maxY, item);
      }
    }
  }

  public void insertItem(double minX, double minY, double maxX, double maxY, final T item) {
    final double delX = maxX - minX;
    if (delX < this.minExtent) {
      this.minExtent = this.geometryFactory.makeXyPrecise(delX);
      if (this.minExtent < this.absoluteMinExtent) {
        this.minExtent = this.absoluteMinExtent;
        this.minExtentTimes2 = this.minExtent * 2;
      }
    }
    final double delY = maxY - minY;
    if (delY < this.minExtent) {
      this.minExtent = this.geometryFactory.makeXyPrecise(delY);
      if (this.minExtent < this.absoluteMinExtent) {
        this.minExtent = this.absoluteMinExtent;
        this.minExtentTimes2 = this.minExtent * 2;
      }
    }

    if (delX < this.minExtentTimes2) {
      minX -= this.minExtent;
      maxX += this.minExtent;
    }

    if (delY < this.minExtentTimes2) {
      minY -= this.minExtent;
      maxY += this.minExtent;
    }

    if (this.root.insertRoot(this, minX, minY, maxX, maxY, item)) {
      this.size++;
    }
  }

  public void insertItem(final double x, final double y, final T item) {
    insertItem(x, y, x, y, item);
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
    final double[] bounds = convert(boundingBox);
    if (bounds != null) {
      double minX = bounds[0];
      double minY = bounds[1];
      double maxX = bounds[2];
      double maxY = bounds[3];

      if (minX == maxX) {
        minX -= this.minExtent;
        maxX += this.minExtent;
      }
      if (minY == maxY) {
        minY -= this.minExtent;
        maxY += this.minExtent;
      }

      final boolean removed = this.root.removeItem(this, minX, minY, maxX, maxY, item);
      if (removed) {
        this.size--;
      }
      return removed;
    } else {
      return false;
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setUseEquals(final boolean useEquals) {
    this.useEquals = useEquals;
  }

  public int size() {
    return getSize();
  }
}
