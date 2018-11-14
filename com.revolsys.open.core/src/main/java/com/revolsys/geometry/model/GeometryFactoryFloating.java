package com.revolsys.geometry.model;

import java.util.Arrays;

import com.revolsys.geometry.cs.CoordinateSystem;

public class GeometryFactoryFloating extends GeometryFactory {

  private static final long serialVersionUID = 1L;

  public GeometryFactoryFloating(final CoordinateSystem coordinateSystem, final int axisCount) {
    super(coordinateSystem, axisCount);
  }

  protected GeometryFactoryFloating(final int coordinateSystemId, final int axisCount) {
    super(coordinateSystemId, axisCount);
  }

  @Override
  public GeometryFactory convertCoordinateSystem(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return this;
    } else {
      if (coordinateSystem == this.coordinateSystem) {
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

  @Override
  public GeometryFactory newWithOffsets(final double offsetX, final double offsetY,
    final double offsetZ) {
    if (this.coordinateSystemId > 0) {
      return new GeometryFactoryWithOffsets(this.coordinateSystemId, offsetX, 0, offsetY, 0,
        offsetZ, 0);
    } else {
      return new GeometryFactoryWithOffsets(this.coordinateSystem, offsetX, 0, offsetY, 0, offsetZ,
        0);
    }
  }
}
