package com.revolsys.jts.util;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;

public class EnvelopeUtil {
  public static double[] createBounds(final Coordinates point) {
    final int numAxis = point.getNumAxis();
    return createBounds(numAxis, point);
  }

  public static double[] createBounds(final double... bounds) {
    final int numAxis = bounds.length;
    final double[] newBounds = new double[numAxis * 2];
    for (int axisIndex = 0; axisIndex < numAxis; axisIndex++) {
      final double value = bounds[axisIndex];
      newBounds[axisIndex] = value;
      newBounds[numAxis + numAxis] = value;
    }
    return newBounds;
  }

  public static double[] createBounds(final GeometryFactory geometryFactory,
    final Coordinates point) {
    final int numAxis = point.getNumAxis();
    return createBounds(geometryFactory, numAxis, point);
  }

  public static double[] createBounds(final GeometryFactory geometryFactory,
    final double... bounds) {
    final int numAxis = bounds.length;
    final double[] newBounds = new double[numAxis * 2];
    for (int axisIndex = 0; axisIndex < numAxis; axisIndex++) {
      double value = bounds[axisIndex];
      if (geometryFactory != null) {
        value = geometryFactory.makePrecise(axisIndex, value);
      }
      newBounds[axisIndex] = value;
      newBounds[numAxis + axisIndex] = value;
    }
    return newBounds;
  }

  public static double[] createBounds(final GeometryFactory geometryFactory,
    final int numAxis, final Coordinates point) {
    final double[] bounds = new double[numAxis * 2];
    for (int axisIndex = 0; axisIndex < numAxis; axisIndex++) {
      double value = point.getValue(axisIndex);
      if (geometryFactory != null) {
        value = geometryFactory.makePrecise(axisIndex, value);
      }
      bounds[axisIndex] = value;
      bounds[numAxis + axisIndex] = value;
    }
    return bounds;
  }

  public static double[] createBounds(final int numAxis, final Coordinates point) {
    final double[] bounds = new double[numAxis * 2];
    for (int axisIndex = 0; axisIndex < numAxis; axisIndex++) {
      final double value = point.getValue(axisIndex);
      bounds[axisIndex] = value;
      bounds[numAxis + axisIndex] = value;
    }
    return bounds;
  }

  public static void expand(final double[] bounds, final int numAxis,
    final Coordinates point) {
    for (int axisIndex = 0; axisIndex < numAxis; axisIndex++) {
      final double value = point.getValue(axisIndex);
      expand(bounds, numAxis, axisIndex, value);
    }
  }

  public static void expand(final double[] bounds, final int numAxis,
    final int axisIndex, final double value) {
    final double min = bounds[axisIndex];
    if (value < min || Double.isNaN(min)) {
      bounds[axisIndex] = value;
    }
    final double max = bounds[numAxis + axisIndex];
    if (value > max || Double.isNaN(max)) {
      bounds[numAxis + axisIndex] = value;
    }
  }

  public static void expand(final GeometryFactory geometryFactory,
    final double[] bounds, final Coordinates point) {
    final int numAxis = bounds.length / 2;
    final int count = Math.min(numAxis, point.getNumAxis());
    for (int axisIndex = 0; axisIndex < count; axisIndex++) {
      final double value = point.getValue(axisIndex);
      expand(geometryFactory, bounds, numAxis, axisIndex, value);
    }
  }

  public static void expand(final GeometryFactory geometryFactory,
    final double[] bounds, final double... values) {
    final int numAxis = bounds.length / 2;
    final int count = Math.min(numAxis, values.length);
    for (int axisIndex = 0; axisIndex < count; axisIndex++) {
      final double value = values[axisIndex];
      expand(geometryFactory, bounds, numAxis, axisIndex, value);
    }
  }

  public static void expand(final GeometryFactory geometryFactory,
    final double[] bounds, final int axisIndex, double value) {
    if (geometryFactory != null) {
      value = geometryFactory.makePrecise(axisIndex, value);
    }
    final int numAxis = bounds.length / 2;
    final double min = bounds[axisIndex];
    if (value < min || Double.isNaN(min)) {
      bounds[axisIndex] = value;
    }
    final double max = bounds[numAxis + axisIndex];
    if (value > max || Double.isNaN(max)) {
      bounds[numAxis + axisIndex] = value;
    }
  }

  public static void expand(final GeometryFactory geometryFactory,
    final double[] bounds, final int numAxis, final int axisIndex, double value) {
    if (!Double.isNaN(value)) {
      if (geometryFactory != null) {
        value = geometryFactory.makePrecise(axisIndex, value);
      }
      final double min = bounds[axisIndex];
      if (value < min || Double.isNaN(min)) {
        bounds[axisIndex] = value;
      }
      final double max = bounds[numAxis + axisIndex];
      if (value > max || Double.isNaN(max)) {
        bounds[numAxis + axisIndex] = value;
      }
    }
  }

  public static void expandX(final double[] bounds, final int numAxis,
    final double value) {
    expand(bounds, numAxis, 0, value);
  }

  public static void expandY(final double[] bounds, final int numAxis,
    final double value) {
    expand(bounds, numAxis, 1, value);
  }

  public static void expandZ(final double[] bounds, final int numAxis,
    final double value) {
    expand(bounds, numAxis, 2, value);
  }

  public static double getMax(final double[] bounds, final int axisIndex) {
    if (bounds == null) {
      return Double.NaN;
    } else {
      final int numAxis = bounds.length / 2;
      if (axisIndex < 0 || axisIndex > numAxis) {
        return Double.NaN;
      } else {
        final double max = bounds[numAxis + axisIndex];
        return max;
      }
    }
  }

  public static double getMin(final double[] bounds, final int axisIndex) {
    if (bounds == null) {
      return Double.NaN;
    } else {
      final int numAxis = bounds.length / 2;
      if (axisIndex < 0 || axisIndex > numAxis) {
        return Double.NaN;
      } else {
        final double min = bounds[axisIndex];
        return min;
      }
    }
  }
}
