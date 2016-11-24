package com.revolsys.elevation.gridded;

import com.revolsys.elevation.gridded.compactbinary.CompactBinaryGriddedElevation;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;

public abstract class DirectFileElevationModel extends AbstractGriddedElevationModel
  implements BaseCloseable {

  private int elevationByteCount;

  private boolean open = true;

  public DirectFileElevationModel() {
  }

  public DirectFileElevationModel(final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridWidth, final int gridHeight, final int gridCellSize,
    final int elevationByteCount) {
    super(geometryFactory, minX, minY, gridWidth, gridHeight, gridCellSize);
    this.elevationByteCount = elevationByteCount;
  }

  @Override
  public void clear() {
  }

  @Override
  public void close() {
    this.open = false;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }

  @Override
  public double getElevation(final int gridX, final int gridY) {
    final int gridWidth = getGridWidth();
    final int offset = CompactBinaryGriddedElevation.HEADER_SIZE
      + (gridY * gridWidth + gridX) * this.elevationByteCount;
    return readElevation(offset);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNull(final int x, final int y) {
    return false;
  }

  public boolean isOpen() {
    return this.open;
  }

  @Override
  public GriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int gridCellSize) {
    // TODO Auto-generated method stub
    return null;
  }

  protected abstract double readElevation(final int offset);

  @Override
  public void setElevation(final int gridX, final int gridY, final double elevation) {
    final int gridWidth = getGridWidth();
    final int offset = CompactBinaryGriddedElevation.HEADER_SIZE
      + (gridY * gridWidth + gridX) * this.elevationByteCount;
    writeElevation(offset, elevation);
  }

  protected abstract void writeElevation(int offset, double elevation);
}
