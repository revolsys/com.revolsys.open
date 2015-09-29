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

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.slf4j.LoggerFactory;

import com.revolsys.collection.list.Lists;
import com.revolsys.equals.NumberEquals;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.ProjectionFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.wkt.WktParser;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

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

  /**
   * The bitmask that indicates that a point lies below
   * this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public static final int OUT_BOTTOM = 8;

  /**
   * The bitmask that indicates that a point lies to the left of
   * this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public static final int OUT_LEFT = 1;

  /**
   * The bitmask that indicates that a point lies to the right of
   * this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public static final int OUT_RIGHT = 4;

  /**
   * The bitmask that indicates that a point lies above
   * this <code>Rectangle2D</code>.
   * @since 1.2
   */
  public static final int OUT_TOP = 2;

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
            return create(paramObject.toString());
          }
        }
        return null;
      }
    }, BoundingBox.class);
  }

  public static BoundingBox create(final String wkt) {
    if (Property.hasValue(wkt)) {
      GeometryFactory geometryFactory = null;
      final StringBuilder text = new StringBuilder(wkt);
      if (WktParser.hasText(text, "SRID=")) {
        final Integer srid = WktParser.parseInteger(text);
        if (srid != null) {
          geometryFactory = GeometryFactory.floating(srid, 2);
        }
        WktParser.hasText(text, ";");
      }
      if (WktParser.hasText(text, "BBOX(")) {
        final Double x1 = WktParser.parseDouble(text);
        if (WktParser.hasText(text, ",")) {
          final Double y1 = WktParser.parseDouble(text);
          WktParser.skipWhitespace(text);
          final Double x2 = WktParser.parseDouble(text);
          if (WktParser.hasText(text, ",")) {
            final Double y2 = WktParser.parseDouble(text);
            return new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
          } else {
            throw new IllegalArgumentException("Expecting a ',' not " + text);
          }

        } else {
          throw new IllegalArgumentException("Expecting a ',' not " + text);
        }
      } else if (WktParser.hasText(text, "BBOX EMPTY")) {
        return new BoundingBoxDoubleGf(geometryFactory);
      }
    }

    return BoundingBox.EMPTY;
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
      this.bounds = BoundingBoxUtil.createBounds(axisCount);
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
            bounds = BoundingBoxUtil.createBounds(geometryFactory, point);
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
          bounds = BoundingBoxUtil.createBounds(geometryFactory, point);
        } else {
          BoundingBoxUtil.expand(geometryFactory, bounds, point);
        }
      }
    }
    this.bounds = bounds;
  }

  public BoundingBoxDoubleGf(final GeometryFactory geometryFactory, final Point... points) {
    this(geometryFactory, Lists.array(points));
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
  public BoundingBox convert(final GeometryFactory geometryFactory) {
    final GeometryFactory factory = getGeometryFactory();
    if (geometryFactory == null) {
      return this;
    } else if (factory == geometryFactory) {
      return this;
    } else if (factory == null || factory.getCoordinateSystem() == null) {
      return new BoundingBoxDoubleGf(geometryFactory, getAxisCount(), getBounds());
    } else if (isEmpty()) {
      return new BoundingBoxDoubleGf(geometryFactory);
    } else {
      final CoordinatesOperation operation = ProjectionFactory.getCoordinatesOperation(factory,
        geometryFactory);
      if (operation != null) {

        double xStep = getWidth() / 10;
        double yStep = getHeight() / 10;
        final double scaleXY = geometryFactory.getScaleXY();
        if (scaleXY > 0) {
          if (xStep < 1 / scaleXY) {
            xStep = 1 / scaleXY;
          }
          if (yStep < 1 / scaleXY) {
            yStep = 1 / scaleXY;
          }
        }

        final int axisCount = getAxisCount();

        final double minX = getMinX();
        final double maxX = getMaxX();
        final double minY = getMinY();
        final double maxY = getMaxY();

        final double[] bounds = getBounds();
        bounds[0] = Double.NaN;
        bounds[1] = Double.NaN;
        bounds[axisCount] = Double.NaN;
        bounds[axisCount + 1] = Double.NaN;

        final double[] to = new double[2];
        expand(geometryFactory, bounds, operation, to, minX, minY);
        expand(geometryFactory, bounds, operation, to, minX, maxY);
        expand(geometryFactory, bounds, operation, to, minX, minY);
        expand(geometryFactory, bounds, operation, to, maxX, minY);

        if (xStep != 0) {
          for (double x = minX + xStep; x < maxX; x += xStep) {
            expand(geometryFactory, bounds, operation, to, x, minY);
            expand(geometryFactory, bounds, operation, to, x, maxY);
          }
        }
        if (yStep != 0) {
          for (double y = minY + yStep; y < maxY; y += yStep) {
            expand(geometryFactory, bounds, operation, to, minX, y);
            expand(geometryFactory, bounds, operation, to, maxX, y);
          }
        }
        return new BoundingBoxDoubleGf(geometryFactory, axisCount, bounds);
      } else {
        return this;
      }
    }
  }

  @Override
  public BoundingBox convert(GeometryFactory geometryFactory, final int axisCount) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null || sourceGeometryFactory == null) {
      return this;
    } else {
      geometryFactory = geometryFactory.convertAxisCount(axisCount);
      boolean copy = false;
      if (geometryFactory != null && sourceGeometryFactory != geometryFactory) {
        final int srid = getCoordinateSystemId();
        final int srid2 = geometryFactory.getCoordinateSystemId();
        if (srid <= 0) {
          if (srid2 > 0) {
            copy = true;
          }
        } else if (srid != srid2) {
          copy = true;
        }
        if (!copy) {
          for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
            final double scale = sourceGeometryFactory.getScale(axisIndex);
            final double scale1 = geometryFactory.getScale(axisIndex);
            if (!NumberEquals.equal(scale, scale1)) {
              copy = true;
            }
          }
        }
      }
      if (copy) {
        return convert(geometryFactory);
      } else {
        return this;
      }
    }
  }

  @Override
  public boolean coveredBy(final double... bounds) {
    final double minX1 = bounds[0];
    final double minY1 = bounds[1];
    final double maxX1 = bounds[2];
    final double maxY1 = bounds[3];
    final double minX2 = getMinX();
    final double minY2 = getMinY();
    final double maxX2 = getMaxX();
    final double maxY2 = getMaxY();
    return BoundingBoxUtil.covers(minX1, minY1, maxX1, maxY1, minX2, minY2, maxX2, maxY2);
  }

  /**
   * Tests if the <code>BoundingBoxDoubleGf other</code>
   * lies wholely inside this <code>BoundingBoxDoubleGf</code> (inclusive of the boundary).
   *
   *@param  other the <code>BoundingBoxDoubleGf</code> to check
   *@return true if this <code>BoundingBoxDoubleGf</code> covers the <code>other</code>
   */
  @Override
  public boolean covers(BoundingBox other) {
    if (other == null || isEmpty() || other.isEmpty()) {
      return false;
    } else {
      other = other.convert(getGeometryFactory());
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return other.coveredBy(minX, minY, maxX, maxY);
    }
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  x  the x-coordinate of the point which this <code>BoundingBoxDoubleGf</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>BoundingBoxDoubleGf</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>BoundingBoxDoubleGf</code>.
   */
  @Override
  public boolean covers(final double x, final double y) {
    if (isEmpty()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return BoundingBoxUtil.covers(minX, minY, maxX, maxY, x, y, x, y);
    }
  }

  @Override
  public boolean covers(final Geometry geometry) {
    if (geometry == null) {
      return false;
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return covers(boundingBox);
    }
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  p  the point which this <code>BoundingBoxDoubleGf</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>BoundingBoxDoubleGf</code>.
   */
  @Override
  public boolean covers(final Point point) {
    if (point == null || point.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point projectedPoint = point.convert(geometryFactory);
      final double x = projectedPoint.getX();
      final double y = projectedPoint.getY();
      return covers(x, y);
    }
  }

  /**
   * Computes the distance between this and another
   * <code>BoundingBoxDoubleGf</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  @Override
  public double distance(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox.convert(geometryFactory);
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();

    final double minX2 = boundingBox.getMinX();
    final double minY2 = boundingBox.getMinY();
    final double maxX2 = boundingBox.getMaxX();
    final double maxY2 = boundingBox.getMaxY();

    if (isEmpty(minX, maxX) || isEmpty(minX2, maxX2)) {
      // Empty
      return Double.MAX_VALUE;
    } else if (!(minX2 > maxX || maxX2 < minX || minY2 > maxY || maxY2 < minY)) {
      // Intersects
      return 0;
    } else {
      double dx;
      if (maxX < minX2) {
        dx = minX2 - maxX;
      } else {
        if (minX > maxX2) {
          dx = minX - maxX2;
        } else {
          dx = 0;
        }
      }

      double dy;
      if (maxY < minY2) {
        dy = minY2 - maxY;
      } else if (minY > maxY2) {
        dy = minY - maxY2;
      } else {
        dy = 0;
      }

      if (dx == 0.0) {
        return dy;
      } else if (dy == 0.0) {
        return dx;
      } else {
        return Math.sqrt(dx * dx + dy * dy);
      }
    }
  }

  @Override
  public double distance(final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return distance(boundingBox);
  }

  /**
   * Computes the distance between this and another
   * <code>BoundingBoxDoubleGf</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  @Override
  public double distance(Point point) {
    point = point.convert(getGeometryFactory());
    if (intersects(point)) {
      return 0;
    } else {
      final double x = point.getX();
      final double y = point.getY();

      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      double dx = 0.0;
      if (maxX < x) {
        dx = x - maxX;
      } else if (minX > x) {
        dx = minX - x;
      }

      double dy = 0.0;
      if (maxY < y) {
        dy = y - maxY;
      } else if (minY > y) {
        dy = minY - y;
      }

      // if either is zero, the envelopes overlap either vertically or
      // horizontally
      if (dx == 0.0) {
        return dy;
      }
      if (dy == 0.0) {
        return dx;
      }
      return Math.sqrt(dx * dx + dy * dy);
    }
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
   * Return a new bounding box expanded by delta.
   *
   * @param delta
   * @return
   */
  @Override
  public BoundingBox expand(final double delta) {
    return expand(delta, delta);
  }

  /**
   * Return a new bounding box expanded by deltaX, deltaY.
   *
   * @param delta
   * @return
   */
  @Override
  public BoundingBox expand(final double deltaX, final double deltaY) {
    if (isEmpty() || deltaX == 0 && deltaY == 0) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double x1 = getMinX() - deltaX;
      final double x2 = getMaxX() + deltaX;
      final double y1 = getMinY() - deltaY;
      final double y2 = getMaxY() + deltaY;

      if (x1 > x2 || y1 > y2) {
        return new BoundingBoxDoubleGf(geometryFactory);
      } else {
        return new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
      }
    }
  }

  private void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final CoordinatesOperation operation, final double[] to, final double... from) {

    operation.perform(2, from, 2, to);
    BoundingBoxUtil.expand(geometryFactory, bounds, to);
  }

  @Override
  public BoundingBox expand(final Point coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return new BoundingBoxDoubleGf(geometryFactory, coordinates);
    } else {
      final double x = coordinates.getX();
      final double y = coordinates.getY();

      double minX = getMinX();
      double maxX = getMaxX();
      double minY = getMinY();
      double maxY = getMaxY();

      if (x < minX) {
        minX = x;
      }
      if (x > maxX) {
        maxX = x;
      }
      if (y < minY) {
        minY = y;
      }
      if (y > maxY) {
        maxY = y;
      }
      return new BoundingBoxDoubleGf(geometryFactory, 2, minX, minY, maxX, maxY);
    }
  }

  @Override
  public BoundingBox expandPercent(final double factor) {
    return expandPercent(factor, factor);
  }

  @Override
  public BoundingBox expandPercent(final double factorX, final double factorY) {
    if (isEmpty()) {
      return this;
    } else {
      final double deltaX = getWidth() * factorX / 2;
      final double deltaY = getHeight() * factorY / 2;
      return expand(deltaX, deltaY);
    }
  }

  @Override
  public BoundingBox expandToInclude(final BoundingBox other) {
    if (other == null || other.isEmpty()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedOther = other.convert(geometryFactory);
      if (isEmpty()) {
        return convertedOther;
      } else if (covers(convertedOther)) {
        return this;
      } else {
        final double minX = Math.min(getMinX(), convertedOther.getMinX());
        final double maxX = Math.max(getMaxX(), convertedOther.getMaxX());
        final double minY = Math.min(getMinY(), convertedOther.getMinY());
        final double maxY = Math.max(getMaxY(), convertedOther.getMaxY());
        return new BoundingBoxDoubleGf(geometryFactory, 2, minX, minY, maxX, maxY);
      }
    }
  }

  @Override
  public BoundingBox expandToInclude(final double... coordinates) {
    if (coordinates == null || coordinates.length < 2) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (isEmpty()) {
        return new BoundingBoxDoubleGf(geometryFactory, coordinates.length, coordinates);
      } else {
        final double[] bounds = getBounds();
        final int axisCount = getAxisCount();
        BoundingBoxUtil.expand(bounds, axisCount, coordinates);
        return new BoundingBoxDoubleGf(geometryFactory, axisCount, bounds);
      }
    }
  }

  @Override
  public BoundingBox expandToInclude(final Geometry geometry) {
    if (geometry == null || geometry.isEmpty()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Geometry convertedGeometry = geometry.convert(geometryFactory);
      final BoundingBox box = convertedGeometry.getBoundingBox();
      return expandToInclude(box);
    }
  }

  public BoundingBox expandToInclude(final Point point) {
    return expandToInclude((Geometry)point);
  }

  @Override
  public BoundingBox expandToInclude(final Record object) {
    if (object != null) {
      final Geometry geometry = object.getGeometry();
      return expandToInclude(geometry);
    }
    return this;
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
  public Measure<Length> getHeightLength() {
    final double height = getHeight();
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return Measure.valueOf(height, SI.METRE);
    } else {
      return Measure.valueOf(height, coordinateSystem.getLengthUnit());
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
  public <Q extends Quantity> Measurable<Q> getMaximum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double max = this.getMax(axisIndex);
    return Measure.valueOf(max, unit);
  }

  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMaximum(final int axisIndex, final Unit convertUnit) {
    final Measurable<Quantity> max = getMaximum(axisIndex);
    return max.doubleValue(convertUnit);
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
  public <Q extends Quantity> Measurable<Q> getMinimum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double min = this.getMin(axisIndex);
    return Measure.valueOf(min, unit);
  }

  @Override
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMinimum(final int axisIndex, final Unit convertUnit) {
    final Measurable<Quantity> min = getMinimum(axisIndex);
    return min.doubleValue(convertUnit);
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

  public Point getTopRightPoint() {
    return getGeometryFactory().point(getMaxX(), getMaxY());
  }

  @SuppressWarnings("unchecked")
  private <Q extends Quantity> Unit<Q> getUnit() {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return (Unit<Q>)SI.METRE;
    } else {
      return coordinateSystem.<Q> getUnit();
    }
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
  public Measure<Length> getWidthLength() {
    final double width = getWidth();
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return Measure.valueOf(width, SI.METRE);
    } else {
      return Measure.valueOf(width, coordinateSystem.getLengthUnit());
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
   * @return a new WmsBoundingBox representing the intersection of the envelopes (this will be
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

  public boolean intersects(final Geometry geometry) {
    return geometry.intersects(this);
  }

  /**
   *  Check if the point <code>p</code>
   *  overlaps (lies inside) the region of this <code>BoundingBoxDoubleGf</code>.
   *
   *@param  p  the <code>Coordinate</code> to be tested
   *@return        <code>true</code> if the point overlaps this <code>BoundingBoxDoubleGf</code>
   */
  @Override
  public boolean intersects(final Point point) {
    return point.intersects(this);
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
  public boolean isWithinDistance(final BoundingBox boundingBox, final double maxDistance) {
    final double distance = boundingBox.distance(boundingBox);
    if (distance < maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isWithinDistance(final Geometry geometry, final double maxDistance) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return isWithinDistance(boundingBox, maxDistance);
  }

  /**
   * <p>Construct a new new WmsBoundingBox by moving the min/max x coordinates by xDisplacement and
   * the min/max y coordinates by yDisplacement. If the bounding box is null or the xDisplacement
   * and yDisplacement are 0 then this bounding box will be returned.</p>
   *
   * @param xDisplacement The distance to move the min/max x coordinates.
   * @param yDisplacement The distance to move the min/max y coordinates.
   * @return The moved bounding box.
   */
  @Override
  public BoundingBox move(final double xDisplacement, final double yDisplacement) {
    if (isEmpty() || xDisplacement == 0 && yDisplacement == 0) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double x1 = getMinX() + xDisplacement;
      final double x2 = getMaxX() + xDisplacement;
      final double y1 = getMinY() + yDisplacement;
      final double y2 = getMaxY() + yDisplacement;
      return new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
    }
  }

  private int outcode(final double x, final double y) {
    int out = 0;
    if (x < getMinX()) {
      out |= OUT_LEFT;
    } else if (x > getMaxX()) {
      out |= OUT_RIGHT;
    }
    if (y < getMinY()) {
      out |= OUT_BOTTOM;
    } else if (y > getMaxY()) {
      out |= OUT_TOP;
    }
    return out;
  }

  @Override
  public Geometry toGeometry() {
    GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.floating(0, 2);
    }
    if (isEmpty()) {
      return geometryFactory.point();
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      final double width = getWidth();
      final double height = getHeight();
      if (width == 0 && height == 0) {
        return geometryFactory.point(minX, minY);
      } else if (width == 0 || height == 0) {
        return geometryFactory.lineString(2, minX, minY, maxX, maxY);
      } else {
        return geometryFactory.polygon(geometryFactory.linearRing(2, minX, minY, minX, maxY, maxX,
          maxY, maxX, minY, minX, minY));
      }
    }
  }

  @Override
  public Polygon toPolygon() {
    return toPolygon(100, 100);

  }

  @Override
  public Polygon toPolygon(final GeometryFactory factory) {
    return toPolygon(factory, 100, 100);
  }

  @Override
  public Polygon toPolygon(final GeometryFactory factory, final int numSegments) {
    return toPolygon(factory, numSegments, numSegments);
  }

  @Override
  public Polygon toPolygon(GeometryFactory geometryFactory, int numX, int numY) {
    if (isEmpty()) {
      return geometryFactory.polygon();
    } else {
      final GeometryFactory factory = getGeometryFactory();
      if (geometryFactory == null) {
        if (factory == null) {
          geometryFactory = GeometryFactory.floating(0, 2);
        } else {
          geometryFactory = factory;
        }
      }
      try {
        double minStep = 0.00001;
        final CoordinateSystem coordinateSystem = factory.getCoordinateSystem();
        if (coordinateSystem instanceof ProjectedCoordinateSystem) {
          minStep = 1;
        } else {
          minStep = 0.00001;
        }

        double xStep;
        final double width = getWidth();
        if (numX <= 1) {
          numX = 1;
          xStep = width;
        } else {
          xStep = width / numX;
          if (xStep < minStep) {
            xStep = minStep;
          }
          numX = Math.max(1, (int)Math.ceil(width / xStep));
        }

        double yStep;
        if (numY <= 1) {
          numY = 1;
          yStep = getHeight();
        } else {
          yStep = getHeight() / numY;
          if (yStep < minStep) {
            yStep = minStep;
          }
          numY = Math.max(1, (int)Math.ceil(getHeight() / yStep));
        }

        final double minX = getMinX();
        final double maxX = getMaxX();
        final double minY = getMinY();
        final double maxY = getMaxY();
        final int numCoordinates = 1 + 2 * (numX + numY);
        final double[] coordinates = new double[numCoordinates * 2];
        int i = 0;

        CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX, minY);
        i++;
        for (int j = 0; j < numX - 1; j++) {
          CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX - j * xStep, minY);
          i++;
        }
        CoordinatesListUtil.setCoordinates(coordinates, 2, i, minX, minY);
        i++;

        for (int j = 0; j < numY - 1; j++) {
          CoordinatesListUtil.setCoordinates(coordinates, 2, i, minX, minY + j * yStep);
          i++;
        }
        CoordinatesListUtil.setCoordinates(coordinates, 2, i, minX, maxY);
        i++;

        for (int j = 0; j < numX - 1; j++) {
          CoordinatesListUtil.setCoordinates(coordinates, 2, i, minX + j * xStep, maxY);
          i++;
        }

        CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX, maxY);
        i++;

        for (int j = 0; j < numY - 1; j++) {
          CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX, minY + (numY - j) * yStep);
          i++;
        }
        CoordinatesListUtil.setCoordinates(coordinates, 2, i, maxX, minY);

        final LinearRing ring = factory.linearRing(2, coordinates);
        final Polygon polygon = factory.polygon(ring);
        if (geometryFactory == null) {
          return polygon;
        } else {
          return (Polygon)polygon.convert(geometryFactory);
        }
      } catch (final IllegalArgumentException e) {
        LoggerFactory.getLogger(getClass()).error("Unable to convert to polygon: " + this, e);
        return geometryFactory.polygon();
      }
    }
  }

  @Override
  public Polygon toPolygon(final int numSegments) {
    return toPolygon(numSegments, numSegments);
  }

  @Override
  public Polygon toPolygon(final int numX, final int numY) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return toPolygon(geometryFactory, numX, numY);
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
        s.append(MathUtil.toString(getMin(axisIndex)));
      }
      s.append(' ');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(',');
        }
        s.append(MathUtil.toString(getMax(axisIndex)));
      }
      s.append(')');
    }
    return s.toString();
  }

}
