package com.revolsys.elevation.gridded.rasterizer;

import com.revolsys.awt.WebColors;
import com.revolsys.elevation.gridded.GriddedElevationModel;

public class GreyscaleRasterizer extends AbstractGriddedElevationModelRasterizer {

  private static final int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  private double colourGreyMultiple;

  private double minColourMultiple;

  private double minZ;

  private double maxZ;

  public GreyscaleRasterizer(final GriddedElevationModel elevationModel) {
    super(elevationModel);
    updateCachedFields();
  }

  public double getMaxZ() {
    return this.maxZ;
  }

  public double getMinZ() {
    return this.minZ;
  }

  @Override
  public int getValue(final int gridX, final int gridY) {
    final int colour;
    final double elevation = this.elevationModel.getElevation(gridX, gridY);
    if (Double.isNaN(elevation)) {
      colour = NULL_COLOUR;
    } else {
      final double elevationMultiple = elevation * this.colourGreyMultiple;
      final double elevationPercent = elevationMultiple - this.minColourMultiple;
      final int grey = (int)Math.round(elevationPercent * 255);
      colour = WebColors.colorToRGB(255, grey, grey, grey);
    }
    return colour;
  }

  @Override
  public void setElevationModel(final GriddedElevationModel elevationModel) {
    super.setElevationModel(elevationModel);
    updateCachedFields();
  }

  public void setMaxZ(final double maxZ) {
    this.maxZ = maxZ;
  }

  public void setMinZ(final double minZ) {
    this.minZ = minZ;
  }

  public void updateCachedFields() {
    this.minZ = this.elevationModel.getMinZ();
    this.maxZ = this.elevationModel.getMaxZ();
    if (Double.isFinite(this.minZ)) {
      this.colourGreyMultiple = 1.0 / (this.maxZ - this.minZ);
      this.minColourMultiple = this.minZ * this.colourGreyMultiple;
    }
  }
}
