package com.revolsys.elevation.gridded;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevationWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.Buffers;

public class IntArrayScaleGriddedElevationModel extends AbstractGriddedElevationModel {
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
    final ByteBuffer buffer = ByteBuffer.allocateDirect(16392);
    int writtenCount = 0;
    final int[] elevations = this.elevations;
    for (final int elevation : elevations) {
      buffer.putInt(elevation);
      writtenCount += 4;
      if (writtenCount == 16392) {
        Buffers.writeAll(out, buffer);
        writtenCount = 0;
      }
    }
    if (writtenCount > 0) {
      Buffers.writeAll(out, buffer);
    }
  }
}
