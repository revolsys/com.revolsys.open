package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.range.FloatMinMax;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.number.Doubles;

public class FloatArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final float NULL_VALUE = Float.NaN;

  private final float[] elevations;

  private FloatMinMax minMax;

  public FloatArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final int cellSize) {
    super(geometryFactory, x, y, width, height, cellSize);
    this.elevations = new float[width * height];
  }

  @Override
  public void clear() {
    Arrays.fill(this.elevations, NULL_VALUE);
    clearCachedObjects();
  }

  private void clearCachedObjects() {
    this.minMax = null;
  }

  @Override
  public BufferedImage getBufferedImage() {
    final FloatMinMax minMax = getMinMax();
    final float min = minMax.getMin();
    final float range = minMax.getRange();
    final float multiple = 1.0f / range;
    final float minMultiple = min * multiple;
    final int width = getWidth();
    final int height = getHeight();
    int i = 0;
    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final float elevation = this.elevations[i];
        if (Doubles.equal(elevation, NULL_VALUE)) {
          image.setRGB(x, y, WebColors.colorToRGB(0, 0, 0, 0));
        } else {
          final float elevationMultiple = elevation * multiple;
          final float elevationPercent = elevationMultiple - minMultiple;
          final int grey = Math.round(elevationPercent * 255);
          final int color = WebColors.colorToRGB(255, grey, grey, grey);
          image.setRGB(x, y, color);
        }
        i++;
      }
    }
    return image;
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

  public FloatMinMax getMinMax() {
    if (this.minMax == null) {
      this.minMax = FloatMinMax.newWithIgnore(NULL_VALUE, this.elevations);
    }
    return this.minMax;
  }

  @Override
  public boolean isEmpty() {
    final FloatMinMax minMax = getMinMax();
    return minMax.isEmpty();
  }

  @Override
  public boolean isNull(final double x, final double y) {
    final float elevation = getElevationFloat(x, y);
    return Float.isNaN(elevation);
  }

  @Override
  public boolean isNull(final int x, final int y) {
    final float elevation = getElevationFloat(x, y);
    return Float.isNaN(elevation);
  }

  @Override
  public FloatArrayGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int cellSize) {
    return new FloatArrayGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public void setElevation(final GriddedElevationModel elevationModel, final double x,
    final double y) {
    final int cellX = getCellX(x);
    final int cellY = getCellY(x);
    if (elevationModel.isNull(x, y)) {
      elevationModel.setElevationNull(cellX, cellY);
    } else {
      final float elevation = elevationModel.getElevationFloat(x, y);
      setElevation(cellX, cellY, elevation);
    }
  }

  @Override
  public void setElevation(final int cellX, final int cellY, final float elevation) {
    final int width = getWidth();
    final int height = getHeight();
    if (cellX >= 0 && cellX < width && cellY >= 0 && cellY < height) {
      final int index = cellY * width + cellX;
      this.elevations[index] = elevation;
      clearCachedObjects();
    }
  }

  @Override
  public void setElevation(final int cellX, final int cellY,
    final GriddedElevationModel elevationModel, final double x, final double y) {
    if (!elevationModel.isNull(x, y)) {
      final float elevation = elevationModel.getElevationFloat(x, y);
      setElevation(cellX, cellY, elevation);
    }
  }

  @Override
  public void setElevation(final int cellX, final int cellY, final short elevation) {
    setElevation(cellX, cellY, (float)elevation);
  }

  @Override
  public void setElevationNull(final int cellX, final int cellY) {
    setElevation(cellX, cellY, NULL_VALUE);
  }
}
