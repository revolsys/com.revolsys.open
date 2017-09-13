package com.revolsys.elevation.gridded.rasterizer;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;

public abstract class AbstractGriddedElevationModelRasterizer extends
  BaseObjectWithPropertiesAndChange implements GriddedElevationModelRasterizer, MapSerializer {

  protected GriddedElevationModel elevationModel;

  protected int width;

  protected int height;

  private final String type;

  public AbstractGriddedElevationModelRasterizer(final String type) {
    this.type = type;
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
    if (elevationModel != null) {
      this.width = elevationModel.getGridWidth();
      this.height = elevationModel.getGridHeight();
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap(this.type);
    return map;
  }

}
