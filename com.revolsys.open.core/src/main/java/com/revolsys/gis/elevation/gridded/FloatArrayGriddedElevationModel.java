package com.revolsys.gis.elevation.gridded;

import java.util.Arrays;

import com.revolsys.geometry.model.GeometryFactory;

public class FloatArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final float NULL_VALUE = Float.NaN;

  private final float[] elevations;

  public FloatArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final int cellSize) {
    super(geometryFactory, x, y, width, height, cellSize);
    this.elevations = new float[width * height];
  }

  @Override
  public void clear() {
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  public float getElevationFloat(final int x, final int y) {
    final int width = getWidth();
    final int height = getHeight();
    if (x >= 0 && x < width && y >= 0 && y < height) {
      final int index = y * width + x;
      final float elevation = this.elevations[index];
      return elevation;
    } else {
      return NULL_VALUE;
    }
  }

  @Override
  public short getElevationShort(final int x, final int y) {
    return (short)getElevationFloat(x, y);
  }

  @Override
  public boolean isNull(final int x, final int y) {
    final float elevation = getElevationFloat(x, y);
    return Float.isNaN(elevation);
  }

  @Override
  public void setElevation(final int x, final int y, final float elevation) {
    final int width = getWidth();
    final int height = getHeight();
    if (x >= 0 && x < width && y >= 0 && y < height) {
      final int index = y * width + x;
      this.elevations[index] = elevation;
    }
  }

  @Override
  public void setElevation(final int x, final int y, final short elevation) {
    setElevation(x, y, (float)elevation);
  }

  @Override
  public void setElevationNull(final int x, final int y) {
    setElevation(x, y, NULL_VALUE);
  }
}
