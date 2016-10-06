/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.model.impl;

import java.io.Serializable;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.util.MathUtil;

/**
 *  Defines a rectangular region of the 2D coordinate plane.
 *  It is often used to represent the bounding box of a {@link Geometry},
 *  e.g. the minimum and maximum x and y values of the {@link Coordinates}s.
 *  <p>
 *  Note that Envelopes support infinite or half-infinite regions, by using the values of
 *  <code>Double.POSITIVE_INFINITY</code> and <code>Double.NEGATIVE_INFINITY</code>.
 *  <p>
 *  When BoundingBoxDoubleGf objects are created or initialized,
 *  the supplies extent values are automatically sorted into the correct order.
 *
 *@version 1.7
 */
public class BoundingBoxDoubleGf implements Serializable, BoundingBox {

  /** The serialization version. */
  private static final long serialVersionUID = -810356856421113732L;

  static {
    ConvertUtils.register(new Converter() {

      @Override
      public Object convert(@SuppressWarnings("rawtypes") final Class paramClass,
        final Object paramObject) {
        if (paramObject == null) {
          return null;
        } else if (BoundingBox.class.isAssignableFrom(paramClass)) {
          if (paramObject instanceof BoundingBox) {
            return paramObject;
          } else {
            return BoundingBox.newBoundingBox(paramObject.toString());
          }
        }
        return null;
      }
    }, BoundingBox.class);
  }

  private final double[] bounds;

  private final GeometryFactory geometryFactory;

  public BoundingBoxDoubleGf() {
    this(GeometryFactory.DEFAULT);
  }

  /**
   * Construct a new Bounding Box.
   *
   * @param geometryFactory The geometry factory.
   */
  public BoundingBoxDoubleGf(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.bounds = null;
  }

  public BoundingBoxDoubleGf(final GeometryFactory geometryFactory, final int axisCount,
    final double... bounds) {
    this.geometryFactory = geometryFactory;
    if (bounds == null || bounds.length == 0 || axisCount < 1) {
      this.bounds = null;
    } else if (bounds.length % axisCount == 0) {
      this.bounds = BoundingBoxUtil.newBounds(axisCount);
      BoundingBoxUtil.expand(geometryFactory, this.bounds, bounds);
    } else {
      throw new IllegalArgumentException(
        "Expecting a multiple of " + axisCount + " not " + bounds.length);
    }
  }

  public BoundingBoxDoubleGf(final GeometryFactory geometryFactory,
    final Iterable<? extends Point> points) {
    this.geometryFactory = geometryFactory;
    double[] bounds = null;
    if (points != null) {
      for (final Point point : points) {
        if (point != null) {
          if (bounds == null) {
            bounds = BoundingBoxUtil.newBounds(geometryFactory, point);
          } else {
            BoundingBoxUtil.expand(geometryFactory, bounds, point);
          }
        }
      }
    }
    this.bounds = bounds;
  }

  public BoundingBoxDoubleGf(final GeometryFactory geometryFactory, final LineString points) {
    this.geometryFactory = geometryFactory;
    double[] bounds = null;
    if (points != null) {
      for (int i = 0; i < points.getVertexCount(); i++) {
        final Point point = points.getPoint(0);
        if (bounds == null) {
          bounds = BoundingBoxUtil.newBounds(geometryFactory, point);
        } else {
          BoundingBoxUtil.expand(geometryFactory, bounds, point);
        }
      }
    }
    this.bounds = bounds;
  }

  public BoundingBoxDoubleGf(final GeometryFactory geometryFactory, final Point point) {
    this.geometryFactory = geometryFactory;
    double[] bounds = null;
    if (point != null) {
      bounds = BoundingBoxUtil.newBounds(geometryFactory, point);
    }
    this.bounds = bounds;
  }

  public BoundingBoxDoubleGf(final GeometryFactory geometryFactory, final Point... points) {
    this(geometryFactory, Lists.newArray(points));
  }

  public BoundingBoxDoubleGf(final GeometryFactory geometryFactory, final Vertex vertex) {
    this((Point)vertex);
  }

  public BoundingBoxDoubleGf(final int axisCount, final double... bounds) {
    this(GeometryFactory.DEFAULT, axisCount, bounds);
  }

  public BoundingBoxDoubleGf(final Iterable<? extends Point> points) {
    this(GeometryFactory.DEFAULT, points);
  }

  public BoundingBoxDoubleGf(final LineString points) {
    this(GeometryFactory.DEFAULT, points);
  }

  public BoundingBoxDoubleGf(final Point... points) {
    this(GeometryFactory.DEFAULT, points);
  }

  /**
   * <p>Bounding boxes are immutable so clone returns this.</p>
   *
   * @return this
   */
  @Override
  public BoundingBox clone() {
    return this;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      return equals(boundingBox);
    } else {
      return false;
    }
  }

  @Override
  public int getAxisCount() {
    if (this.bounds == null) {
      return 0;
    } else {
      return this.bounds.length / 2;
    }
  }

  @Override
  public double[] getBounds() {
    if (this.bounds == null) {
      return this.bounds;
    } else {
      return this.bounds.clone();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.geometryFactory == null) {
      return GeometryFactory.DEFAULT;
    }
    return this.geometryFactory;
  }

  @Override
  public double getMax(final int axisIndex) {
    if (this.bounds == null || axisIndex >= getAxisCount()) {
      return Double.NaN;
    } else {
      return BoundingBoxUtil.getMax(this.bounds, axisIndex);
    }
  }

  @Override
  public double getMin(final int axisIndex) {
    if (this.bounds == null) {
      return Double.NaN;
    } else {
      return BoundingBoxUtil.getMin(this.bounds, axisIndex);
    }
  }

  @Override
  public int hashCode() {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    int result = 17;
    result = 37 * result + MathUtil.hashCode(minX);
    result = 37 * result + MathUtil.hashCode(maxX);
    result = 37 * result + MathUtil.hashCode(minY);
    result = 37 * result + MathUtil.hashCode(maxY);
    return result;
  }

  @Override
  public BoundingBox newBoundingBox(final double x, final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory, 2, x, y);
  }

  @Override
  public BoundingBox newBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory, 2, minX, minY, maxX, maxY);
  }

  @Override
  public BoundingBox newBoundingBox(final GeometryFactory geometryFactory, final int axisCount,
    final double[] bounds) {
    return new BoundingBoxDoubleGf(geometryFactory, axisCount, bounds);
  }

  @Override
  public BoundingBox newBoundingBox(final int axisCount, final double... bounds) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory, 2, bounds);
  }

  @Override
  public BoundingBox newBoundingBox(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory, point);
  }

  @Override
  public BoundingBox newBoundingBoxEmpty() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory);
  }

  @Override
  public String toString() {
    return BoundingBox.toString(this);
  }
}
