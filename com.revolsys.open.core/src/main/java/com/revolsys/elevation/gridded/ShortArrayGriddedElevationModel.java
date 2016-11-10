package com.revolsys.elevation.gridded;

import java.util.Arrays;

import com.revolsys.geometry.model.GeometryFactory;

public class ShortArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  public static final short NULL_VALUE = Short.MIN_VALUE;

  private final short[] elevations;

  public ShortArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final int gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = new short[gridWidth * gridHeight];
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  public double getElevation(final int gridX, final int gridY) {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
      final int offset = gridY * gridHeight + gridX;
      final short elevation = this.elevations[offset];
      if (elevation != NULL_VALUE) {
        return elevation;
      }
    }
    return Double.NaN;
  }

  @Override
  public ShortArrayGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int cellSize) {
    return new ShortArrayGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final double elevation) {
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();
    if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
      short shortElevation;
      if (Double.isNaN(elevation)) {
        shortElevation = NULL_VALUE;
      } else {
        shortElevation = (short)elevation;
      }
      this.elevations[gridY * gridHeight + gridX] = shortElevation;
      clearCachedObjects();
    }
  }

}
