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

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.QuantityType;
import com.revolsys.util.number.Doubles;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

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

  public static boolean isEmpty(final double minX, final double maxX) {
    if (Double.isNaN(minX)) {
      return true;
    } else if (Double.isNaN(maxX)) {
      return true;
    } else {
      return maxX < minX;
    }
  }

  private final double[] bounds;

  private GeometryFactory geometryFactory;

  public BoundingBoxDoubleGf() {
    this.bounds = null;
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
    this(null, axisCount, bounds);
  }

  public BoundingBoxDoubleGf(final Iterable<? extends Point> points) {
    this(null, points);
  }

  public BoundingBoxDoubleGf(final LineString points) {
    this(null, points);
  }

  public BoundingBoxDoubleGf(final Point... points) {
    this(null, points);
  }

  @Override
  public BoundingBox clipToCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem == null) {
      return this;
    } else {
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      return intersection(areaBoundingBox);
    }
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
      if (isEmpty()) {
        return boundingBox.isEmpty();
      } else if (boundingBox.isEmpty()) {
        return false;
      } else if (getCoordinateSystemId() == boundingBox.getCoordinateSystemId()) {
        if (getMaxX() == boundingBox.getMaxX()) {
          if (getMaxY() == boundingBox.getMaxY()) {
            if (getMinX() == boundingBox.getMinX()) {
              if (getMinY() == boundingBox.getMinY()) {
                return true;
              }
            }
          }
        }

      }
    }
    return false;
  }

  /**
   * Gets the area of this envelope.
   *
   * @return the area of the envelope
   * @return 0.0 if the envelope is null
   */
  @Override
  public double getArea() {
    if (getAxisCount() < 2) {
      return 0;
    } else {
      final double width = getWidth();
      final double height = getHeight();
      return width * height;
    }
  }

  /**
   * Get the aspect ratio x:y.
   *
   * @return The aspect ratio.
   */
  @Override
  public double getAspectRatio() {
    final double width = getWidth();
    final double height = getHeight();
    final double aspectRatio = width / height;
    return aspectRatio;
  }

  @Override
  public int getAxisCount() {
    if (this.bounds == null) {
      return 0;
    } else {
      return this.bounds.length / 2;
    }
  }

  public Point getBottomLeftPoint() {
    return getGeometryFactory().point(getMinX(), getMinY());
  }

  public Point getBottomRightPoint() {
    return getGeometryFactory().point(getMaxX(), getMinY());
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
  public double[] getBounds(final int axisCount) {
    if (this.bounds == null) {
      return this.bounds;
    } else {
      final double[] bounds = new double[2 * axisCount];
      for (int i = 0; i < axisCount; i++) {
        bounds[i] = getMin(i);
        bounds[i + axisCount] = getMax(i);
      }
      return bounds;
    }
  }

  @Override
  public Point getCentre() {
    if (isEmpty()) {
      return this.geometryFactory.point();
    } else {
      final double centreX = getCentreX();
      final double centreY = getCentreY();
      return this.geometryFactory.point(centreX, centreY);
    }
  }

  @Override
  public double getCentreX() {
    return getMinX() + getWidth() / 2;
  }

  @Override
  public double getCentreY() {
    return getMinY() + getHeight() / 2;
  }

  /**
   * Get the geometry factory.
   *
   * @return The geometry factory.
   */
  @Override
  public CoordinateSystem getCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getCoordinateSystem();
    }
  }

  /**
   * maxX,minY
   * minX,minY
   * minX,maxY
   * maxX,maxY
   */
  @Override
  public Point getCornerPoint(int index) {
    if (isEmpty()) {
      return null;
    } else {
      final double minX = getMinX();
      final double maxX = getMaxX();
      final double minY = getMinY();
      final double maxY = getMaxY();
      index = index % 4;
      switch (index) {
        case 0:
          return new PointDoubleGf(getGeometryFactory(), maxX, minY);
        case 1:
          return new PointDoubleGf(getGeometryFactory(), minX, minY);
        case 2:
          return new PointDoubleGf(getGeometryFactory(), minX, maxY);
        default:
          return new PointDoubleGf(getGeometryFactory(), maxX, maxY);
      }
    }
  }

  @Override
  public LineString getCornerPoints() {
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    return new LineStringDouble(2, maxX, minY, minX, minY, minX, maxY, maxX, maxY);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  /**
   *  Returns the difference between the maximum and minimum y values.
   *
   *@return    max y - min y, or 0 if this is a null <code>BoundingBoxDoubleGf</code>
   */
  @Override
  public double getHeight() {
    if (getAxisCount() < 2) {
      return 0;
    } else {
      return getMaxY() - getMinY();
    }
  }

  @Override
  public Quantity<Length> getHeightLength() {
    final double height = getHeight();
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return Quantities.getQuantity(height, Units.METRE);
    } else {
      return Quantities.getQuantity(height, coordinateSystem.getLengthUnit());
    }
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
  public <Q extends Quantity<Q>> Quantity<Q> getMaximum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double max = this.getMax(axisIndex);
    return Quantities.getQuantity(max, unit);
  }

  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity<Q>> double getMaximum(final int axisIndex, final Unit convertUnit) {
    final Quantity<Q> max = getMaximum(axisIndex);
    return QuantityType.doubleValue(max, convertUnit);
  }

  /**
   *  Returns the <code>BoundingBoxDoubleGf</code>s maximum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBoxDoubleGf</code>.
   *
   *@return    the maximum x-coordinate
   */
  @Override
  public double getMaxX() {
    return getMax(0);
  }

  /**
   *  Returns the <code>BoundingBoxDoubleGf</code>s maximum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBoxDoubleGf</code>.
   *
   *@return    the maximum y-coordinate
   */
  @Override
  public double getMaxY() {
    return getMax(1);
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
  public <Q extends Quantity<Q>> Quantity<Q> getMinimum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double min = this.getMin(axisIndex);
    return Quantities.getQuantity(min, unit);
  }

  /**
   *  Returns the <code>BoundingBoxDoubleGf</code>s minimum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBoxDoubleGf</code>.
   *
   *@return    the minimum x-coordinate
   */
  @Override
  public double getMinX() {
    return getMin(0);
  }

  /**
   *  Returns the <code>BoundingBoxDoubleGf</code>s minimum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBoxDoubleGf</code>.
   *
   *@return    the minimum y-coordinate
   */
  @Override
  public double getMinY() {
    return getMin(1);
  }

  @Override
  public Point getRandomPointWithin() {
    final double x = getMinX() + getWidth() * Math.random();
    final double y = getMinY() + getHeight() * Math.random();
    return this.geometryFactory.point(x, y);
  }

  @Override
  public Point getTopLeftPoint() {
    return getGeometryFactory().point(getMinX(), getMaxY());
  }

  @Override
  public Point getTopRightPoint() {
    return getGeometryFactory().point(getMaxX(), getMaxY());
  }

  /**
   *  Returns the difference between the maximum and minimum x values.
   *
   *@return    max x - min x, or 0 if this is a null <code>BoundingBoxDoubleGf</code>
   */
  @Override
  public double getWidth() {
    if (getAxisCount() < 2) {
      return 0;
    } else {
      final double minX = getMinX();
      final double maxX = getMaxX();

      return maxX - minX;
    }
  }

  @Override
  public Quantity<Length> getWidthLength() {
    final double width = getWidth();
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return Quantities.getQuantity(width, Units.METRE);
    } else {
      return Quantities.getQuantity(width, coordinateSystem.getLengthUnit());
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

  /**
   * Computes the intersection of two {@link BoundingBoxDoubleGf}s.
   *
   * @param env the envelope to intersect with
   * @return a new BoundingBox representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  @Override
  public BoundingBox intersection(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    if (isEmpty() || convertedBoundingBox.isEmpty() || !intersects(convertedBoundingBox)) {
      return new BoundingBoxDoubleGf(geometryFactory);
    } else {
      final double intMinX = Math.max(getMinX(), convertedBoundingBox.getMinX());
      final double intMinY = Math.max(getMinY(), convertedBoundingBox.getMinY());
      final double intMaxX = Math.min(getMaxX(), convertedBoundingBox.getMaxX());
      final double intMaxY = Math.min(getMaxY(), convertedBoundingBox.getMaxY());
      return new BoundingBoxDoubleGf(geometryFactory, 2, intMinX, intMinY, intMaxX, intMaxY);
    }
  }

  /**
   *  Check if the region defined by <code>other</code>
   *  overlaps (intersects) the region of this <code>BoundingBoxDoubleGf</code>.
   *
   *@param  other  the <code>BoundingBoxDoubleGf</code> which this <code>BoundingBoxDoubleGf</code> is
   *          being checked for overlapping
   *@return        <code>true</code> if the <code>BoundingBoxDoubleGf</code>s overlap
   */
  @Override
  public boolean intersects(final BoundingBox other) {
    if (isEmpty() || other.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = other.convert(geometryFactory, 2);
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      final double minX2 = convertedBoundingBox.getMinX();
      final double minY2 = convertedBoundingBox.getMinY();
      final double maxX2 = convertedBoundingBox.getMaxX();
      final double maxY2 = convertedBoundingBox.getMaxY();
      return !(minX2 > maxX || maxX2 < minX || minY2 > maxY || maxY2 < minY);
    }
  }

  /**
   *  Check if the point <code>(x, y)</code>
   *  overlaps (lies inside) the region of this <code>BoundingBoxDoubleGf</code>.
   *
   *@param  x  the x-ordinate of the point
   *@param  y  the y-ordinate of the point
   *@return        <code>true</code> if the point overlaps this <code>BoundingBoxDoubleGf</code>
   */
  @Override
  public boolean intersects(final double x, final double y) {
    if (isEmpty()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      return !(x > maxX || x < minX || y > maxY || y < minY);
    }
  }

  @Override
  public boolean intersects(double x0, double y0, double x1, double y1) {
    int out1 = outcode(x0, y0);
    int out2 = outcode(x1, y1);
    final double xmin = getMinX();
    final double ymin = getMinY();
    final double xmax = getMaxX();
    final double ymax = getMaxY();
    while (true) {
      if ((out1 | out2) == 0) {
        return true;
      } else if ((out1 & out2) != 0) {
        return false;
      } else {

        int out;
        if (out1 != 0) {
          out = out1;
        } else {
          out = out2;
        }

        double x = 0;
        double y = 0;
        if ((out & OUT_TOP) != 0) {
          x = x0 + (x1 - x0) * (ymax - y0) / (y1 - y0);
          y = ymax;
        } else if ((out & OUT_BOTTOM) != 0) {
          x = x0 + (x1 - x0) * (ymin - y0) / (y1 - y0);
          y = ymin;
        } else if ((out & OUT_RIGHT) != 0) {
          y = y0 + (y1 - y0) * (xmax - x0) / (x1 - x0);
          x = xmax;
        } else if ((out & OUT_LEFT) != 0) {
          y = y0 + (y1 - y0) * (xmin - x0) / (x1 - x0);
          x = xmin;
        }

        if (out == out1) {
          x0 = x;
          y0 = y;
          out1 = outcode(x0, y0);
        } else {
          x1 = x;
          y1 = y;
          out2 = outcode(x1, y1);
        }
      }
    }
  }

  @Override
  public boolean intersects(final Geometry geometry) {
    return geometry.intersects(this);
  }

  @Override
  public boolean isEmpty() {
    final double minX = getMinX();
    final double maxX = getMaxX();
    if (Double.isNaN(minX)) {
      return true;
    } else if (Double.isNaN(maxX)) {
      return true;
    } else {
      return maxX < minX;
    }
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    final int srid = getCoordinateSystemId();
    if (srid > 0) {
      s.append("SRID=");
      s.append(srid);
      s.append(";");
    }
    if (isEmpty()) {
      s.append("BBOX EMPTY");
    } else {
      s.append("BBOX");
      final int axisCount = getAxisCount();
      if (axisCount == 3) {
        s.append(" Z");
      } else if (axisCount == 4) {
        s.append(" ZM");
      } else if (axisCount != 2) {
        s.append(" ");
        s.append(axisCount);
      }
      s.append("(");
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(',');
        }
        s.append(Doubles.toString(getMin(axisIndex)));
      }
      s.append(' ');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(',');
        }
        s.append(Doubles.toString(getMax(axisIndex)));
      }
      s.append(')');
    }
    return s.toString();
  }

}
