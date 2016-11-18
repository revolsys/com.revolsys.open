package com.revolsys.elevation.gridded;

import java.util.Arrays;

import com.revolsys.collection.range.DoubleMinMax;
import com.revolsys.geometry.model.GeometryFactory;

public class IntArrayScaleGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final int NULL_VALUE = Integer.MIN_VALUE;

  private final int[] elevations;

  private final double scaleFactor;

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final int gridCellSize,
    final double scaleFactor) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = new int[gridWidth * gridHeight];
    Arrays.fill(this.elevations, NULL_VALUE);
    this.scaleFactor = scaleFactor;
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  protected void expandMinMax(final DoubleMinMax minMax) {
    for (final int elevationInt : this.elevations) {
      if (elevationInt != NULL_VALUE) {
        final double elevation = elevationInt / this.scaleFactor;
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
      final int elevationInt = this.elevations[index];
      if (elevationInt != NULL_VALUE) {
        return elevationInt / this.scaleFactor;
      }
    }
    return Double.NaN;
  }

  @Override
  public IntArrayScaleGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int cellSize) {
    return new IntArrayScaleGriddedElevationModel(geometryFactory, x, y, width, height, cellSize,
      this.scaleFactor);
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final double elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      int elvationInt;
      if (Double.isFinite(elevation)) {
        elvationInt = (int)(elevation * this.scaleFactor);
      } else {
        elvationInt = NULL_VALUE;
      }
      this.elevations[index] = elvationInt;
      clearCachedObjects();
    }
  }

}
