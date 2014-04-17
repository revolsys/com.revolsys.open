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
package com.revolsys.jts.geom;

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
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.projection.CoordinatesListProjectionUtil;
import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.cs.projection.GeometryOperation;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.jts.LineSegment;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.wkt.WktParser;
import com.revolsys.jts.util.EnvelopeUtil;
import com.revolsys.util.MathUtil;

/**
 *  Defines a rectangular region of the 2D coordinate plane.
 *  It is often used to represent the bounding box of a {@link Geometry},
 *  e.g. the minimum and maximum x and y values of the {@link Coordinates}s.
 *  <p>
 *  Note that Envelopes support infinite or half-infinite regions, by using the values of
 *  <code>Double.POSITIVE_INFINITY</code> and <code>Double.NEGATIVE_INFINITY</code>.
 *  <p>
 *  When Envelope objects are created or initialized,
 *  the supplies extent values are automatically sorted into the correct order.
 *
 *@version 1.7
 */
public class Envelope implements Serializable, BoundingBox {

  static {
    ConvertUtils.register(new Converter() {

      @Override
      public Object convert(
        @SuppressWarnings("rawtypes") final Class paramClass,
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

  /** The serialization version. */
  private static final long serialVersionUID = -810356856421113732L;

  public static BoundingBox create(final String wkt) {
    if (StringUtils.hasLength(wkt)) {
      GeometryFactory geometryFactory = null;
      final StringBuffer text = new StringBuffer(wkt);
      if (WktParser.hasText(text, "SRID=")) {
        final Integer srid = WktParser.parseInteger(text);
        if (srid != null) {
          geometryFactory = GeometryFactory.getFactory(srid, 2);
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
            return new Envelope(geometryFactory, x1, y1, x2, y2);
          } else {
            throw new IllegalArgumentException("Expecting a ',' not " + text);
          }

        } else {
          throw new IllegalArgumentException("Expecting a ',' not " + text);
        }
      } else if (WktParser.hasText(text, "BBOX EMPTY")) {
        return new Envelope(geometryFactory);
      }
    }

    return new Envelope();
  }

  public static BoundingBox getBoundingBox(final DataObject object) {
    if (object == null) {
      return new Envelope();
    } else {
      final Geometry geometry = object.getGeometryValue();
      return getBoundingBox(geometry);
    }
  }

  public static BoundingBox getBoundingBox(final Geometry geometry) {
    if (geometry == null) {
      return new Envelope();
    } else {
      return geometry.getBoundingBox();
    }
  }

  public static BoundingBox getBoundingBox(
    final GeometryFactory geometryFactory, final DataObject object) {
    final BoundingBox boundingBox = getBoundingBox(object);
    return boundingBox.convert(geometryFactory);
  }

  public static BoundingBox getBoundingBox(
    final GeometryFactory geometryFactory, final Geometry geometry) {
    final BoundingBox boundingBox = getBoundingBox(geometry);
    return boundingBox.convert(geometryFactory);
  }

  /**
   * Point intersects the bounding box of the line.
   * 
   * @param lineStart
   * @param lineEnd
   * @param point
   * @return
   */
  public static boolean intersects(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();

    final double x = point.getX();
    final double y = point.getY();
    return intersects(x1, y1, x2, y2, x, y);
  }

  /**
   * Tests whether the envelope defined by p1-p2
   * and the envelope defined by q1-q2
   * intersect.
   * 
   * @param p1 one extremal point of the envelope P
   * @param p2 another extremal point of the envelope P
   * @param q1 one extremal point of the envelope Q
   * @param q2 another extremal point of the envelope Q
   * @return <code>true</code> if Q intersects P
   */
  public static boolean intersects(final Coordinates line1Start,
    final Coordinates line1End, final Coordinates line2Start,
    final Coordinates line2End) {
    final double line1x1 = line1Start.getX();
    final double line1y1 = line1Start.getY();
    final double line1x2 = line1End.getX();
    final double line1y2 = line1End.getY();

    final double line2x1 = line2Start.getX();
    final double line2y1 = line2Start.getY();
    final double line2x2 = line2End.getX();
    final double line2y2 = line2End.getY();
    return intersects(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1,
      line2x2, line2y2);
  }

  /**
   * Point intersects the bounding box of the line.
   * 
   * @param lineStart
   * @param lineEnd
   * @param point
   * @return
   */
  public static boolean intersects(final double p1X, final double p1Y,
    final double p2X, final double p2Y, final double qX, final double qY) {
    if (((qX >= (p1X < p2X ? p1X : p2X)) && (qX <= (p1X > p2X ? p1X : p2X)))
      && ((qY >= (p1Y < p2Y ? p1Y : p2Y)) && (qY <= (p1Y > p2Y ? p1Y : p2Y)))) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean intersects(final double p1X, final double p1Y,
    final double p2X, final double p2Y, final double q1X, final double q1Y,
    final double q2X, final double q2Y) {
    double minp = Math.min(p1X, p2X);
    double maxq = Math.max(q1X, q2X);
    if (minp > maxq) {
      return false;
    } else {
      double minq = Math.min(q1X, q2X);
      double maxp = Math.max(p1X, p2X);
      if (maxp < minq) {
        return false;
      } else {
        minp = Math.min(p1Y, p2Y);
        maxq = Math.max(q1Y, q2Y);
        if (minp > maxq) {
          return false;
        } else {
          minq = Math.min(q1Y, q2Y);
          maxp = Math.max(p1Y, p2Y);
          if (maxp < minq) {
            return false;
          } else {
            return true;
          }
        }
      }
    }
  }

  public static boolean isEmpty(final BoundingBox boundingBox) {
    if (boundingBox == null) {
      return true;
    } else {
      return boundingBox.isEmpty();
    }
  }

  public static Envelope parse(final String bbox) {
    final String[] args = bbox.split(",");
    if (args.length == 4) {
      final double x1 = Double.valueOf(args[0]);
      final double y1 = Double.valueOf(args[1]);
      final double x2 = Double.valueOf(args[2]);
      final double y2 = Double.valueOf(args[3]);
      return new Envelope(GeometryFactory.getFactory(4326), x1, y1, x2, y2);
    } else {
      throw new IllegalArgumentException(
        "BBOX must have match <minX>,<minY>,<maxX>,<maxY> not " + bbox);
    }
  }

  private double[] bounds;

  private GeometryFactory geometryFactory;

  public Envelope() {
  }

  /**
   *  Create an <code>Envelope</code> from an existing Envelope.
   *
   *@param  env  the BoundingBox to initialize from
   */
  public Envelope(final BoundingBox boundingBox) {
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.bounds = boundingBox.getBounds();
  }

  public Envelope(final Coordinates point) {
    this((GeometryFactory)null, point);
  }

  /**
   *  Creates an <code>Envelope</code> for a region defined by two Coordinates.
   *
   *@param  p1  the first Coordinate
   *@param  p2  the second Coordinate
   */
  public Envelope(final Coordinates point1, final Coordinates point2) {
    this((GeometryFactory)null, point1, point2);
  }

  public Envelope(final double x, final double y) {
    this(null, x, y);
  }

  /**
   *  Creates an <code>Envelope</code> for a region defined by maximum and minimum values.
   *
   *@param  x1  the first x-value
   * @param  y1  the first y-value
   * @param  x2  the second x-value
   * @param  y2  the second y-value
   */
  public Envelope(final double x1, final double y1, final double x2,
    final double y2) {
    this(null, x1, y1, x2, y2);
  }

  public Envelope(final Geometry geometry) {
    this(GeometryFactory.getFactory(geometry), geometry.getBoundingBox());
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   */
  public Envelope(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory to convert the bounding box to.
   * @param boundingBox The bounding box.
   */
  public Envelope(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox) {
    this.geometryFactory = geometryFactory;
    if (geometryFactory == null) {
      throw new IllegalArgumentException(
        "A bounding box must have a geometry factory");
    } else if (boundingBox.getGeometryFactory() == null) {
      expandToInclude(boundingBox);
    } else if (boundingBox.getGeometryFactory().equals(geometryFactory)) {
      this.geometryFactory = boundingBox.getGeometryFactory();
      expandToInclude(boundingBox);
    } else {
      final Polygon polygon = boundingBox.toPolygon();
      final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
        boundingBox.getGeometryFactory(), geometryFactory);
      if (operation != null) {
        final Polygon projectedPolygon = operation.perform(polygon);
        final BoundingBox envelope = projectedPolygon.getBoundingBox();
        expandToInclude(envelope);
      } else {
        expandToInclude(boundingBox);
      }
    }
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param coordinate The coordinate.
   */
  public Envelope(final GeometryFactory geometryFactory, final Coordinates point) {
    this.geometryFactory = geometryFactory;
    if (point != null) {
      this.bounds = EnvelopeUtil.createBounds(point);
    }

  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param point1 The first coordinate.
   * @param point2 The second coordinate.
   */
  public Envelope(final GeometryFactory geometryFactory,
    final Coordinates point1, final Coordinates point2) {
    this.geometryFactory = geometryFactory;
    if (point1 == null) {
      if (point2 != null) {
        this.bounds = EnvelopeUtil.createBounds(geometryFactory, point2);
      }
    } else if (point2 == null) {
      this.bounds = EnvelopeUtil.createBounds(geometryFactory, point1);
    } else {
      final int numAxis = Math.max(point1.getNumAxis(), point2.getNumAxis());
      this.bounds = EnvelopeUtil.createBounds(geometryFactory, numAxis, point1);
      EnvelopeUtil.expand(geometryFactory, bounds, point2);
    }

  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param x The x value.
   * @param y The y value.
   */
  public Envelope(final GeometryFactory geometryFactory, final double x,
    final double y) {
    this(geometryFactory, x, y, x, y);
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   */
  public Envelope(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double x2, final double y2) {
    this.geometryFactory = geometryFactory;
    this.bounds = EnvelopeUtil.createBounds(geometryFactory, x1, y1);
    EnvelopeUtil.expand(geometryFactory, bounds, x2, y2);
  }

  public Envelope(final GeometryFactory geometryFactory, final double[] bounds) {
    this.geometryFactory = geometryFactory;
    this.bounds = bounds;
  }

  /**
   * Construct a new Bounding Box.
   * 
   * @param geometryFactory The geometry factory.
   */
  public Envelope(final int srid) {
    this.geometryFactory = GeometryFactory.getFactory(srid);
  }

  public Envelope(final Point p1, final Point p2) {
    this(GeometryFactory.getFactory(p1), p1, p1);
  }

  /**
   * Computes the coordinate of the centre of this envelope (as long as it is non-null
   *
   * @return the centre coordinate of this envelope
   * <code>null</code> if the envelope is null
   */
  @Override
  public Coordinates centre() {
    if (isNull()) {
      return null;
    }
    return new Coordinate((getMinX() + getMaxX()) / 2.0,
      (getMinY() + getMaxY()) / 2.0, Coordinates.NULL_ORDINATE);
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

  @Override
  public BoundingBox clone() {
    try {
      return (BoundingBox)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Tests if the <code>Envelope other</code>
   * lies wholely inside this <code>Envelope</code> (inclusive of the boundary).
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  other the <code>Envelope</code> to check
   *@return true if <code>other</code> is contained in this <code>Envelope</code>
   *
   *@see #covers(Envelope)
   */
  @Override
  public boolean contains(final BoundingBox other) {
    return covers(other);
  }

  /**
   * Tests if the given point lies in or on the envelope.
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  p  the point which this <code>Envelope</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>Envelope</code>.
   *      
   *@see #covers(Coordinates)
   */
  @Override
  public boolean contains(final Coordinates p) {
    return covers(p);
  }

  /**
   * Tests if the given point lies in or on the envelope.
   * <p>
   * Note that this is <b>not</b> the same definition as the SFS <tt>contains</tt>,
   * which would exclude the envelope boundary.
   *
   *@param  x  the x-coordinate of the point which this <code>Envelope</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>Envelope</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>Envelope</code>.
   *      
   *@see #covers(double, double)
   */
  @Override
  public boolean contains(final double x, final double y) {
    return covers(x, y);
  }

  @Override
  public boolean contains(final Geometry geometry) {
    if (geometry == null) {
      return false;
    } else {
      final BoundingBox boundingBox = getBoundingBox(geometry);
      return contains(boundingBox);
    }
  }

  @Override
  public boolean contains(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point projectedPoint = point.convert(geometryFactory);
    final boolean contains = contains(projectedPoint);
    return contains;
  }

  @Override
  public BoundingBox convert(final GeometryFactory geometryFactory) {
    final GeometryFactory factory = getGeometryFactory();
    if (factory == null || geometryFactory == null
      || factory.equals(geometryFactory) || isEmpty()) {
      return this;
    } else {
      final CoordinatesOperation operation = ProjectionFactory.getCoordinatesOperation(
        factory, geometryFactory);
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

        final double minX = getMinX();
        final double maxX = getMaxX();
        final double minY = getMinY();
        final double maxY = getMaxY();
        final Envelope boundingBox = new Envelope(geometryFactory);
        final double[] from = new double[2];
        final double[] to = new double[2];
        expand(boundingBox, operation, from, to, minX, minY);
        expand(boundingBox, operation, from, to, minX, maxY);
        expand(boundingBox, operation, from, to, minX, minY);
        expand(boundingBox, operation, from, to, maxX, minY);

        if (xStep != 0) {
          for (double x = minX + xStep; x < maxX; x += xStep) {
            expand(boundingBox, operation, from, to, x, minY);
            expand(boundingBox, operation, from, to, x, maxY);
          }
        }
        if (yStep != 0) {
          for (double y = minY + yStep; y < maxY; y += yStep) {
            expand(boundingBox, operation, from, to, minX, y);
            expand(boundingBox, operation, from, to, maxX, y);
          }
        }
        return boundingBox;
      } else {
        return this;
      }
    }
  }

  /**
   * Tests if the <code>Envelope other</code>
   * lies wholely inside this <code>Envelope</code> (inclusive of the boundary).
   *
   *@param  other the <code>Envelope</code> to check
   *@return true if this <code>Envelope</code> covers the <code>other</code> 
   */
  @Override
  public boolean covers(final BoundingBox other) {
    if (other == null || isNull() || other.isNull()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return other.getMinX() >= minX && other.getMaxX() <= maxX
        && other.getMinY() >= minY && other.getMaxY() <= maxY;
    }
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  p  the point which this <code>Envelope</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>Envelope</code>.
   */
  @Override
  public boolean covers(final Coordinates p) {
    final double x = p.getX();
    final double y = p.getY();
    return covers(x, y);
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  x  the x-coordinate of the point which this <code>Envelope</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>Envelope</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>Envelope</code>.
   */
  @Override
  public boolean covers(final double x, final double y) {
    if (isNull()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
  }

  /**
   * Computes the distance between this and another
   * <code>Envelope</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  @Override
  public double distance(BoundingBox boundingBox) {
    boundingBox = boundingBox.convert(getGeometryFactory());
    if (intersects(boundingBox)) {
      return 0;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      double dx = 0.0;
      if (maxX < boundingBox.getMinX()) {
        dx = boundingBox.getMinX() - maxX;
      } else if (minX > boundingBox.getMaxX()) {
        dx = minX - boundingBox.getMaxX();
      }

      double dy = 0.0;
      if (maxY < boundingBox.getMinY()) {
        dy = boundingBox.getMinY() - maxY;
      } else if (minY > boundingBox.getMaxY()) {
        dy = minY - boundingBox.getMaxY();
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
  public double distance(final Geometry geometry) {
    final BoundingBox boundingBox = getBoundingBox(geometry);
    return distance(boundingBox);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      if (isEmpty()) {
        return boundingBox.isEmpty();
      } else if (boundingBox.isEmpty()) {
        return false;
      } else if (getSrid() == boundingBox.getSrid()) {
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

  @Override
  public BoundingBox expand(final Coordinates coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return new Envelope(geometryFactory, coordinates);
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
      return new Envelope(geometryFactory, minX, minY, maxX, maxY);
    }
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
    if (isEmpty() || (deltaX == 0 && deltaY == 0)) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double x1 = getMinX() - deltaX;
      final double x2 = getMaxX() + deltaX;
      final double y1 = getMinY() - deltaY;
      final double y2 = getMaxY() + deltaY;

      if (x1 > x2 || y1 > y2) {
        return new Envelope(geometryFactory);
      } else {
        return new Envelope(geometryFactory, x1, y1, x2, y2);
      }
    }
  }

  private void expand(final Envelope boundingBox,
    final CoordinatesOperation operation, final double[] from,
    final double[] to, final double x, final double y) {

    from[0] = x;
    from[1] = y;
    operation.perform(2, from, 2, to);
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final double newX = geometryFactory.makeXyPrecise(to[0]);
    final double newY = geometryFactory.makeXyPrecise(to[1]);
    boundingBox.expandToInclude(newX, newY);
  }

  /**
   * Expands this envelope by a given distance in all directions.
   * Both positive and negative distances are supported.
   *
   * @param distance the distance to expand the envelope
   */
  public void expandBy(final double distance) {
    expandBy(distance, distance);
  }

  /**
   * Expands this envelope by a given distance in all directions.
   * Both positive and negative distances are supported.
   *
   * @param deltaX the distance to expand the envelope along the the X axis
   * @param deltaY the distance to expand the envelope along the the Y axis
   */
  public void expandBy(final double deltaX, final double deltaY) {
    if (!isNull()) {
      this.bounds[0] -= deltaX;
      this.bounds[getNumAxis()] += deltaX;
      this.bounds[1] -= deltaY;
      this.bounds[getNumAxis() + 1] += deltaY;

    }

    // check for envelope disappearing
    if (getMinX() > getMaxX() || getMinY() > getMaxY()) {
      setToNull();
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
    if (other.isEmpty()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedOther = other.convert(geometryFactory);
      if (isEmpty()) {
        return convertedOther;
      } else if (contains(convertedOther)) {
        return this;
      } else {
        final double minX = Math.min(getMinX(), convertedOther.getMinX());
        final double maxX = Math.max(getMaxX(), convertedOther.getMaxX());
        final double minY = Math.min(getMinY(), convertedOther.getMinY());
        final double maxY = Math.max(getMaxY(), convertedOther.getMaxY());
        return new Envelope(geometryFactory, minX, minY, maxX, maxY);
      }
    }
  }

  /**
   *  Enlarges this <code>Envelope</code> so that it contains
   *  the given {@link Coordinates}. 
   *  Has no effect if the point is already on or within the envelope.
   *
   *@param  p  the Coordinates to expand to include
   */
  public void expandToInclude(final Coordinates p) {
    expandToInclude(p.getX(), p.getY());
  }

  @Override
  public BoundingBox expandToInclude(final DataObject object) {
    if (object != null) {
      final Geometry geometry = object.getGeometryValue();
      return expandToInclude(geometry);
    }
    return this;
  }

  /**
   *  Enlarges this <code>Envelope</code> so that it contains
   *  the given point. 
   *  Has no effect if the point is already on or within the envelope.
   *
   *@param  x  the value to lower the minimum x to or to raise the maximum x to
   *@param  y  the value to lower the minimum y to or to raise the maximum y to
   */
  public void expandToInclude(final double x, final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isNull()) {
      this.bounds = EnvelopeUtil.createBounds(geometryFactory, x, y);
    } else {
      EnvelopeUtil.expand(geometryFactory, bounds, x, y);
    }
  }

  /**
   *  Enlarges this <code>Envelope</code> so that it contains
   *  the <code>other</code> Envelope. 
   *  Has no effect if <code>other</code> is wholly on or
   *  within the envelope.
   *
   *@param  other  the <code>Envelope</code> to expand to include
   */
  public void expandToInclude(final Envelope other) {
    if (!other.isNull()) {
      if (isNull()) {
        this.bounds = other.getBounds();
      } else {
        for (int axisIndex = 0; axisIndex < Math.min(other.getNumAxis(),
          getNumAxis()); axisIndex++) {
          final double min = other.getMin(axisIndex);
          final GeometryFactory geometryFactory = getGeometryFactory();
          EnvelopeUtil.expand(geometryFactory, bounds, axisIndex, min);
          final double max = other.getMax(axisIndex);
          EnvelopeUtil.expand(geometryFactory, bounds, axisIndex, max);
        }
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

  /**
   * Gets the area of this envelope.
   * 
   * @return the area of the envelope
   * @return 0.0 if the envelope is null
   */
  @Override
  public double getArea() {
    return getWidth() * getHeight();
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

  public Point getBottomLeftPoint() {
    return getGeometryFactory().point(getMinX(), getMinY());
  }

  public Point getBottomRightPoint() {
    return getGeometryFactory().point(getMaxX(), getMinY());
  }

  @Override
  public double[] getBounds() {
    if (bounds == null) {
      return bounds;
    } else {
      return bounds.clone();
    }
  }

  @Override
  public Coordinates getCentre() {
    return new DoubleCoordinates(getCentreX(), getCentreY());
  }

  @Override
  public Point getCentrePoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(getCentre());
  }

  @Override
  public double getCentreX() {
    return getMinX() + (getWidth() / 2);
  }

  @Override
  public double getCentreY() {
    return getMinY() + (getHeight() / 2);
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

  @Override
  public Coordinates getCornerPoint(int index) {
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    index = index % 4;
    switch (index) {
      case 0:
        return new DoubleCoordinates(maxX, minY);
      case 1:
        return new DoubleCoordinates(minX, minY);
      case 2:
        return new DoubleCoordinates(minX, maxY);
      default:
        return new DoubleCoordinates(maxX, maxY);
    }
  }

  @Override
  public CoordinatesList getCornerPoints() {
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    return new DoubleCoordinatesList(2, maxX, minY, minX, minY, minX, maxY,
      maxX, maxY);
  }

  @Override
  public LineSegment getEastLine() {
    return new LineSegment(getGeometryFactory(), getMaxX(), getMinY(),
      getMaxX(), getMaxY());
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  /**
   *  Returns the difference between the maximum and minimum y values.
   *
   *@return    max y - min y, or 0 if this is a null <code>Envelope</code>
   */
  @Override
  public double getHeight() {
    if (isNull()) {
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
  public String getId() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final String string = MathUtil.toString(getMinX()) + "_"
      + MathUtil.toString(getMinY()) + "_" + MathUtil.toString(getMaxX()) + "_"
      + MathUtil.toString(getMaxY());
    if (geometryFactory == null) {
      return string;
    } else {
      return geometryFactory.getSrid() + "-" + string;
    }
  }

  public double getMax(final int axisIndex) {
    if (bounds == null) {
      return Double.NaN;
    } else {
      return EnvelopeUtil.getMax(bounds, axisIndex);
    }
  }

  @Override
  public <Q extends Quantity> Measurable<Q> getMaximumX() {
    final Unit<Q> unit = getUnit();
    return Measure.valueOf(this.getMaxX(), unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMaximumX(final Unit convertUnit) {
    return getMaximumX().doubleValue(convertUnit);
  }

  @Override
  public <Q extends Quantity> Measurable<Q> getMaximumY() {
    final Unit<Q> unit = getUnit();
    return Measure.valueOf(this.getMaxY(), unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMaximumY(final Unit convertUnit) {
    return getMaximumY().doubleValue(convertUnit);
  }

  /**
   *  Returns the <code>Envelope</code>s maximum x-value. min x > max x
   *  indicates that this is a null <code>Envelope</code>.
   *
   *@return    the maximum x-coordinate
   */
  @Override
  public double getMaxX() {
    return getMax(0);
  }

  /**
   *  Returns the <code>Envelope</code>s maximum y-value. min y > max y
   *  indicates that this is a null <code>Envelope</code>.
   *
   *@return    the maximum y-coordinate
   */
  @Override
  public double getMaxY() {
    return getMax(1);
  }

  @Override
  public double getMaxZ() {
    return getMax(2);
  }

  public double getMin(final int axisIndex) {
    if (bounds == null) {
      return Double.NaN;
    } else {
      return EnvelopeUtil.getMin(bounds, axisIndex);
    }
  }

  @Override
  public <Q extends Quantity> Measurable<Q> getMinimumX() {
    final Unit<Q> unit = getUnit();
    return Measure.valueOf(this.getMinX(), unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMinimumX(final Unit convertUnit) {
    return getMinimumX().doubleValue(convertUnit);
  }

  @Override
  public <Q extends Quantity> Measurable<Q> getMinimumY() {
    final Unit<Q> unit = getUnit();
    return Measure.valueOf(this.getMinY(), unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public <Q extends Quantity> double getMinimumY(final Unit convertUnit) {
    return getMinimumY().doubleValue(convertUnit);
  }

  /**
   *  Returns the <code>Envelope</code>s minimum x-value. min x > max x
   *  indicates that this is a null <code>Envelope</code>.
   *
   *@return    the minimum x-coordinate
   */
  @Override
  public double getMinX() {
    return getMin(0);
  }

  /**
   *  Returns the <code>Envelope</code>s minimum y-value. min y > max y
   *  indicates that this is a null <code>Envelope</code>.
   *
   *@return    the minimum y-coordinate
   */
  @Override
  public double getMinY() {
    return getMin(1);
  }

  @Override
  public double getMinZ() {
    return getMin(2);
  }

  @Override
  public LineSegment getNorthLine() {
    return new LineSegment(getGeometryFactory(), getMinX(), getMaxY(),
      getMaxX(), getMaxY());
  }

  public int getNumAxis() {
    if (bounds == null) {
      return 0;
    } else {
      return bounds.length / 2;
    }
  }

  @Override
  public LineSegment getSouthLine() {
    return new LineSegment(getGeometryFactory(), getMinX(), getMinY(),
      getMaxX(), getMinY());
  }

  @Override
  public int getSrid() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return -1;
    } else {
      return geometryFactory.getSrid();
    }
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

  @Override
  public LineSegment getWestLine() {
    return new LineSegment(getGeometryFactory(), getMinX(), getMinY(),
      getMinX(), getMaxY());
  }

  /**
   *  Returns the difference between the maximum and minimum x values.
   *
   *@return    max x - min x, or 0 if this is a null <code>Envelope</code>
   */
  @Override
  public double getWidth() {
    if (isNull()) {
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
    result = 37 * result + CoordinatesUtil.hashCode(minX);
    result = 37 * result + CoordinatesUtil.hashCode(maxX);
    result = 37 * result + CoordinatesUtil.hashCode(minY);
    result = 37 * result + CoordinatesUtil.hashCode(maxY);
    return result;
  }

  /**
   * Computes the intersection of two {@link Envelope}s.
   *
   * @param env the envelope to intersect with
   * @return a new BoundingBox representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  @Override
  public BoundingBox intersection(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    if (isEmpty() || convertedBoundingBox.isEmpty()
      || !intersects(convertedBoundingBox)) {
      return new Envelope(geometryFactory);
    } else {
      final double intMinX = Math.max(getMinX(), convertedBoundingBox.getMinX());
      final double intMinY = Math.max(getMinY(), convertedBoundingBox.getMinY());
      final double intMaxX = Math.min(getMaxX(), convertedBoundingBox.getMaxX());
      final double intMaxY = Math.min(getMaxY(), convertedBoundingBox.getMaxY());
      return new Envelope(geometryFactory, intMinX, intMinY, intMaxX, intMaxY);
    }
  }

  /**
   *  Check if the region defined by <code>other</code>
   *  overlaps (intersects) the region of this <code>Envelope</code>.
   *
   *@param  other  the <code>Envelope</code> which this <code>Envelope</code> is
   *          being checked for overlapping
   *@return        <code>true</code> if the <code>Envelope</code>s overlap
   */
  @Override
  public boolean intersects(final BoundingBox other) {
    if (isNull() || other.isNull()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = other.convert(geometryFactory);
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return !(convertedBoundingBox.getMinX() > maxX
        || convertedBoundingBox.getMaxX() < minX
        || convertedBoundingBox.getMinY() > maxY || convertedBoundingBox.getMaxY() < minY);
    }
  }

  /**
   *  Check if the point <code>p</code>
   *  overlaps (lies inside) the region of this <code>Envelope</code>.
   *
   *@param  p  the <code>Coordinate</code> to be tested
   *@return        <code>true</code> if the point overlaps this <code>Envelope</code>
   */
  @Override
  public boolean intersects(final Coordinates point) {
    if (point == null) {
      return false;
    } else {
      final double x = point.getX();
      final double y = point.getY();
      return this.intersects(x, y);
    }
  }

  public boolean intersects(final DataObject record) {
    final BoundingBox boundingBox = getBoundingBox(record);
    return intersects(boundingBox);
  }

  /**
   *  Check if the point <code>(x, y)</code>
   *  overlaps (lies inside) the region of this <code>Envelope</code>.
   *
   *@param  x  the x-ordinate of the point
   *@param  y  the y-ordinate of the point
   *@return        <code>true</code> if the point overlaps this <code>Envelope</code>
   */
  @Override
  public boolean intersects(final double x, final double y) {
    if (isNull()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      return !(x > maxX || x < minX || y > maxY || y < minY);
    }
  }

  public boolean intersects(final Geometry geometry) {
    final BoundingBox boundingBox = getBoundingBox(geometry);
    return intersects(boundingBox);
  }

  @Override
  public boolean isEmpty() {
    return isNull();
  }

  /**
   *  Returns <code>true</code> if this <code>Envelope</code> is a "null"
   *  envelope.
   *
   *@return    <code>true</code> if this <code>Envelope</code> is uninitialized
   *      or is the envelope of the empty geometry.
   */
  @Override
  public boolean isNull() {
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

  /**
   * Gets the maximum extent of this envelope across both dimensions.
   * 
   * @return the maximum extent of this envelope
   */
  @Override
  public double maxExtent() {
    if (isNull()) {
      return 0.0;
    }
    final double w = getWidth();
    final double h = getHeight();
    if (w > h) {
      return w;
    }
    return h;
  }

  /**
   * Gets the minimum extent of this envelope across both dimensions.
   * 
   * @return the minimum extent of this envelope
   */
  @Override
  public double minExtent() {
    if (isNull()) {
      return 0.0;
    }
    final double w = getWidth();
    final double h = getHeight();
    if (w < h) {
      return w;
    }
    return h;
  }

  /**
   * <p>Create a new BoundingBox by moving the min/max x coordinates by xDisplacement and
   * the min/max y coordinates by yDisplacement. If the bounding box is null or the xDisplacement
   * and yDisplacement are 0 then this bounding box will be returned.</p>
   * 
   * @param xDisplacement The distance to move the min/max x coordinates.
   * @param yDisplacement The distance to move the min/max y coordinates.
   * @return The moved bounding box.
   */
  @Override
  public BoundingBox move(final double xDisplacement, final double yDisplacement) {
    if (isEmpty() || (xDisplacement == 0 && yDisplacement == 0)) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double x1 = getMinX() + xDisplacement;
      final double x2 = getMaxX() + xDisplacement;
      final double y1 = getMinY() + yDisplacement;
      final double y2 = getMaxY() + yDisplacement;
      return new Envelope(geometryFactory, x1, y1, x2, y2);
    }
  }

  /**
   *  Makes this <code>Envelope</code> a "null" envelope, that is, the envelope
   *  of the empty geometry.
   */
  protected void setToNull() {
    this.bounds = null;
  }

  @Override
  public Geometry toGeometry() {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    final double width = getWidth();
    final double height = getHeight();
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (width == 0 && height == 0) {
      return geometryFactory.point(new DoubleCoordinatesList(2, minX, minY));
    } else if (width == 0 || height == 0) {
      return geometryFactory.lineString(new DoubleCoordinatesList(2, minX,
        minY, maxX, maxY));
    } else {
      return geometryFactory.polygon(new DoubleCoordinatesList(2, minX, minY,
        minX, maxY, maxX, maxY, maxX, minY, minX, minY));
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
  public Polygon toPolygon(final GeometryFactory geometryFactory, int numX,
    int numY) {
    if (isEmpty()) {
      return geometryFactory.polygon();
    } else {
      final GeometryFactory factory = getGeometryFactory();
      try {
        double minStep;
        if (geometryFactory.getCoordinateSystem() instanceof ProjectedCoordinateSystem) {
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
        CoordinatesList coordinates = new DoubleCoordinatesList(numCoordinates,
          2);
        int i = 0;

        coordinates.setX(i, maxX);
        coordinates.setY(i, minY);
        i++;
        for (int j = 0; j < numX - 1; j++) {
          coordinates.setX(i, maxX - j * xStep);
          coordinates.setY(i, minY);
          i++;
        }
        coordinates.setX(i, minX);
        coordinates.setY(i, minY);
        i++;

        for (int j = 0; j < numY - 1; j++) {
          coordinates.setX(i, minX);
          coordinates.setY(i, minY + j * yStep);
          i++;
        }
        coordinates.setX(i, minX);
        coordinates.setY(i, maxY);
        i++;

        for (int j = 0; j < numX - 1; j++) {
          coordinates.setX(i, minX + j * xStep);
          coordinates.setY(i, maxY);
          i++;
        }

        coordinates.setX(i, maxX);
        coordinates.setY(i, maxY);
        i++;

        for (int j = 0; j < numY - 1; j++) {
          coordinates.setX(i, maxX);
          coordinates.setY(i, minY + (numY - j) * yStep);
          i++;
        }
        coordinates.setX(i, maxX);
        coordinates.setY(i, minY);

        if (geometryFactory != factory) {
          coordinates = CoordinatesListProjectionUtil.perform(coordinates,
            factory.getCoordinateSystem(),
            geometryFactory.getCoordinateSystem());
        }

        final Polygon polygon = geometryFactory.polygon(coordinates);
        return polygon;
      } catch (final IllegalArgumentException e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to convert to polygon: " + this, e);
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
    final GeometryFactory geometryFactory = getGeometryFactory();
    final StringBuffer s = new StringBuffer();
    if (geometryFactory != null) {
      s.append("SRID=");
      s.append(geometryFactory.getSrid());
      s.append(";");
    }
    if (isEmpty()) {
      s.append("BBOX EMPTY");
    } else {
      s.append("BBOX(");
      s.append(StringConverterRegistry.toString(getMinX()));
      s.append(',');
      s.append(StringConverterRegistry.toString(getMinY()));
      s.append(' ');
      s.append(StringConverterRegistry.toString(getMaxX()));
      s.append(',');
      s.append(StringConverterRegistry.toString(getMaxY()));
      s.append(')');
    }
    return s.toString();
  }

}
