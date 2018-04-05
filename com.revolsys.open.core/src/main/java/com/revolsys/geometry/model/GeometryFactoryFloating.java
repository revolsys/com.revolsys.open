package com.revolsys.geometry.model;

import java.util.Arrays;

import com.revolsys.geometry.cs.CoordinateSystem;

public class GeometryFactoryFloating extends GeometryFactory {

  private static final long serialVersionUID = 1L;

  public GeometryFactoryFloating(final CoordinateSystem coordinateSystem, final int axisCount) {
    super(coordinateSystem, axisCount);
  }

  public GeometryFactoryFloating(final CoordinateSystem coordinateSystem,
    final int coordinateSystemId, final int axisCount) {
    super(coordinateSystem, coordinateSystemId, axisCount);
  }

  public GeometryFactoryFloating(final int coordinateSystemId, final int axisCount) {
    super(coordinateSystemId, axisCount);
  }

  @Override
  public GeometryFactory convertCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return this;
    } else {
      final CoordinateSystem coordinateSystemThis = getCoordinateSystem();
      if (coordinateSystem == coordinateSystemThis) {
        return this;
      } else {
        return coordinateSystem.getGeometryFactoryFloating(this.axisCount);
      }
    }
  }

  @Override
  public GeometryFactory convertToFixed(final double defaultScale) {
    final double[] scales = new double[this.axisCount];
    Arrays.fill(scales, defaultScale);
    return convertScales(scales);
  }
}
