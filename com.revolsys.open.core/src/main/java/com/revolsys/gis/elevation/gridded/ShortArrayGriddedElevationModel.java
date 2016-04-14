package com.revolsys.gis.elevation.gridded;

import java.util.Arrays;

import com.revolsys.geometry.model.GeometryFactory;

public class ShortArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  public static final short NULL_VALUE = Short.MIN_VALUE;

  private final short[] elevations;

  public ShortArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final int cellSize) {
    super(geometryFactory, x, y, width, height, cellSize);
    this.elevations = new short[width * height];
  }

  @Override
  public void clear() {
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  public short getElevationShort(final int x, final int y) {
    final int width = getWidth();
    final int height = getHeight();
    if (x >= 0 && x < width && y >= 0 && y < height) {
      final short elevation = this.elevations[y * height + x];
      return elevation;
    } else {
      return NULL_VALUE;
    }
  }

  @Override
  public boolean isNull(final int x, final int y) {
    final short elevation = getElevationShort(x, y);
    return elevation == NULL_VALUE;
  }

  @Override
  public void setElevation(final int x, final int y, final short elevation) {
    final int width = getWidth();
    final int height = getHeight();
    if (x >= 0 && x < width && y >= 0 && y < height) {
      this.elevations[y * height + x] = elevation;
    }
  }

  @Override
  public void setElevationNull(final int x, final int y) {
    setElevation(x, y, NULL_VALUE);
  }
}
