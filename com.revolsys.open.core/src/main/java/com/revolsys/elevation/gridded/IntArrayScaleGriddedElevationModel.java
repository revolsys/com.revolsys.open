package com.revolsys.elevation.gridded;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevationWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class IntArrayScaleGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final int BUFFER_SIZE = 8192;

  private static final int NULL_VALUE = Integer.MIN_VALUE;

  private final int[] elevations;

  private double scaleZ;

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final int gridCellSize, final int[] elevations) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize);
    this.elevations = elevations;
  }

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final int gridCellSize) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = new int[gridWidth * gridHeight];
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  public void clear() {
    super.clear();
    Arrays.fill(this.elevations, NULL_VALUE);
  }

  @Override
  protected void expandZ() {
    for (final int elevationInt : this.elevations) {
      if (elevationInt != NULL_VALUE) {
        final double elevation = elevationInt / this.scaleZ;
        expandZ(elevation);
      }
    }
  }

  @Override
  protected double getElevationDo(final int gridX, final int gridY, final int gridWidth) {
    final int index = gridY * gridWidth + gridX;
    final int elevationInt = this.elevations[index];
    if (elevationInt != NULL_VALUE) {
      return elevationInt / this.scaleZ;
    }
    return Double.NaN;
  }

  @Override
  public IntArrayScaleGriddedElevationModel newElevationModel(GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int cellSize) {
    final double scaleZ = geometryFactory.getScaleZ();
    if (scaleZ <= 0) {
      geometryFactory = geometryFactory.convertScales(geometryFactory.getScaleXY(), this.scaleZ);
    }
    return new IntArrayScaleGriddedElevationModel(geometryFactory, x, y, width, height, cellSize);
  }

  @Override
  public GriddedElevationModel resample(final int newGridCellSize) {
    final int gridCellSize = getGridCellSize();
    final double cellRatio = (double)gridCellSize / newGridCellSize;
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
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      int elvationInt;
      if (Double.isFinite(elevation)) {
        elvationInt = (int)(elevation * this.scaleZ);
      } else {
        elvationInt = NULL_VALUE;
      }
      this.elevations[index] = elvationInt;
      clearCachedObjects();
    }
  }

  @Override
  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    super.setGeometryFactory(geometryFactory);
    this.scaleZ = geometryFactory.getScaleZ();
    if (this.scaleZ <= 0) {
      this.scaleZ = 1000;
    }
  }

  public void writeIntArray(final CompactBinaryGriddedElevationWriter writer,
    final WritableByteChannel out) throws IOException {
    final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    int bufferCount = 0;
    final int[] elevations = this.elevations;
    for (final int elevation : elevations) {
      buffer.putInt(elevation);
      bufferCount += 4;
      if (bufferCount == BUFFER_SIZE) {
        buffer.flip();
        int totalWritten = 0;
        do {
          final int written = out.write(buffer);
          totalWritten += written;
        } while (totalWritten < bufferCount);
        buffer.clear();
        bufferCount = 0;
      }
    }
    if (bufferCount > 0) {
      buffer.flip();
      int totalWritten = 0;
      do {
        final int written = out.write(buffer);
        totalWritten += written;
      } while (totalWritten < bufferCount);
    }
  }
}
