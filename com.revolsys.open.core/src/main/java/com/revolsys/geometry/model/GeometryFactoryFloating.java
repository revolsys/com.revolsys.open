package com.revolsys.geometry.model;

import java.util.Arrays;

import com.revolsys.geometry.cs.CoordinateSystem;

public class GeometryFactoryFloating extends GeometryFactory {

  private static final long serialVersionUID = 1L;

  protected GeometryFactoryFloating(final CoordinateSystem coordinateSystem, final int axisCount) {
    super(coordinateSystem, axisCount);
  }

  protected GeometryFactoryFloating(final CoordinateSystem coordinateSystem,
    final int coordinateSystemId, final int axisCount) {
    super(coordinateSystem, coordinateSystemId, axisCount);
  }

  protected GeometryFactoryFloating(final int coordinateSystemId, final int axisCount) {
    super(coordinateSystemId, axisCount);
  }

  @Override
  public GeometryFactory convertToFixed(final double defaultScale) {
    final double[] scales = new double[this.axisCount];
    Arrays.fill(scales, defaultScale);
    return convertScales(scales);
  }
}
