package com.revolsys.elevation.gridded;

import java.util.Arrays;
import java.util.function.DoubleConsumer;

import com.revolsys.geometry.model.GeometryFactory;

public class DoubleArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final double NULL_VALUE = Double.NaN;

  private final double[] elevations;

  public DoubleArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final double gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = new double[gridWidth * gridHeight];
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  protected void expandZ() {
    for (final double elevation : this.elevations) {
      expandZ(elevation);
    }
  }

  @Override
  public void forEachElevationFinite(final DoubleConsumer action) {
    for (final double z : this.elevations) {
      if (Double.isFinite(z)) {
        action.accept(z);
      }
    }
  }

  @Override
  protected double getElevationDo(final int x, final int y, final int gridWidth) {
    final int index = y * gridWidth + x;
    final double elevation = this.elevations[index];
    return elevation;
  }

  @Override
  public DoubleArrayGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final double cellSize) {
    return new DoubleArrayGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final double elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      if (Double.isFinite(elevation)) {
        this.elevations[index] = elevation;
      } else {
        this.elevations[index] = NULL_VALUE;
      }
      clearCachedObjects();
    }
  }

  @Override
  public void setElevationNull(final int gridX, final int gridY) {
    setElevation(gridX, gridY, NULL_VALUE);
  }
}
