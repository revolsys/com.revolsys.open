package com.revolsys.jts.util;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;

public class EnvelopeUtil {
  public static double[] createBounds(final Coordinates point) {
    final int axisCount = point.getAxisCount();
    return createBounds(axisCount, point);
  }

  public static double[] createBounds(final double... bounds) {
    final int axisCount = bounds.length;
    final double[] newBounds = new double[axisCount * 2];
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = bounds[axisIndex];
      newBounds[axisIndex] = value;
      newBounds[axisCount + axisCount] = value;
    }
    return newBounds;
  }

  public static double[] createBounds(final GeometryFactory geometryFactory,
    final Coordinates point) {
    final int axisCount = point.getAxisCount();
    return createBounds(geometryFactory, axisCount, point);
  }

  public static double[] createBounds(final GeometryFactory geometryFactory,
    final double... bounds) {
    final int axisCount = bounds.length;
    final double[] newBounds = new double[axisCount * 2];
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double value = bounds[axisIndex];
      if (geometryFactory != null) {
        value = geometryFactory.makePrecise(axisIndex, value);
      }
      newBounds[axisIndex] = value;
      newBounds[axisCount + axisIndex] = value;
    }
    return newBounds;
  }

  public static double[] createBounds(final GeometryFactory geometryFactory,
    final int axisCount, final Coordinates point) {
    final double[] bounds = new double[axisCount * 2];
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double value = point.getValue(axisIndex);
      if (geometryFactory != null) {
        value = geometryFactory.makePrecise(axisIndex, value);
      }
      bounds[axisIndex] = value;
      bounds[axisCount + axisIndex] = value;
    }
    return bounds;
  }

  public static double[] createBounds(final int axisCount, final Coordinates point) {
    final double[] bounds = new double[axisCount * 2];
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = point.getValue(axisIndex);
      bounds[axisIndex] = value;
      bounds[axisCount + axisIndex] = value;
    }
    return bounds;
  }

  public static void expand(final double[] bounds, final int axisCount,
    final Coordinates point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = point.getValue(axisIndex);
      expand(bounds, axisCount, axisIndex, value);
    }
  }

  public static void expand(final double[] bounds, final int axisCount,
    final int axisIndex, final double value) {
    final double min = bounds[axisIndex];
    if (value < min || Double.isNaN(min)) {
      bounds[axisIndex] = value;
    }
    final double max = bounds[axisCount + axisIndex];
    if (value > max || Double.isNaN(max)) {
      bounds[axisCount + axisIndex] = value;
    }
  }

  public static void expand(final GeometryFactory geometryFactory,
    final double[] bounds, final Coordinates point) {
    final int axisCount = bounds.length / 2;
    final int count = Math.min(axisCount, point.getAxisCount());
    for (int axisIndex = 0; axisIndex < count; axisIndex++) {
      final double value = point.getValue(axisIndex);
      expand(geometryFactory, bounds, axisCount, axisIndex, value);
    }
  }

  public static void expand(final GeometryFactory geometryFactory,
    final double[] bounds, final double... values) {
    final int axisCount = bounds.length / 2;
    final int count = Math.min(axisCount, values.length);
    for (int axisIndex = 0; axisIndex < count; axisIndex++) {
      final double value = values[axisIndex];
      expand(geometryFactory, bounds, axisCount, axisIndex, value);
    }
  }

  public static void expand(final GeometryFactory geometryFactory,
    final double[] bounds, final int axisIndex, double value) {
    if (geometryFactory != null) {
      value = geometryFactory.makePrecise(axisIndex, value);
    }
    final int axisCount = bounds.length / 2;
    final double min = bounds[axisIndex];
    if (value < min || Double.isNaN(min)) {
      bounds[axisIndex] = value;
    }
    final double max = bounds[axisCount + axisIndex];
    if (value > max || Double.isNaN(max)) {
      bounds[axisCount + axisIndex] = value;
    }
  }

  public static void expand(final GeometryFactory geometryFactory,
    final double[] bounds, final int axisCount, final int axisIndex, double value) {
    if (!Double.isNaN(value)) {
      if (geometryFactory != null) {
        value = geometryFactory.makePrecise(axisIndex, value);
      }
      final double min = bounds[axisIndex];
      if (value < min || Double.isNaN(min)) {
        bounds[axisIndex] = value;
      }
      final double max = bounds[axisCount + axisIndex];
      if (value > max || Double.isNaN(max)) {
        bounds[axisCount + axisIndex] = value;
      }
    }
  }

  public static void expandX(final double[] bounds, final int axisCount,
    final double value) {
    expand(bounds, axisCount, 0, value);
  }

  public static void expandY(final double[] bounds, final int axisCount,
    final double value) {
    expand(bounds, axisCount, 1, value);
  }

  public static void expandZ(final double[] bounds, final int axisCount,
    final double value) {
    expand(bounds, axisCount, 2, value);
  }

  public static double getMax(final double[] bounds, final int axisIndex) {
    if (bounds == null) {
      return Double.NaN;
    } else {
      final int axisCount = bounds.length / 2;
      if (axisIndex < 0 || axisIndex > axisCount) {
        return Double.NaN;
      } else {
        final double max = bounds[axisCount + axisIndex];
        return max;
      }
    }
  }

  public static double getMin(final double[] bounds, final int axisIndex) {
    if (bounds == null) {
      return Double.NaN;
    } else {
      final int axisCount = bounds.length / 2;
      if (axisIndex < 0 || axisIndex > axisCount) {
        return Double.NaN;
      } else {
        final double min = bounds[axisIndex];
        return min;
      }
    }
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
}
