package com.revolsys.elevation.gridded;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.range.FloatMinMax;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.number.Floats;

public class FloatArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final float NULL_VALUE = Float.NaN;

  private final float[] elevations;

  private FloatMinMax minMax;

  public FloatArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final int gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = new float[gridWidth * gridHeight];
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
    final int width = getGridWidth();
    final int height = getGridHeight();
    int i = 0;
    final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    // Images are from top left as opposed to bottom left
    for (int y = height - 1; y >= 0; y--) {
      for (int x = 0; x < width; x++) {
        final float elevation = this.elevations[i];
        if (Floats.equal(elevation, NULL_VALUE)) {
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
    final int width = getGridWidth();
    final int height = getGridHeight();
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
    final int gridX = getGridCellX(x);
    final int gridY = getGridCellY(y);
    if (elevationModel.isNull(x, y)) {
      elevationModel.setElevationNull(gridX, gridY);
    } else {
      final float elevation = elevationModel.getElevationFloat(x, y);
      setElevation(gridX, gridY, elevation);
    }
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final float elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      this.elevations[index] = elevation;
      clearCachedObjects();
    }
  }

  @Override
  public void setElevation(final int gridX, final int gridY,
    final GriddedElevationModel elevationModel, final double x, final double y) {
    if (!elevationModel.isNull(x, y)) {
      final float elevation = elevationModel.getElevationFloat(x, y);
      setElevation(gridX, gridY, elevation);
    }
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final short elevation) {
    setElevation(gridX, gridY, (float)elevation);
  }

  @Override
  public void setElevationNull(final int gridX, final int gridY) {
    setElevation(gridX, gridY, NULL_VALUE);
  }
}
