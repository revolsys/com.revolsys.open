package com.revolsys.geometry.model;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.ProjectionFactory;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.PointDoubleGf;
import com.revolsys.geometry.model.util.BoundingBoxEditor;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.logging.Logs;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.wkt.WktParser;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;
import com.revolsys.util.QuantityType;
import com.revolsys.util.function.Consumer3;
import com.revolsys.util.number.Doubles;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public interface BoundingBox extends Emptyable, BoundingBoxProxy {

  BoundingBox EMPTY = new BoundingBoxDoubleGf();

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

  static BoundingBox bboxNew(final String wkt) {
    return newBoundingBox(wkt);
  }

  static String bboxToWkt(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (minX > maxX) {
      return "BBOX EMPTY";
    } else {
      final StringBuilder s = new StringBuilder();
      s.append("BBOX(");
      s.append(Doubles.toString(minX));
      s.append(' ');
      s.append(Doubles.toString(minY));
      s.append(',');
      s.append(Doubles.toString(maxX));
      s.append(' ');
      s.append(Doubles.toString(maxY));
      s.append(')');
      return s.toString();
    }
  }

  static BoundingBox empty() {
    return EMPTY;
  }

  static int hashCode(final BoundingBox boundingBox) {
    if (boundingBox.isEmpty()) {
      return 0;
    } else {
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      long bits = 17;
      bits ^= java.lang.Double.doubleToLongBits(minX) * 37;
      bits ^= java.lang.Double.doubleToLongBits(minY) * 37;
      if (minX != maxX) {
        bits ^= java.lang.Double.doubleToLongBits(maxX) * 37;
      }
      if (minY != maxY) {
        bits ^= java.lang.Double.doubleToLongBits(maxY) * 37;
      }
      return (int)bits ^ (int)(bits >> 32);
    }
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
      return EMPTY;
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
            geometryFactory = GeometryFactory.floating(srid, 2);
          }
          WktParser.hasText(reader, ";");
        }
        if (WktParser.hasText(reader, "BBOX(")) {
          final Double x1 = WktParser.parseDouble(reader);
          if (WktParser.hasText(reader, ",")) {
            WktParser.skipWhitespace(reader);
            final Double y1 = WktParser.parseDouble(reader);
            WktParser.skipWhitespace(reader);
            final Double x2 = WktParser.parseDouble(reader);
            if (WktParser.hasText(reader, ",")) {
              WktParser.skipWhitespace(reader);
              final Double y2 = WktParser.parseDouble(reader);
              return new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
            } else {
              throw new IllegalArgumentException(
                "Expecting a ',' not " + FileUtil.getString(reader));
            }

          } else {
            throw new IllegalArgumentException("Expecting a ',' not " + FileUtil.getString(reader));
          }
        } else if (WktParser.hasText(reader, "BBOX EMPTY")) {
          return new BoundingBoxDoubleGf(geometryFactory);
        }
      } catch (final IOException e) {
        throw Exceptions.wrap("Error reading WKT:" + wkt, e);
      }
    }

    return EMPTY;
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
          s.append(' ');
        }
        s.append(Doubles.toString(boundingBox.getMin(axisIndex)));
      }
      s.append(',');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(' ');
        }
        s.append(Doubles.toString(boundingBox.getMax(axisIndex)));
      }
      s.append(')');
    }
    return s.toString();
  }

  default boolean bboxCoveredBy(final double minX, final double minY, final double maxX,
    final double maxY) {
    final double minX2 = getMinX();
    final double minY2 = getMinY();
    final double maxX2 = getMaxX();
    final double maxY2 = getMaxY();
    return minX <= minX2 && maxX2 <= maxX && minY <= minY2 && maxY2 <= maxY;
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
  default boolean bboxCovers(final double x, final double y) {
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

  default double bboxDistance(final double x, final double y) {
    if (bboxIntersects(x, y)) {
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

  /**
   * Computes the distance between this and another
   * <code>BoundingBox</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  default double bboxDistance(final double minX, final double minY, final double maxX,
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

  default BoundingBox bboxEdit(final Consumer<BoundingBoxEditor> action) {
    final BoundingBoxEditor editor = new BoundingBoxEditor(this);
    action.accept(editor);
    return editor.newBoundingBox();
  }

  default BoundingBox bboxIntersection(final BoundingBox boundingBox) {
    return intersection(boundingBox);
  }

  /**
   * Computes the intersection of two {@link BoundingBox}s.
   *
   * @param env the envelope to intersect with
   * @return a new BoundingBox representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  default BoundingBox bboxIntersection(final BoundingBoxProxy boundingBox) {
    final BoundingBoxFunction<BoundingBox> action = BoundingBox::bboxIntersection;
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox empty = geometryFactory.bboxEmpty();
    return bboxWith(boundingBox, action, empty);
  }

  /**
   * Computes the intersection of this and another bounding box.
   *
   * @return The intersection.
   */
  default BoundingBox bboxIntersection(final double minX, final double minY, final double maxX,
    final double maxY) {
    if (isEmpty()) {
      return this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (bboxIntersects(minX, minY, maxX, maxY)) {
        final double intMinX = Math.max(getMinX(), minX);
        final double intMinY = Math.max(getMinY(), minY);
        final double intMaxX = Math.min(getMaxX(), maxX);
        final double intMaxY = Math.min(getMaxY(), maxY);
        return geometryFactory.newBoundingBox(intMinX, intMinY, intMaxX, intMaxY);
      } else {
        return geometryFactory.bboxEmpty();
      }
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
  default boolean bboxIntersects(final double x, final double y) {
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

  default boolean bboxIntersects(double x1, double y1, double x2, double y2) {
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

  default BoundingBox bboxToCs(final GeometryFactory geometryFactory) {
    return convert(geometryFactory);
  }

  default String bboxToEWkt() {
    final StringBuilder s = new StringBuilder();
    final int srid = getHorizontalCoordinateSystemId();
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
          s.append(' ');
        }
        s.append(Doubles.toString(getMin(axisIndex)));
      }
      s.append(',');
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        if (axisIndex > 0) {
          s.append(' ');
        }
        s.append(Doubles.toString(getMax(axisIndex)));
      }
      s.append(')');
    }
    return s.toString();
  }

  @Override
  default <R> R bboxWith(final BoundingBoxProxy boundingBox,
    final BiFunction<BoundingBox, BoundingBox, R> action, final R emptyValue) {
    BoundingBox boundingBox2;
    if (boundingBox == null) {
      boundingBox2 = BoundingBox.empty();
    } else {
      boundingBox2 = boundingBox.getBoundingBox();
    }
    return action.apply(this, boundingBox2);

  }

  @Override
  default <R> R bboxWith(final BoundingBoxProxy boundingBox, final BoundingBoxFunction<R> action,
    final R emptyResult) {
    if (boundingBox != null && !isEmpty()) {
      BoundingBox boundingBox2 = boundingBox.getBoundingBox();
      if (isProjectionRequired(boundingBox2)) {
        // TODO just convert points
        boundingBox2 = boundingBox2.bboxToCs(getGeometryFactory());
      }
      if (!boundingBox2.isEmpty()) {
        final double minX = boundingBox2.getMinX();
        final double minY = boundingBox2.getMinY();
        final double maxX = boundingBox2.getMaxX();
        final double maxY = boundingBox2.getMaxY();
        return action.accept(this, minX, minY, maxX, maxY);
      }

    }
    return emptyResult;
  }

  @Override
  default <R> R bboxWith(Point point, final BoundingBoxPointFunction<R> action,
    final R emptyResult) {
    if (point != null && !isEmpty()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.as2d(geometryFactory);
      if (!point.isEmpty()) {
        final double x = point.getX();
        final double y = point.getY();
        return action.accept(this, x, y);
      }

    }
    return emptyResult;
  }

  default BoundingBox clipToCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem == null) {
      return this;
    } else {
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      return intersection(areaBoundingBox);
    }
  }

  default BoundingBox convert(final GeometryFactory geometryFactory) {
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

  default BoundingBox convert(GeometryFactory geometryFactory, final int axisCount) {
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
    return BoundingBoxUtil.covers(minX1, minY1, maxX1, maxY1, minX2, minY2, maxX2, maxY2);
  }

  /**
   * Tests if the <code>BoundingBoxDoubleGf other</code>
   * lies wholely inside this <code>BoundingBoxDoubleGf</code> (inclusive of the boundary).
   *
   *@param  other the <code>BoundingBoxDoubleGf</code> to check
   *@return true if this <code>BoundingBoxDoubleGf</code> covers the <code>other</code>
   */
  default boolean covers(BoundingBox other) {
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
  default boolean covers(final double x, final double y) {
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
   *@param  p  the point which this <code>BoundingBoxDoubleGf</code> is
   *      being checked for containing
   *@return    <code>true</code> if the point lies in the interior or
   *      on the boundary of this <code>BoundingBoxDoubleGf</code>.
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
   * <code>BoundingBoxDoubleGf</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  default double distance(BoundingBox boundingBox) {
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

  default double distance(final Geometry geometry) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    return distance(boundingBox);
  }

  /**
   * Computes the distance between this and another
   * <code>BoundingBoxDoubleGf</code>.
   * The distance between overlapping Envelopes is 0.  Otherwise, the
   * distance is the Euclidean distance between the closest points.
   */
  default double distance(Point point) {
    point = point.convertGeometry(getGeometryFactory());
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

  default void expand(final GeometryFactory geometryFactory, final double[] bounds,
    final CoordinatesOperation operation, final double[] to, final double... from) {

    operation.perform(2, from, 2, to);
    BoundingBoxUtil.expand(geometryFactory, bounds, to);
  }

  default BoundingBox expand(final Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return new BoundingBoxDoubleGf(geometryFactory, point);
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
      return new BoundingBoxDoubleGf(geometryFactory, 2, minX, minY, maxX, maxY);
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
        return new BoundingBoxDoubleGf(geometryFactory, 2, minX, minY, maxX, maxY);
      }
    }
  }

  default BoundingBox expandToInclude(final double... coordinates) {
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
  double getArea();

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

  int getAxisCount();

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

  default double[] getBounds() {
    return getMinMaxValues();
  }

  default double[] getBounds(final int axisCount) {
    return getMinMaxValues(axisCount);
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

  @Override
  default int getHorizontalCoordinateSystemId() {
    return getCoordinateSystemId();
  }

  double getMax(int i);

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

  double getMin(int i);

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
   * Computes the intersection of two {@link BoundingBoxDoubleGf}s.
   *
   * @param env the envelope to intersect with
   * @return a new BoundingBox representing the intersection of the envelopes (this will be
   * the null envelope if either argument is null, or they do not intersect
   */
  default BoundingBox intersection(final BoundingBox boundingBox) {
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
  default boolean intersects(final BoundingBox other) {
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

  default boolean intersects(double x0, double y0, double x1, double y1) {
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

  default boolean intersects(final Geometry geometry) {
    if (geometry != null) {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return intersects(boundingBox);
    }
    return false;

  }

  /**
   *  Check if the point <code>p</code>
   *  overlaps (lies inside) the region of this <code>BoundingBoxDoubleGf</code>.
   *
   *@param  p  the <code>Coordinate</code> to be tested
   *@return        <code>true</code> if the point overlaps this <code>BoundingBoxDoubleGf</code>
   */
  default boolean intersects(final Point point) {
    return point.intersects(this);
  }

  default boolean intersects(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      return intersects(geometry);
    }
    return false;
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
      final GeometryFactory geometryFactory = getGeometryFactory();
      final double x1 = getMinX() + xDisplacement;
      final double x2 = getMaxX() + xDisplacement;
      final double y1 = getMinY() + yDisplacement;
      final double y2 = getMaxY() + yDisplacement;
      return new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
    }
  }

  default int outcode(final double x, final double y) {
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

  default Geometry toGeometry() {
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

}
