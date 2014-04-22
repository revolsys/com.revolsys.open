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
}
