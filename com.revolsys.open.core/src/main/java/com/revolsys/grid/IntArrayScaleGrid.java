package com.revolsys.grid;

import java.util.Arrays;
import java.util.function.DoubleConsumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class IntArrayScaleGrid extends AbstractGrid {
  private static final int NULL_VALUE = Integer.MIN_VALUE;

  protected final int[] cells;

  public IntArrayScaleGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellSize, final int[] cells) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize);
    this.cells = cells;
  }

  public IntArrayScaleGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    final int size = gridWidth * gridHeight;
    final int[] cells = new int[size];
    Arrays.fill(cells, NULL_VALUE);
    this.cells = cells;
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.cells, NULL_VALUE);
  }

  @Override
  public void forEachValueFinite(final DoubleConsumer action) {
    for (final int elevation : this.cells) {
      if (elevation != NULL_VALUE) {
        final double value = toDoubleZ(elevation);
        action.accept(value);
      }
    }
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final int elevationInt = this.cells[index];
    if (elevationInt == NULL_VALUE) {
      return Double.NaN;
    } else {
      return toDoubleZ(elevationInt);
    }
  }

  public int getValueInt(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      return this.cells[index];
    } else {
      return NULL_VALUE;
    }
  }

  @Override
  public boolean hasValueFast(final int gridX, final int gridY) {
    final int gridWidth1 = this.gridWidth;
    final int index = gridY * gridWidth1 + gridX;
    final int elevationInt = this.cells[index];
    if (elevationInt == NULL_VALUE) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public IntArrayScaleGrid newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double cellSize) {
    return new IntArrayScaleGrid(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public Grid resample(final int newGridCellSize) {
    final double gridCellSize = getGridCellSize();
    final double cellRatio = gridCellSize / newGridCellSize;
    final int step = (int)Math.round(1 / cellRatio);
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();

    final int newGridWidth = (int)Math.round(gridWidth * cellRatio);
    final int newGridHeight = (int)Math.round(gridHeight * cellRatio);

    final GeometryFactory geometryFactory = getGeometryFactory();
    final int[] oldValues = this.cells;
    final int[] newValues = new int[newGridWidth * newGridHeight];

    int newIndex = 0;
    for (int gridYMin = 0; gridYMin < gridHeight; gridYMin += step) {
      final int gridYMax = gridYMin + step;
      for (int gridXMin = 0; gridXMin < gridWidth; gridXMin += step) {
        final int gridXMax = gridXMin + step;
        int count = 0;
        long sum = 0;
        for (int gridY = gridYMin; gridY < gridYMax; gridY++) {
          for (int gridX = gridXMin; gridX < gridXMax; gridX++) {
            final int elevation = oldValues[gridY * gridWidth + gridX];
            if (elevation != NULL_VALUE) {
              count++;
              sum += elevation;
            }
          }
        }
        if (count > 0) {
          newValues[newIndex] = (int)(sum / count);
        } else {
          newValues[newIndex] = NULL_VALUE;
        }
        newIndex++;
      }
    }
    final BoundingBox boundingBox = getBoundingBox();

    return new IntArrayScaleGrid(geometryFactory, boundingBox, newGridWidth, newGridHeight,
      newGridCellSize, newValues);
  }

  @Override
  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory.getScaleZ() <= 0) {
      throw new IllegalArgumentException("Geometry factory must have a z scale factor");
    }
    super.setGeometryFactory(geometryFactory);
  }

  @Override
  public void setValue(final int gridX, final int gridY, final double elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final int elevationInt = geometryFactory.toIntZ(elevation);
      this.cells[index] = elevationInt;
      clearCachedObjects();
    }
  }

  @Override
  public void setValueNull(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      this.cells[index] = NULL_VALUE;
      clearCachedObjects();
    }
  }

}
