package com.revolsys.elevation.gridded.rasterizer;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;

public abstract class AbstractGriddedElevationModelRasterizer extends
  BaseObjectWithPropertiesAndChange implements GriddedElevationModelRasterizer, MapSerializer {

  protected GriddedElevationModel elevationModel;

  protected double maxZ = Double.NaN;

  protected double minZ = Double.NaN;

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
  public double getMaxZ() {
    return this.maxZ;
  }

  @Override
  public double getMinZ() {
    return this.minZ;
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
      if (Double.isNaN(this.minZ)) {
        this.minZ = this.elevationModel.getMinZ();
        this.maxZ = this.elevationModel.getMaxZ();
      }
    }
    updateValues();
  }

  @Override
  public void setMaxZ(final double maxZ) {
    this.maxZ = maxZ;
    updateValues();
  }

  @Override
  public void setMinZ(final double minZ) {
    this.minZ = minZ;
    updateValues();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap(this.type);
    return map;
  }

  protected void updateValues() {
  }

}
