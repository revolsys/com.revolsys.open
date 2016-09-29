package com.revolsys.elevation.gridded;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractGriddedElevationModel extends BaseObjectWithProperties
  implements GriddedElevationModel {
  private BoundingBox boundingBox;

  private int gridHeight;

  private int gridWidth;

  private final int gridCellSize;

  private Resource resource;

  private GriddedElevationModelImage image;

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double x,
    final double y, final int gridWidth, final int gridHeight, final int gridCellSize) {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.gridCellSize = gridCellSize;
    final double x1 = x;
    final double y1 = y;
    final double x2 = x1 + (double)gridWidth * gridCellSize;
    final double y2 = y1 + (double)gridHeight * gridCellSize;
    this.boundingBox = geometryFactory.boundingBox(x1, y1, x2, y2);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public int getGridCellSize() {
    return this.gridCellSize;
  }

  @Override
  public int getGridHeight() {
    return this.gridHeight;
  }

  @Override
  public int getGridWidth() {
    return this.gridWidth;
  }

  @Override
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
  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setGridHeight(final int gridHeight) {
    this.gridHeight = gridHeight;
  }

  public void setGridWidth(final int gridWidth) {
    this.gridWidth = gridWidth;
  }

  @Override
  public void setResource(final Resource resource) {
    this.resource = resource;
  }
}
