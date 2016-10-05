package com.revolsys.elevation.gridded.compactbinary;

import com.revolsys.elevation.gridded.AbstractGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.GeometryFactory;

public abstract class DirectFileElevationModel extends AbstractGriddedElevationModel {

  private int elevationByteCount;

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
  public double getElevationDouble(final int gridX, final int gridY) {
    final int gridWidth = getGridWidth();
    final int offset = CompactBinaryGriddedElevation.HEADER_SIZE
      + (gridX * gridWidth + gridY) * this.elevationByteCount;
    return readElevation(offset);
  }

  @Override
  public float getElevationFloat(final int x, final int y) {
    final double elevation = getElevationDouble(x, y);
    if (Double.isNaN(elevation)) {
      return Short.MIN_VALUE;
    } else {
      return (short)elevation;
    }
  }

  @Override
  public short getElevationShort(final double x, final double y) {
    final double elevation = getElevationDouble(x, y);
    if (Double.isNaN(elevation)) {
      return Short.MIN_VALUE;
    } else {
      return (short)elevation;
    }
  }

  @Override
  public short getElevationShort(final int x, final int y) {
    final double elevation = getElevationDouble(x, y);
    if (Double.isNaN(elevation)) {
      return Short.MIN_VALUE;
    } else {
      return (short)elevation;
    }
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNull(final int x, final int y) {
    return false;
  }

  @Override
  public GriddedElevationModel newElevationModel(final GeometryFactory geometryFactory,
    final double x, final double y, final int width, final int height, final int gridCellSize) {
    // TODO Auto-generated method stub
    return null;
  }

  protected abstract double readElevation(final int offset);

  @Override
  public void setElevation(final GriddedElevationModel elevationModel, final double x,
    final double y) {
  }

  @Override
  public void setElevation(final int gridX, final int gridY,
    final GriddedElevationModel elevationModel, final double x, final double y) {
  }

  @Override
  public void setElevation(final int x, final int y, final short elevation) {
  }

  @Override
  public void setElevationNull(final int x, final int y) {
  }

}
