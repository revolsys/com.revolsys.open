package com.revolsys.elevation.gridded;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevationWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class IntArrayScaleGriddedElevationModel extends AbstractGriddedElevationModel {
  private static final int NULL_VALUE = Integer.MIN_VALUE;

  private final int[] elevations;

  private final double scaleFactorZ;

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final int gridWidth, final int gridHeight,
    final int gridCellSize, final double scaleFactorZ, final int[] elevations) {
    super(geometryFactory, boundingBox, gridWidth, gridHeight, gridCellSize);
    this.elevations = elevations;
    this.scaleFactorZ = scaleFactorZ;
  }

  public IntArrayScaleGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final int gridCellSize,
    final double scaleFactorZ) {
    super(geometryFactory, x, y, gridWidth, gridHeight, gridCellSize);
    this.elevations = new int[gridWidth * gridHeight];
    Arrays.fill(this.elevations, NULL_VALUE);
    this.scaleFactorZ = scaleFactorZ;
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
        final double elevation = elevationInt / this.scaleFactorZ;
        expandZ(elevation);
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
        return elevationInt / this.scaleFactorZ;
      }
    }
    return Double.NaN;
  }

  @Override
  public IntArrayScaleGriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int cellSize) {
    return new IntArrayScaleGriddedElevationModel(geometryFactory, x, y, width, height, cellSize,
      this.scaleFactorZ);
  }

  @Override
  public void setElevation(final int gridX, final int gridY, final double elevation) {
    final int width = getGridWidth();
    final int height = getGridHeight();
    if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
      final int index = gridY * width + gridX;
      int elvationInt;
      if (Double.isFinite(elevation)) {
        elvationInt = (int)(elevation * this.scaleFactorZ);
      } else {
        elvationInt = NULL_VALUE;
      }
      this.elevations[index] = elvationInt;
      clearCachedObjects();
    }
  }

  public void writeIntArray(final CompactBinaryGriddedElevationWriter writer,
    final ByteBuffer buffer) throws IOException {
    final int gridWidth = getGridWidth();
    int index = 0;
    final int gridHeight = getGridHeight();
    for (int gridY = 0; gridY < gridHeight; gridY++) {
      for (int gridX = 0; gridX < gridWidth; gridX++) {
        final int elevation = this.elevations[index++];
        buffer.putInt(elevation);
      }
      writer.writeBuffer();
    }
  }
}
