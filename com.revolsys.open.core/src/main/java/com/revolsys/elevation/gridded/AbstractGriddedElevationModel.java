package com.revolsys.elevation.gridded;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractGriddedElevationModel extends BaseObjectWithProperties
  implements GriddedElevationModel {
  private BoundingBox boundingBox;

  private int height;

  private int width;

  private final int cellSize;

  private Resource resource;

  private GriddedElevationModelImage image;

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int width, final int height, final int cellSize) {
    this.width = width;
    this.height = height;
    this.cellSize = cellSize;
    final double x1 = x;
    final double y1 = y;
    final double x2 = x1 + (double)width * cellSize;
    final double y2 = y1 + (double)height * cellSize;
    this.boundingBox = geometryFactory.boundingBox(x1, y1, x2, y2);
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

  public GriddedElevationModelImage getImage() {
    if (this.image == null) {
      this.image = new GriddedElevationModelImage(this);
    }
    return this.image;
  }

  @Override
  public Resource getResource() {
    return this.resource;
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

  @Override
  public void setResource(final Resource resource) {
    this.resource = resource;
  }

  public void setWidth(final int width) {
    this.width = width;
  }
}
