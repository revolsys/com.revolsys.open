package com.revolsys.grid;

import java.util.Arrays;
import java.util.function.DoubleConsumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class FloatArrayGrid extends AbstractGrid {
  private static final float NULL_VALUE = -Float.MAX_VALUE;

  private final float[] values;

  public FloatArrayGrid(final double x, final double y, final int gridWidth, final int gridHeight,
    final double gridCellSize, final float[] values) {
    this(GeometryFactory.DEFAULT_3D, x, y, gridWidth, gridHeight, gridCellSize, values);
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final BoundingBox boundingBox,
    final int gridWidth, final int gridHeight, final double gridCellSize, final float[] values) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize);
    this.values = values;
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    final int size = gridWidth * gridHeight;
    final float[] values = new float[size];
    Arrays.fill(values, NULL_VALUE);
    this.values = values;
  }

  public FloatArrayGrid(final GeometryFactory geometryFactory, final double x, final double y,
    final int gridWidth, final int gridHeight, final double gridCellSize, final float[] values) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.values = values;
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.values, NULL_VALUE);
  }

  @Override
  protected void expandRange() {
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (final float value : this.values) {
      if (value != NULL_VALUE) {
        if (value < min) {
          min = value;
        }
        if (value > max) {
          max = value;
        }
      }
    }
    final double minZ = min;
    final double maxZ = max;
    setZRange(minZ, maxZ);

  }

  @Override
  public void forEachValueFinite(final DoubleConsumer action) {
    for (final float value : this.values) {
      if (value != NULL_VALUE) {
        action.accept(value);
      }
    }
  }

  @Override
  public double getValueFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final float value = this.values[index];
    if (value == NULL_VALUE) {
      return Double.NaN;
    } else {
      return value;
    }
  }

  @Override
  public boolean hasValueFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final float value = this.values[index];
    if (value == NULL_VALUE) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public FloatArrayGrid newGrid(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final double cellSize) {
    return new FloatArrayGrid(geometryFactory, x, y, width, height, cellSize);
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
    final float[] oldValues = this.values;
    final float[] newValues = new float[newGridWidth * newGridHeight];

    int newIndex = 0;
    for (int gridYMin = 0; gridYMin < gridHeight; gridYMin += step) {
      final int gridYMax = gridYMin + step;
      for (int gridXMin = 0; gridXMin < gridWidth; gridXMin += step) {
        final int gridXMax = gridXMin + step;
        int count = 0;
        double sum = 0;
        for (int gridY = gridYMin; gridY < gridYMax; gridY++) {
          for (int gridX = gridXMin; gridX < gridXMax; gridX++) {
            final float value = oldValues[gridY * gridWidth + gridX];
            if (value != NULL_VALUE) {
              count++;
              sum += value;
            }
          }
        }
        if (count > 0) {
          newValues[newIndex] = (float)(sum / count);
        } else {
          newValues[newIndex] = NULL_VALUE;
        }
        newIndex++;
      }
    }
    final BoundingBox boundingBox = getBoundingBox();

    return new FloatArrayGrid(geometryFactory, boundingBox, newGridWidth, newGridHeight,
      newGridCellSize, newValues);
  }

  @Override
  public void setValue(final int gridX, final int gridY, final double value) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final float valueFloat = (float)value;
      this.values[index] = valueFloat;
      clearCachedObjects();
    }
  }

  @Override
  public void setValueNull(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      this.values[index] = NULL_VALUE;
      clearCachedObjects();
    }
  }
}
