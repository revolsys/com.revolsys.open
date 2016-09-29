package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.revolsys.collection.range.ShortMinMax;
import com.revolsys.geometry.model.GeometryFactory;

public class ShortArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  public static final short NULL_VALUE = Short.MIN_VALUE;

  private final short[] elevations;

  private ShortMinMax minMax;

  public ShortArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final int cellSize) {
    super(geometryFactory, x, y, width, height, cellSize);
    this.elevations = new short[width * height];
  }

  @Override
  public void clear() {
    Arrays.fill(this.elevations, NULL_VALUE);
    this.minMax = null;
  }

  @Override
  public BufferedImage getBufferedImage() {
    // TODO Auto-generated method stub
    return null;
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

  public ShortMinMax getMinMax() {
    if (this.minMax == null) {
      this.minMax = ShortMinMax.newWithIgnore(NULL_VALUE, this.elevations);
    }
    return this.minMax;
  }

  @Override
  public boolean isEmpty() {
    final ShortMinMax minMax = getMinMax();
    return minMax.isEmpty();
  }

  @Override
  public boolean isNull(final int cellX, final int cellY) {
    final short elevation = getElevationShort(cellX, cellY);
    return elevation == NULL_VALUE;
  }

  @Override
  public ShortArrayGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int cellSize) {
    return new ShortArrayGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public void setElevation(final GriddedElevationModel elevationModel, final double x,
    final double y) {
    final short elevation = elevationModel.getElevationShort(x, y);
    setElevation(x, y, elevation);
  }

  @Override
  public void setElevation(final int cellX, final int cellY,
    final GriddedElevationModel elevationModel, final double x, final double y) {
    if (!elevationModel.isNull(x, y)) {
      final short elevation = elevationModel.getElevationShort(x, y);
      setElevation(cellX, cellY, elevation);
    }
  }

  @Override
  public void setElevation(final int cellX, final int cellY, final short elevation) {
    final int width = getWidth();
    final int height = getHeight();
    if (cellX >= 0 && cellX < width && cellY >= 0 && cellY < height) {
      this.elevations[cellY * height + cellX] = elevation;
      this.minMax = null;
    }
  }

  @Override
  public void setElevationNull(final int cellX, final int cellY) {
    setElevation(cellX, cellY, NULL_VALUE);
  }
}
