package com.revolsys.elevation.gridded;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevationWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.io.channels.ChannelWriter;

public class IntArrayScaleGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final int NULL_VALUE = Integer.MIN_VALUE;

  private final int[] elevations;

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final double gridCellSize, final int[] elevations) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize);
    this.elevations = elevations;
  }

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final double gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    final int size = gridWidth * gridHeight;
    final int[] elevations = new int[size];
    Arrays.fill(elevations, NULL_VALUE);
    this.elevations = elevations;
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  protected void expandZ() {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (final int elevationInt : this.elevations) {
      if (elevationInt != NULL_VALUE) {
        if (elevationInt < min) {
          min = elevationInt;
        }
        if (elevationInt > max) {
          max = elevationInt;
        }
      }
    }
    final double minZ = toDoubleZ(min);
    final double maxZ = toDoubleZ(max);
    setZRange(minZ, maxZ);

  }

  @Override
  protected double getElevationDo(final int gridX, final int gridY, final int gridWidth) {
    final int index = gridY * gridWidth + gridX;
    final int elevationInt = this.elevations[index];
    if (elevationInt == NULL_VALUE) {
      return Double.NaN;
    } else {
      return toDoubleZ(elevationInt);
    }
  }

  public int getElevationInt(final int gridX, final int gridY) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      return this.elevations[index];
    } else {
      return NULL_VALUE;
    }
  }

  @Override
  public List<Point> getNullBoundaryPoints() {
    final List<Point> points = new ArrayList<>();
    final double minX = getMinX();
    final double minY = getMinY();

    final double gridCellSize = getGridCellSize();
    final int gridHeight = getGridHeight();
    final int gridWidth = getGridWidth();
    final int[] elevations = this.elevations;
    int index = 0;
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final int elevation = elevations[index];
        if (elevation == NULL_VALUE) {
          boolean hasNeighbour = false;
          if (gridX == 0) {
            if (gridY == 0) {
              hasNeighbour = elevations[index + 1] != NULL_VALUE //
                || elevations[index + gridWidth] != NULL_VALUE //
                || elevations[index + gridWidth + 1] != NULL_VALUE//
              ;
            } else if (gridY == gridHeight - 1) {
              hasNeighbour = elevations[index - 1] != NULL_VALUE //
                || elevations[index - gridWidth - 1] != NULL_VALUE //
                || elevations[index - gridWidth] != NULL_VALUE //
              ;
            } else {
              hasNeighbour = elevations[index + 1] != NULL_VALUE //
                || elevations[index - gridWidth] != NULL_VALUE //
                || elevations[index - gridWidth + 1] != NULL_VALUE //
                || elevations[index + gridWidth] != NULL_VALUE //
                || elevations[index + gridWidth + 1] != NULL_VALUE//
              ;
            }
          } else if (gridX == gridWidth - 1) {
            if (gridY == 0) {
              hasNeighbour = elevations[index - 1] != NULL_VALUE //
                || elevations[index + gridWidth - 1] != NULL_VALUE //
                || elevations[index + gridWidth] != NULL_VALUE //
              ;

            } else if (gridY == gridHeight - 1) {
              hasNeighbour = elevations[index - 1] != NULL_VALUE //
                || elevations[index - gridWidth - 1] != NULL_VALUE //
                || elevations[index - gridWidth] != NULL_VALUE //
              ;
            } else {
              hasNeighbour = elevations[index - 1] != NULL_VALUE //
                || elevations[index - gridWidth - 1] != NULL_VALUE //
                || elevations[index - gridWidth] != NULL_VALUE //
                || elevations[index + gridWidth - 1] != NULL_VALUE //
                || elevations[index + gridWidth] != NULL_VALUE //
              ;
            }
          } else {
            if (gridY == 0) {
              hasNeighbour = elevations[index - 1] != NULL_VALUE //
                || elevations[index + 1] != NULL_VALUE //
                || elevations[index + gridWidth - 1] != NULL_VALUE //
                || elevations[index + gridWidth] != NULL_VALUE //
                || elevations[index + gridWidth + 1] != NULL_VALUE//
              ;

            } else if (gridY == gridHeight - 1) {
              hasNeighbour = elevations[index - 1] != NULL_VALUE //
                || elevations[index + 1] != NULL_VALUE //
                || elevations[index - gridWidth - 1] != NULL_VALUE //
                || elevations[index - gridWidth] != NULL_VALUE //
                || elevations[index - gridWidth + 1] != NULL_VALUE //
              ;
            } else {
              hasNeighbour = elevations[index - 1] != NULL_VALUE //
                || elevations[index + 1] != NULL_VALUE //
                || elevations[index - gridWidth - 1] != NULL_VALUE //
                || elevations[index - gridWidth] != NULL_VALUE //
                || elevations[index - gridWidth + 1] != NULL_VALUE //
                || elevations[index + gridWidth - 1] != NULL_VALUE //
                || elevations[index + gridWidth] != NULL_VALUE //
                || elevations[index + gridWidth + 1] != NULL_VALUE//
              ;
            }
          }
          if (hasNeighbour) {
            final double x = minX + gridCellSize * gridX;
            final double y = minY + gridCellSize * gridY;
            final PointDoubleXY point = new PointDoubleXY(x, y);
            points.add(point);
          }
        }
        index++;
      }
    }
    return points;
  }

  @Override
  public IntArrayScaleGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final double cellSize) {
    return new IntArrayScaleGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
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
    final int[] oldElevations = this.elevations;
    final int[] newElevations = new int[newGridWidth * newGridHeight];

    int newIndex = 0;
    for (int gridYMin = 0; gridYMin < gridHeight; gridYMin += step) {
      final int gridYMax = gridYMin + step;
      for (int gridXMin = 0; gridXMin < gridWidth; gridXMin += step) {
        final int gridXMax = gridXMin + step;
        int count = 0;
        long sum = 0;
        for (int gridY = gridYMin; gridY < gridYMax; gridY++) {
          for (int gridX = gridXMin; gridX < gridXMax; gridX++) {
            final int elevation = oldElevations[gridY * gridWidth + gridX];
            if (elevation != NULL_VALUE) {
              count++;
              sum += elevation;
            }
          }
        }
        if (count > 0) {
          newElevations[newIndex] = (int)(sum / count);
        } else {
          newElevations[newIndex] = NULL_VALUE;
        }
        newIndex++;
      }
    }
    final BoundingBox boundingBox = getBoundingBox();

    final GriddedElevationModel newDem = new IntArrayScaleGriddedElevationModel(geometryFactory,
      boundingBox, newGridWidth, newGridHeight, newGridCellSize, newElevations);
    return newDem;
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final double elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      final int elevationInt = geometryFactory.toIntZ(elevation);
      this.elevations[index] = elevationInt;
      clearCachedObjects();
    }
  }

  @Override
  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory.getScaleZ() <= 0) {
      throw new IllegalArgumentException("Geometry factory must have a z scale factor");
    }
    super.setGeometryFactory(geometryFactory);
  }

  public void writeIntArray(final CompactBinaryGriddedElevationWriter writer,
    final ChannelWriter out) throws IOException {
    final int[] elevations = this.elevations;
    for (final int elevation : elevations) {
      out.putInt(elevation);
    }
  }
}
