package com.revolsys.gis.elevation.gridded;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.properties.BaseObjectWithProperties;

public abstract class AbstractGriddedElevationModel extends BaseObjectWithProperties
  implements GriddedElevationModel {
  private BoundingBox boundingBox;

  private int height;

  private int width;

  private final int cellSize;

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final int cellSize) {
    this.width = width;
    this.height = height;
    this.cellSize = cellSize;
    final double x1 = x;
    final double y1 = y;
    final double x2 = x1 + (double)width * cellSize;
    final double y2 = y1 + (double)height * cellSize;
    this.boundingBox = geometryFactory.newBoundingBox(x1, y1, x2, y2);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public int getCellSize() {
    return this.cellSize;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setHeight(final int height) {
    this.height = height;
  }

  public void setWidth(final int width) {
    this.width = width;
  }
}
