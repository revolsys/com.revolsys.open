package com.revolsys.geometry.model;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXYGeometryFactory;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.PointDoubleGf;
import com.revolsys.geometry.model.impl.RectangleXY;
import com.revolsys.geometry.util.OutCode;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.logging.Logs;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.wkt.WktParser;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;
import com.revolsys.util.QuantityType;
import com.revolsys.util.function.BiConsumerDouble;
import com.revolsys.util.function.Consumer3;
import com.revolsys.util.function.Consumer4Double;
import com.revolsys.util.number.Doubles;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public interface BoundingBox
  extends BoundingBoxProxy, Emptyable, GeometryFactoryProxy, Cloneable, Serializable {
  static BoundingBox empty() {
    return GeometryFactory.DEFAULT_3D.newBoundingBoxEmpty();
  }

  static boolean isEmpty(final double minX, final double maxX) {
    if (Double.isNaN(minX)) {
      return true;
    } else if (Double.isNaN(maxX)) {
      return true;
    } else {
      return maxX < minX;
    }
  }

  static <V> List<V> newArray(final BiConsumer<BoundingBoxProxy, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy) {
    final List<V> values = new ArrayList<>();
    if (boundingBoxProxy != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      forEachFunction.accept(boundingBox, values::add);
    }
    return values;
  }

  static <V> List<V> newArray(
    final Consumer3<BoundingBoxProxy, Predicate<? super V>, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy, final Predicate<? super V> filter) {
    final List<V> values = new ArrayList<>();
    if (boundingBoxProxy != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      forEachFunction.accept(boundingBox, filter, values::add);
    }
    return values;
  }

  static <V> List<V> newArraySorted(final BiConsumer<BoundingBoxProxy, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy) {
    return newArraySorted(forEachFunction, boundingBoxProxy, null);
  }

  static <V> List<V> newArraySorted(final BiConsumer<BoundingBoxProxy, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy, final Comparator<V> comparator) {
    final List<V> values = newArray(forEachFunction, boundingBoxProxy);
    values.sort(comparator);
    return values;
  }

  static <V> List<V> newArraySorted(
    final Consumer3<BoundingBoxProxy, Predicate<? super V>, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy, final Predicate<? super V> filter) {
    return newArraySorted(forEachFunction, boundingBoxProxy, filter, null);
  }

  static <V> List<V> newArraySorted(
    final Consumer3<BoundingBoxProxy, Predicate<? super V>, Consumer<V>> forEachFunction,
    final BoundingBoxProxy boundingBoxProxy, final Predicate<? super V> filter,
    final Comparator<V> comparator) {
    final List<V> values = newArray(forEachFunction, boundingBoxProxy, filter);
    values.sort(comparator);
    return values;
  }

  static BoundingBox newBoundingBox(final Object value) {
    if (value == null) {
      return empty();
    } else if (value instanceof BoundingBox) {
      return (BoundingBox)value;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return geometry.getBoundingBox();
    } else {
      final String string = DataTypes.toString(value);
      return BoundingBox.newBoundingBox(string);
    }
  }

  static BoundingBox newBoundingBox(final String wkt) {
    if (Property.hasValue(wkt)) {
      try {
        GeometryFactory geometryFactory = null;
        final PushbackReader reader = new PushbackReader(new StringReader(wkt), 20);
        WktParser.skipWhitespace(reader);
        if (WktParser.hasText(reader, "SRID=")) {
          final Integer srid = WktParser.parseInteger(reader);
          if (srid != null) {
            geometryFactory = GeometryFactory.floating2d(srid);
          }
          WktParser.hasText(reader, ";");
        }
        if (WktParser.hasText(reader, "BBOX(")) {
          final double x1 = WktParser.parseDouble(reader);
          if (WktParser.hasText(reader, ",")) {
            WktParser.skipWhitespace(reader);
            final double y1 = WktParser.parseDouble(reader);
            WktParser.skipWhitespace(reader);
            final double x2 = WktParser.parseDouble(reader);
            if (WktParser.hasText(reader, ",")) {
              WktParser.skipWhitespace(reader);
              final double y2 = WktParser.parseDouble(reader);
              return geometryFactory.newBoundingBox(x1, y1, x2, y2);
            } else {
              throw new IllegalArgumentException(
                "Expecting a ',' not " + FileUtil.getString(reader, 50));
            }

          } else {
            throw new IllegalArgumentException("Expecting a ',' not " + FileUtil.getString(reader));
          }
        } else if (WktParser.hasText(reader, "BBOX EMPTY")) {
          if (geometryFactory == null) {
            return BoundingBox.empty();
          } else {
            return geometryFactory.newBoundingBoxEmpty();
          }
        }
      } catch (final IOException e) {
        throw Exceptions.wrap("Error reading WKT:" + wkt, e);
      }
    }

    return empty();
  }

  static String toString(final BoundingBox boundingBox) {
    final StringBuilder s = new StringBuilder();
    final int srid = boundingBox.getHorizontalCoordinateSystemId();
    if (srid > 0) {
      s.append("SRID=");
      s.append(srid);
      s.append(";");
    }
    if (boundingBox.isEmpty()) {
      s.append("BBOX EMPTY");
    } else {
      s.append("BBOX");
      final int axisCount = boundingBox.getAxisCount();
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
        s.append(Doubles.toString(boundingBox.getMin(axisIndex)));
      }
      s.append(' ');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(',');
        }
        s.append(Doubles.toString(boundingBox.getMax(axisIndex)));
      }
      s.append(')');
    }
    return s.toString();
  }

  /**
   * If the coordinate system is a projected coordinate system then clip to the {@link CoordinateSystem#getAreaBoundingBox()}.
   */
  default BoundingBox clipToCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
    if (coordinateSystem == null || coordinateSystem instanceof GeographicCoordinateSystem) {
      return this;
    } else {
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      return intersection(areaBoundingBox);
    }
  }

  default BoundingBox clone() {
    return this;
  }

  /**
   * Check that geom is not contained entirely in the rectangle boundary.
   * According to the somewhat odd spec of the SFS, if this
   * is the case the geometry is NOT contained.
   */
  default boolean containsSFS(final Geometry geometry) {
    final BoundingBox boundingBox2 = geometry.getBoundingBox();
    if (covers(boundingBox2)) {
      if (geometry.isContainedInBoundary(this)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  default BoundingBox convert(final GeometryFactory geometryFactory) {
    final GeometryFactory factory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.newBoundingBoxEmpty();
    } else if (geometryFactory == null) {
      return this;
    } else if (factory == geometryFactory) {
      return this;
    } else {
      if (factory == null || factory.getHorizontalCoordinateSystem() == null) {
        final int axisCount = Math.min(geometryFactory.getAxisCount(), getAxisCount());
        final double[] minMaxValues = getMinMaxValues(axisCount);
        return geometryFactory.newBoundingBox(axisCount, minMaxValues);
      } else if (isEmpty()) {
        return newBoundingBoxEmpty();
      } else {
        final CoordinatesOperation operation = factory.getCoordinatesOperation(geometryFactory);
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

          final double[] bounds = getMinMaxValues(2);
          bounds[0] = Double.NaN;
          bounds[1] = Double.NaN;
          bounds[2] = Double.NaN;
          bounds[3] = Double.NaN;

          final CoordinatesOperationPoint to = new CoordinatesOperationPoint();
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
          return geometryFactory.newBoundingBox(2, bounds);
        } else {
          return this;
        }
      }
    }
  }

  default BoundingBox convert(GeometryFactory geometryFactory, final int axisCount) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null || sourceGeometryFactory == null) {
      return this;
    } else {
      geometryFactory = geometryFactory.convertAxisCount(axisCount);
      boolean copy = false;
      if (geometryFactory != null && sourceGeometryFactory != geometryFactory) {
        final int srid = getHorizontalCoordinateSystemId();
        final int srid2 = geometryFactory.getHorizontalCoordinateSystemId();
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
            if (!Doubles.equal(scale, scale1)) {
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

  default boolean coveredBy(final double... bounds) {
    final double minX1 = bounds[0];
    final double minY1 = bounds[1];
    final double maxX1 = bounds[2];
    final double maxY1 = bounds[3];
    final double minX2 = getMinX();
    final double minY2 = getMinY();
    final double maxX2 = getMaxX();
    final double maxY2 = getMaxY();
    return RectangleUtil.covers(minX1, minY1, maxX1, maxY1, minX2, minY2, maxX2, maxY2);
  }

  /**
   * Tests if the <code>BoundingBox other</code>
   * lies wholely inside this <code>BoundingBox</code> (inclusive of the boundary).
   *
   *@param  other the <code>BoundingBox</code> to check
   *@return true if this <code>BoundingBox</code> covers the <code>other</code>
   */
  default boolean covers(final BoundingBox other) {
    if (other == null || isEmpty() || other.isEmpty()) {
      return false;
    } else {
      final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
      final BoundingBox convertedBoundingBox = other.toCoordinateSystem(coordinateSystem, 2);
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return convertedBoundingBox.coveredBy(minX, minY, maxX, maxY);
    }
  }

  /**
   * Tests if the given point lies in or on the envelope.
   *
   *@param  x  the x-coordinate of the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@param  y  the y-coordinate of the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if <code>(x, y)</code> lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   */
  default boolean covers(final double x, final double y) {
    if (isEmpty()) {
      return false;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();

      return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
  }

  default boolean covers(final Geometry geometry) {
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
   *@param  p  the point which this <code>BoundingBox</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>BoundingBox</code>.
   */
  default boolean covers(final Point point) {
    if (point == null || point.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point projectedPoint = point.convertGeometry(geometryFactory);
      final double x = projectedPoint.getX();
      final double y = projectedPoint.getY();
      return covers(x, y);
    }
  }

  /**
   * Computes the distance between this and another
   * <code>BoundingBox</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  default double distance(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox.convert(geometryFactory);

    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();

    return distance(minX, minY, maxX, maxY);
  }

  default double distance(final double minX, final double minY, final double maxX,
    final double maxY) {
    final double minXThis = getMinX();
    final double minYThis = getMinY();
    final double maxXThis = getMaxX();
    final double maxYThis = getMaxY();

    if (isEmpty(minXThis, maxXThis) || isEmpty(minX, maxX)) {
      // Empty
      return Double.MAX_VALUE;
    } else if (!(minX > maxXThis || maxX < minXThis || minY > maxYThis || maxY < minYThis)) {
      // Intersects
      return 0;
    } else {
      double dx;
      if (maxXThis < minX) {
        dx = minX - maxXThis;
      } else {
        if (minXThis > maxX) {
          dx = minXThis - maxX;
        } else {
          dx = 0;
        }
      }

      double dy;
      if (maxYThis < minY) {
        dy = minY - maxYThis;
      } else if (minYThis > maxY) {
        dy = minYThis - maxY;
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

  default double distance(final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return distance(boundingBox);
  }

  /**
   * Computes the distance between this and another
   * <code>BoundingBox</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  default double distance(Point point) {
    point = point.convertGeometry(getGeometryFactory());
    final double x = point.getX();
    final double y = point.getY();
    if (intersects(x, y)) {
      return 0;
    } else {

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

  default boolean equals(final BoundingBox boundingBox) {
    if (isEmpty()) {
      return boundingBox.isEmpty();
    } else if (boundingBox.isEmpty()) {
      return false;
    } else {
      final int csId1 = getHorizontalCoordinateSystemId();
      final int csId2 = boundingBox.getHorizontalCoordinateSystemId();
      if (csId1 == csId2 || csId1 < 1 || csId2 < 1) {
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
  default BoundingBox expand(final double delta) {
    return expand(delta, delta);
  }

  /**
   * Return a new bounding box expanded by deltaX, deltaY.
   *
   * @param delta
   * @return
   */
  default BoundingBox expand(final double deltaX, final double deltaY) {
    if (isEmpty() || deltaX == 0 && deltaY == 0) {
      return this;
    } else {
      final double x1 = getMinX() - deltaX;
      final double x2 = getMaxX() + deltaX;
      final double y1 = getMinY() - deltaY;
      final double y2 = getMaxY() + deltaY;

      if (x1 > x2 || y1 > y2) {
        return newBoundingBoxEmpty();
      } else {
        return newBoundingBox(x1, y1, x2, y2);
      }
    }
  }

  default void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final CoordinatesOperation operation, final CoordinatesOperationPoint point, final double x,
    final double y) {
    point.setPoint(x, y);
    operation.perform(point);
    RectangleUtil.expand(geometryFactory, bounds, point.x, point.y);
  }

  default BoundingBox expand(final Point point) {
    if (isEmpty()) {
      return point.newBoundingBox();
    } else {
      final double x = point.getX();
      final double y = point.getY();

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
      return newBoundingBox(minX, minY, maxX, maxY);
    }
  }

  default BoundingBox expandPercent(final double factor) {
    return expandPercent(factor, factor);
  }

  default BoundingBox expandPercent(final double factorX, final double factorY) {
    if (isEmpty()) {
      return this;
    } else {
      final double deltaX = getWidth() * factorX / 2;
      final double deltaY = getHeight() * factorY / 2;
      return expand(deltaX, deltaY);
    }
  }

  default BoundingBox expandToInclude(final BoundingBox other) {
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
        return newBoundingBox(minX, minY, maxX, maxY);
      }
    }
  }

  default BoundingBox expandToInclude(final double... coordinates) {
    if (coordinates == null || coordinates.length < 2) {
      return this;
    } else {
      final double[] bounds;
      final int axisCount = getAxisCount();
      if (isEmpty()) {
        bounds = RectangleUtil.newBounds(axisCount);
      } else {
        bounds = getMinMaxValues();
      }
      RectangleUtil.expand(bounds, axisCount, coordinates);
      return newBoundingBox(axisCount, bounds);
    }
  }

  default BoundingBox expandToInclude(final Geometry geometry) {
    if (geometry == null || geometry.isEmpty()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Geometry convertedGeometry = geometry.convertGeometry(geometryFactory);
      final BoundingBox box = convertedGeometry.getBoundingBox();
      return expandToInclude(box);
    }
  }

  default BoundingBox expandToInclude(final Point point) {
    return expandToInclude((Geometry)point);
  }

  default BoundingBox expandToInclude(final Record object) {
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
  default double getArea() {
    if (getAxisCount() < 2 || isEmpty()) {
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
  default double getAspectRatio() {
    final double width = getWidth();
    final double height = getHeight();
    final double aspectRatio = width / height;
    return aspectRatio;
  }

  default int getAxisCount() {
    return 2;
  }

  default Point getBottomLeftPoint() {
    return getGeometryFactory().point(getMinX(), getMinY());
  }

  default Point getBottomRightPoint() {
    return getGeometryFactory().point(getMaxX(), getMinY());
  }

  @Override
  default BoundingBox getBoundingBox() {
    return this;
  }

  default Point getCentre() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.point();
    } else {
      final double centreX = getCentreX();
      final double centreY = getCentreY();
      return geometryFactory.point(centreX, centreY);
    }
  }

  default double getCentreX() {
    return (getMinX() + getMaxX()) / 2;
  }

  default double getCentreY() {
    return (getMinY() + getMaxY()) / 2;
  }

  /**
   * maxX,minY
   * minX,minY
   * minX,maxY
   * maxX,maxY
   */
  default Point getCornerPoint(int index) {
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

  default LineString getCornerPoints() {
    final double minX = getMinX();
    final double maxX = getMaxX();
    final double minY = getMinY();
    final double maxY = getMaxY();
    return new LineStringDouble(2, maxX, minY, minX, minY, minX, maxY, maxX, maxY);
  }

  /**
   *  Returns the difference between the maximum and minimum y values.
   *
   *@return    max y - min y, or 0 if this is a null <code>BoundingBox</code>
   */
  default double getHeight() {
    if (getAxisCount() < 2 || isEmpty()) {
      return 0;
    } else {
      return getMaxY() - getMinY();
    }
  }

  default Quantity<Length> getHeightLength() {
    final double height = getHeight();
    final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return Quantities.getQuantity(height, Units.METRE);
    } else {
      return Quantities.getQuantity(height, coordinateSystem.getLengthUnit());
    }
  }

  /**
   * Get the geometry factory.
   *
   * @return The geometry factory.
   */
  @Override
  default CoordinateSystem getHorizontalCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getHorizontalCoordinateSystem();
    }
  }

  default double getMax(final int i) {
    return Double.NaN;
  }

  default <Q extends Quantity<Q>> Quantity<Q> getMaximum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double max = this.getMax(axisIndex);
    return Quantities.getQuantity(max, unit);
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  default <Q extends Quantity<Q>> double getMaximum(final int axisIndex, final Unit convertUnit) {
    final Quantity<Q> max = getMaximum(axisIndex);
    return QuantityType.doubleValue(max, convertUnit);
  }

  /**
   *  Returns the <code>BoundingBox</code>s maximum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the maximum x-coordinate
   */
  default double getMaxX() {
    return getMax(0);
  }

  /**
   *  Returns the <code>BoundingBox</code>s maximum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the maximum y-coordinate
   */
  default double getMaxY() {
    return getMax(1);
  }

  default double getMaxZ() {
    return getMax(2);
  }

  default double getMin(final int i) {
    return Double.NaN;
  }

  default <Q extends Quantity<Q>> Quantity<Q> getMinimum(final int axisIndex) {
    final Unit<Q> unit = getUnit();
    final double min = this.getMin(axisIndex);
    return Quantities.getQuantity(min, unit);
  }

  default <Q extends Quantity<Q>> double getMinimum(final int axisIndex,
    final Unit<Q> convertUnit) {
    final Quantity<Q> min = getMinimum(axisIndex);
    return QuantityType.doubleValue(min, convertUnit);
  }

  default double[] getMinMaxValues() {
    if (isEmpty()) {
      return null;
    } else {
      final double minX = getMinX();
      final double minY = getMinY();
      final double maxX = getMaxX();
      final double maxY = getMaxY();
      return new double[] {
        minX, minY, maxX, maxY
      };
    }
  }

  default double[] getMinMaxValues(final int axisCount) {
    if (isEmpty()) {
      return null;
    } else {
      final double[] bounds = new double[2 * axisCount];
      for (int i = 0; i < axisCount; i++) {
        bounds[i] = getMin(i);
        bounds[i + axisCount] = getMax(i);
      }
      return bounds;
    }
  }

  /**
   *  Returns the <code>BoundingBox</code>s minimum x-value. min x > max x
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the minimum x-coordinate
   */
  default double getMinX() {
    return getMin(0);
  }

  /**
   *  Returns the <code>BoundingBox</code>s minimum y-value. min y > max y
   *  indicates that this is a null <code>BoundingBox</code>.
   *
   *@return    the minimum y-coordinate
   */
  default double getMinY() {
    return getMin(1);
  }

  default double getMinZ() {
    return getMin(2);
  }

  default int getOutcode(final double x, final double y) {
    int out;
    final double minX = getMinX();
    final double maxX = getMaxX();
    if (x < minX) {
      out = OutCode.OUT_LEFT;
    } else if (x > maxX) {
      out = OutCode.OUT_RIGHT;
    } else {
      out = 0;
    }
    final double minY = getMinY();
    final double maxY = getMaxY();
    if (y < minY) {
      out |= OutCode.OUT_BOTTOM;
    } else if (y > maxY) {
      out |= OutCode.OUT_TOP;
    }
    return out;
  }

  default Point getRandomPointWithin() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double x = getMinX() + getWidth() * Math.random();
    final double y = getMinY() + getHeight() * Math.random();
    return geometryFactory.point(x, y);
  }

  default Point getTopLeftPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(getMinX(), getMaxY());
  }

  default Point getTopRightPoint() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(getMaxX(), getMaxY());
  }

  @SuppressWarnings("unchecked")
  default <Q extends Quantity<Q>> Unit<Q> getUnit() {
    final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return (Unit<Q>)Units.METRE;
    } else {
      return coordinateSystem.<Q> getUnit();
    }
  }

  /**
   *  Returns the difference between the maximum and minimum x values.
   *
   *@return    max x - min x, or 0 if this is a null <code>BoundingBox</code>
   */
  default double getWidth() {
    if (getAxisCount() < 2 || isEmpty()) {
      return 0;
    } else {
      final double minX = getMinX();
      final double maxX = getMaxX();

      return maxX - minX;
    }
  }

  default Quantity<Length> getWidthLength() {
    final double width = getWidth();
    final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    if (coordinateSystem == null) {
      return Quantities.getQuantity(width, Units.METRE);
    } else {
      return Quantities.getQuantity(width, coordinateSystem.getLengthUnit());
    }
  }

  /**
   * Computes the intersection of two {@link BoundingBox}s.
   *
   * @param env the envelope to intersect with
   * @return a new BoundingBox representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  default BoundingBox intersection(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
    if (isEmpty() || convertedBoundingBox.isEmpty() || !intersects(convertedBoundingBox)) {
      return newBoundingBoxEmpty();
    } else {
      final double intMinX = Math.max(getMinX(), convertedBoundingBox.getMinX());
      final double intMinY = Math.max(getMinY(), convertedBoundingBox.getMinY());
      final double intMaxX = Math.min(getMaxX(), convertedBoundingBox.getMaxX());
      final double intMaxY = Math.min(getMaxY(), convertedBoundingBox.getMaxY());
      return newBoundingBox(intMinX, intMinY, intMaxX, intMaxY);
    }
  }

  /**
   *  Check if the region defined by <code>other</code>
   *  overlaps (intersects) the region of this <code>BoundingBox</code>.
   *
   *@param  other  the <code>BoundingBox</code> which this <code>BoundingBox</code> is
   *          being checked for overlapping
   *@return        <code>true</code> if the <code>BoundingBox</code>s overlap
   */
  default boolean intersects(final BoundingBox other) {
    if (isEmpty() || other.isEmpty()) {
      return false;
    } else {
      final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
      final BoundingBox convertedBoundingBox = other.toCoordinateSystem(coordinateSystem, 2);
      return intersectsFast(convertedBoundingBox);
    }
  }

  /**
   *  Check if the point <code>(x, y)</code>
   *  overlaps (lies inside) the region of this <code>BoundingBox</code>.
   *
   *@param  x  the x-ordinate of the point
   *@param  y  the y-ordinate of the point
   *@return        <code>true</code> if the point overlaps this <code>BoundingBox</code>
   */
  default boolean intersects(final double x, final double y) {
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

  default boolean intersects(double x1, double y1, double x2, double y2) {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();
    if (x1 > x2) {
      final double t = x1;
      x1 = x2;
      x2 = t;
    }
    if (y1 > y2) {
      final double t = y1;
      y1 = y2;
      y2 = t;
    }
    return !(x1 > maxX || x2 < minX || y1 > maxY || y2 < minY);
  }

  /**
   * Fast version of intersects that assumes it's in the same coordinate system.
   *
   * @param boundingBox
   * @return
   */
  default boolean intersectsFast(final BoundingBox boundingBox) {
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    return intersects(minX, minY, maxX, maxY);
  }

  default boolean isWithinDistance(final BoundingBox boundingBox, final double maxDistance) {
    final double distance = boundingBox.distance(boundingBox);
    if (distance < maxDistance) {
      return true;
    } else {
      return false;
    }
  }

  default boolean isWithinDistance(final Geometry geometry, final double maxDistance) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return isWithinDistance(boundingBox, maxDistance);
  }

  /**
   * <p>Construct a new new BoundingBox by moving the min/max x coordinates by xDisplacement and
   * the min/max y coordinates by yDisplacement. If the bounding box is null or the xDisplacement
   * and yDisplacement are 0 then this bounding box will be returned.</p>
   *
   * @param xDisplacement The distance to move the min/max x coordinates.
   * @param yDisplacement The distance to move the min/max y coordinates.
   * @return The moved bounding box.
   */
  default BoundingBox move(final double xDisplacement, final double yDisplacement) {
    if (isEmpty() || xDisplacement == 0 && yDisplacement == 0) {
      return this;
    } else {
      final double x1 = getMinX() + xDisplacement;
      final double x2 = getMaxX() + xDisplacement;
      final double y1 = getMinY() + yDisplacement;
      final double y2 = getMaxY() + yDisplacement;
      return newBoundingBox(x1, y1, x2, y2);
    }
  }

  default BoundingBox newBoundingBox(final double x, final double y) {
    return new BoundingBoxDoubleXY(x, y);
  }

  default BoundingBox newBoundingBox(final double x1, final double y1, final double x2,
    final double y2) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == GeometryFactory.DEFAULT_3D) {
      return new BoundingBoxDoubleXY(x1, y1, x2, y2);
    } else {
      return new BoundingBoxDoubleXYGeometryFactory(geometryFactory, x1, y1, x2, y2);
    }
  }

  default BoundingBox newBoundingBox(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory.convertAxisCount(3), 3, x1, y1, z1, x2, y2, z2);
  }

  default BoundingBox newBoundingBox(final int axisCount, final double... bounds) {
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[axisCount];
    final double maxY = bounds[axisCount + 1];
    return newBoundingBox(minX, minY, maxX, maxY);
  }

  default BoundingBox newBoundingBox(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return newBoundingBox(x, y);
  }

  default BoundingBox newBoundingBoxEmpty() {
    return BoundingBoxDoubleXY.EMPTY;
  }

  /**
   * The outcode must be outside the rectangle and opposing outcode must be inside the rectangle
   */
  default void outCodeIntersection(final int outCode, final double x1, final double y1,
    final double x2, final double y2, final BiConsumerDouble action) {
    double x;
    double y;
    final double deltaX = x2 - x1;
    final double deltaY = y2 - y1;
    if (OutCode.isTop(outCode)) {
      final double maxY = getMaxY();
      final double ratio = (maxY - y1) / deltaY;
      x = x1 + deltaX * ratio;
      y = maxY;
    } else if (OutCode.isBottom(outCode)) {
      final double minY = getMinY();
      final double ratio = (minY - y1) / deltaY;
      x = x2 + deltaX * ratio;
      y = minY;
    } else if (OutCode.isRight(outCode)) {
      final double maxX = getMaxX();
      final double ratio = (maxX - x1) / deltaX;
      y = y1 + deltaY * ratio;
      x = maxX;
    } else if (OutCode.isLeft(outCode)) {
      final double minX = getMinX();
      final double ratio = (minX - x1) / deltaX;
      y = y1 + deltaY * ratio;
      x = minX;
    } else {
      throw new IllegalArgumentException("Outcode " + outCode);
    }
    action.accept(x, y);
  }

  default void outCodeIntersection(int out1, int out2, double x1, double y1, double x2, double y2,
    final Consumer4Double action) {
    final double minX = getMinX();
    final double minY = getMinY();
    final double maxX = getMaxX();
    final double maxY = getMaxY();

    while (true) {
      if ((out1 | out2) == 0) {
        action.accept(x1, y1, x2, y2);
        return;
      } else if ((out1 & out2) != 0) {
        return;
      } else {
        double x;
        double y;

        final int outcodeOut = out1 != 0 ? out1 : out2;

        final double deltaY = y2 - y1;
        final double deltaX = x2 - x1;
        if (OutCode.isTop(outcodeOut)) {
          final double ratio = (maxY - y1) / deltaY;
          x = x1 + deltaX * ratio;
          y = maxY;
        } else if (OutCode.isBottom(outcodeOut)) {
          final double ratio = (minY - y1) / deltaY;
          x = x2 + deltaX * ratio;
          y = minY;
        } else if (OutCode.isRight(outcodeOut)) {
          final double ratio = (maxX - x1) / deltaX;
          x = maxX;
          y = y1 + deltaY * ratio;
        } else if (OutCode.isLeft(outcodeOut)) {
          final double ratio = (minX - x1) / deltaX;
          x = minX;
          y = y1 + deltaY * ratio;
        } else {
          throw new IllegalStateException("Cannot clip as both points are inside the rectangle");
        }

        if (outcodeOut == out1) {
          x1 = x;
          y1 = y;
          out1 = getOutcode(x1, y1);
        } else {
          x2 = x;
          y2 = y;
          out2 = getOutcode(x2, y2);
        }
      }
    }
  }

  default BoundingBox toCoordinateSystem(final CoordinateSystem coordinateSystem,
    final int minAxisCount) {
    final CoordinateSystem coordinateSystemThis = getHorizontalCoordinateSystem();
    if (coordinateSystem == null || coordinateSystemThis == null) {
      return this;
    } else {
      if (coordinateSystemThis == coordinateSystem || coordinateSystemThis
        .getHorizontalCoordinateSystemId() == coordinateSystem.getHorizontalCoordinateSystemId()) {
        return this;
      } else {
        final GeometryFactory geometryFactory = getGeometryFactory()
          .convertCoordinateSystem(coordinateSystem)
          .convertAxisCount(minAxisCount);
        return convert(geometryFactory);
      }
    }
  }

  default BoundingBox toCoordinateSystem(final GeometryFactory geometryFactory,
    final int minAxisCount) {
    if (geometryFactory == null) {
      return this;
    } else {
      final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
      return toCoordinateSystem(coordinateSystem, minAxisCount);
    }
  }

  /**
   * Creates a {@link Geometry} with the same extent as the given envelope.
   * The Geometry returned is guaranteed to be valid.
   * To provide this behaviour, the following cases occur:
   * <p>
   * If the <code>BoundingBox</code> is:
   * <ul>
   * <li>null : returns an empty {@link Point}
   * <li>a point : returns a non-empty {@link Point}
   * <li>a line : returns a two-point {@link LineString}
   * <li>a rectangle : returns a {@link Polygon}> whose points are (minx, miny),
   *  (minx, maxy), (maxx, maxy), (maxx, miny), (minx, miny).
   * </ul>
   *
   *@param  envelope the <code>BoundingBox</code> to convert
   *@return an empty <code>Point</code> (for null <code>BoundingBox</code>s),
   *  a <code>Point</code> (when min x = max x and min y = max y) or a
   *      <code>Polygon</code> (in all other cases)
   */

  default Geometry toGeometry() {
    GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.floating2d(0);
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

  default Polygon toPolygon() {
    return toPolygon(100, 100);

  }

  default Polygon toPolygon(final GeometryFactory factory) {
    return toPolygon(factory, 100, 100);
  }

  default Polygon toPolygon(final GeometryFactory factory, final int numSegments) {
    return toPolygon(factory, numSegments, numSegments);
  }

  default Polygon toPolygon(GeometryFactory geometryFactory, int numX, int numY) {
    if (isEmpty()) {
      return geometryFactory.polygon();
    } else {
      final GeometryFactory factory = getGeometryFactory();
      if (geometryFactory == null) {
        if (factory == null) {
          geometryFactory = GeometryFactory.floating2d(0);
        } else {
          geometryFactory = factory;
        }
      }
      try {
        double minStep = 0.00001;
        final CoordinateSystem coordinateSystem = factory.getHorizontalCoordinateSystem();
        if (coordinateSystem instanceof ProjectedCoordinateSystem) {
          minStep = 1;
        } else {
          minStep = 0.00001;
        }

        double xStep;
        final double width = getWidth();
        if (!Double.isFinite(width)) {
          return geometryFactory.polygon();
        } else if (numX <= 1) {
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
        final int coordinateCount = 1 + 2 * (numX + numY);
        final double[] coordinates = new double[coordinateCount * 2];
        int i = 0;

        coordinates[i++] = maxX;
        coordinates[i++] = minY;
        for (int j = 0; j < numX - 1; j++) {
          final double x = maxX - j * xStep;
          coordinates[i++] = x;
          coordinates[i++] = minY;
        }
        coordinates[i++] = minX;
        coordinates[i++] = minY;

        for (int j = 0; j < numY - 1; j++) {
          final double y = minY + j * yStep;
          coordinates[i++] = minX;
          coordinates[i++] = y;
        }
        coordinates[i++] = minX;
        coordinates[i++] = maxY;

        for (int j = 0; j < numX - 1; j++) {
          final double x = minX + j * xStep;
          coordinates[i++] = x;
          coordinates[i++] = maxY;
        }

        coordinates[i++] = maxX;
        coordinates[i++] = maxY;

        for (int j = 0; j < numY - 1; j++) {
          final double y = minY + (numY - j) * yStep;
          coordinates[i++] = maxX;
          coordinates[i++] = y;
        }
        coordinates[i++] = maxX;
        coordinates[i++] = minY;

        final LinearRing ring = factory.linearRing(2, coordinates);
        final Polygon polygon = factory.polygon(ring);
        if (geometryFactory == null) {
          return polygon;
        } else {
          return (Polygon)polygon.convertGeometry(geometryFactory);
        }
      } catch (final IllegalArgumentException e) {
        Logs.error(this, "Unable to convert to polygon: " + this, e);
        return geometryFactory.polygon();
      }
    }
  }

  default Polygon toPolygon(final int numSegments) {
    return toPolygon(numSegments, numSegments);
  }

  default Polygon toPolygon(final int numX, final int numY) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return toPolygon(geometryFactory, numX, numY);
  }

  default RectangleXY toRectangle() {
    final double minX = getMinX();
    final double minY = getMinY();
    final double width = getWidth();
    final double height = getHeight();
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.newRectangle(minX, minY, width, height);
  }
}
