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

  private int gridCellSize;

  private Resource resource;

  private GriddedElevationModelImage image;

  private double minX;

  private double minY;

  private GeometryFactory geometryFactory;

  public AbstractGriddedElevationModel() {
  }

  public AbstractGriddedElevationModel(final GeometryFactory geometryFactory, final double minX,
    final double minY, final int gridWidth, final int gridHeight, final int gridCellSize) {
    this.gridWidth = gridWidth;
    this.gridHeight = gridHeight;
    this.gridCellSize = gridCellSize;
    this.geometryFactory = geometryFactory;
    this.minX = minX;
    this.minY = minY;
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      final double maxX = this.minX + (double)this.gridWidth * this.gridCellSize;
      final double maxY = this.minY + (double)this.gridHeight * this.gridCellSize;
      this.boundingBox = this.geometryFactory.boundingBox(this.minX, this.minY, maxX, maxY);
    }
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
