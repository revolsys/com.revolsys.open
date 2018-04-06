package com.revolsys.elevation.gridded;

import java.util.Arrays;
import java.util.function.DoubleConsumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.LineStringEditor;

public class FloatArrayGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final float NULL_VALUE = -Float.MAX_VALUE;

  private final float[] elevations;

  public FloatArrayGriddedElevationModel(final double x, final double y, final int gridWidth,
    final int gridHeight, final double gridCellSize, final float[] elevations) {
    this(GeometryFactory.DEFAULT_3D, x, y, gridWidth, gridHeight, gridCellSize, elevations);
  }

  public FloatArrayGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final double gridCellSize, final float[] elevations) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize);
    this.elevations = elevations;
  }

  public FloatArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final double gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    final int size = gridWidth * gridHeight;
    final float[] elevations = new float[size];
    Arrays.fill(elevations, NULL_VALUE);
    this.elevations = elevations;
  }

  public FloatArrayGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final double gridCellSize,
    final float[] elevations) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = elevations;
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  protected void expandZ() {
    float min = Float.MAX_VALUE;
    float max = -Float.MAX_VALUE;
    for (final float elevation : this.elevations) {
      if (elevation != NULL_VALUE) {
        if (elevation < min) {
          min = elevation;
        }
        if (elevation > max) {
          max = elevation;
        }
      }
    }
    final double minZ = min;
    final double maxZ = max;
    setZRange(minZ, maxZ);

  }

  @Override
  public void forEachElevationFinite(final DoubleConsumer action) {
    for (final float elevation : this.elevations) {
      if (elevation != NULL_VALUE) {
        action.accept(elevation);
      }
    }
  }

  @Override
  public double getElevationFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final float elevation = this.elevations[index];
    if (elevation == NULL_VALUE) {
      return Double.NaN;
    } else {
      return elevation;
    }
  }

  @Override
  public LineStringEditor getNullBoundaryPoints() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LineStringEditor points = new LineStringEditor(geometryFactory);
    final double minX = getGridMinX();
    final double minY = getGridMinY();

    final double gridCellSize = getGridCellSize();
    final int gridHeight = getGridHeight();
    final int gridWidth = getGridWidth();
    final float[] elevations = this.elevations;
    int index = 0;
    final int[] offsets = {
      -1, 0, 1
    };
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final float elevation = elevations[index];
        if (elevation == NULL_VALUE) {
          int countZ = 0;
          double sumZ = 0;
          for (final int offsetY : offsets) {
            if (!(gridY == 0 && offsetY == -1 || gridY == gridHeight - 1 && offsetY == 1)) {
              final int offsetIndex = index + offsetY * gridWidth;
              for (final int offsetX : offsets) {
                if (!(gridX == 0 && offsetX == -1 || gridX == gridWidth - 1 && offsetX == 1)) {
                  final float elevationNeighbour = elevations[offsetIndex + offsetX];
                  if (elevationNeighbour != NULL_VALUE) {
                    sumZ += elevationNeighbour;
                    countZ++;
                  }
                }
              }
            }
          }

          if (countZ > 0) {
            final double x = minX + gridCellSize * gridX;
            final double y = minY + gridCellSize * gridY;
            final double z = sumZ / countZ;
            points.appendVertex(x, y, z);
          }
        }
        index++;
      }
    }
    return points;
  }

  @Override
  public boolean hasElevationFast(final int gridX, final int gridY) {
    final int index = gridY * this.gridWidth + gridX;
    final float elevation = this.elevations[index];
    if (elevation == NULL_VALUE) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  public FloatArrayGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final double cellSize) {
    return new FloatArrayGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public GriddedElevationModel resample(final int newGridCellSize) {
    final double gridCellSize = getGridCellSize();
    final double cellRatio = gridCellSize / newGridCellSize;
    final int step = (int)Math.round(1 / cellRatio);
    final int gridWidth = getGridWidth();
    final int gridHeight = getGridHeight();

    final int newGridWidth = (int)Math.round(gridWidth * cellRatio);
    final int newGridHeight = (int)Math.round(gridHeight * cellRatio);

    final GeometryFactory geometryFactory = getGeometryFactory();
    final float[] oldElevations = this.elevations;
    final float[] newElevations = new float[newGridWidth * newGridHeight];

    int newIndex = 0;
    for (int gridYMin = 0; gridYMin < gridHeight; gridYMin += step) {
      final int gridYMax = gridYMin + step;
      for (int gridXMin = 0; gridXMin < gridWidth; gridXMin += step) {
        final int gridXMax = gridXMin + step;
        int count = 0;
        double sum = 0;
        for (int gridY = gridYMin; gridY < gridYMax; gridY++) {
          for (int gridX = gridXMin; gridX < gridXMax; gridX++) {
            final float elevation = oldElevations[gridY * gridWidth + gridX];
            if (elevation != NULL_VALUE) {
              count++;
              sum += elevation;
            }
          }
        }
        if (count > 0) {
          newElevations[newIndex] = (float)(sum / count);
        } else {
          newElevations[newIndex] = NULL_VALUE;
        }
        newIndex++;
      }
    }
    final BoundingBox boundingBox = getBoundingBox();

    final GriddedElevationModel newDem = new FloatArrayGriddedElevationModel(geometryFactory,
      boundingBox, newGridWidth, newGridHeight, newGridCellSize, newElevations);
    return newDem;
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final double elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final float elevationFloat = (float)elevation;
      this.elevations[index] = elevationFloat;
      clearCachedObjects();
    }
  }

  @Override
  public void setElevationNull(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      this.elevations[index] = NULL_VALUE;
      clearCachedObjects();
    }
  }
}
