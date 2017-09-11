package com.revolsys.elevation.gridded.rasterizer;

import com.revolsys.beans.AbstractPropertyChangeSupportProxy;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;

public abstract class AbstractGriddedElevationModelRasterizer
  extends AbstractPropertyChangeSupportProxy implements GriddedElevationModelRasterizer {

  protected GriddedElevationModel elevationModel;

  protected int width;

  protected int height;

  public AbstractGriddedElevationModelRasterizer(final GriddedElevationModel elevationModel) {
    setElevationModel(elevationModel);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.elevationModel.getBoundingBox();
  }

  @Override
  public GriddedElevationModel getElevationModel() {
    return this.elevationModel;
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
  public void setElevationModel(final GriddedElevationModel elevationModel) {
    this.elevationModel = elevationModel;
    this.width = elevationModel.getGridWidth();
    this.height = elevationModel.getGridHeight();
  }

}
