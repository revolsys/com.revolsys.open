package com.revolsys.elevation.gridded;

import java.util.Arrays;

import com.revolsys.collection.range.DoubleMinMax;
import com.revolsys.geometry.model.GeometryFactory;

public class FloatArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final float NULL_VALUE = Float.NaN;

  private final float[] elevations;

  public FloatArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final int gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = new float[gridWidth * gridHeight];
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  protected void expandMinMax(final DoubleMinMax minMax) {
    for (final float elevation : this.elevations) {
      if (Float.isFinite(elevation)) {
        minMax.add(elevation);
      }
    }
  }

  @Override
  public double getElevation(final int x, final int y) {
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
  public FloatArrayGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int cellSize) {
    return new FloatArrayGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final double elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      float elevationFloat;
      if (Double.isFinite(elevation)) {
        elevationFloat = (float)elevation;
      } else {
        elevationFloat = NULL_VALUE;
      }
      this.elevations[index] = elevationFloat;
      clearCachedObjects();
    }
  }

  @Override
  public void setElevationNull(final int gridX, final int gridY) {
    setElevation(gridX, gridY, NULL_VALUE);
  }
}
