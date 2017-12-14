package com.revolsys.elevation.gridded.rasterizer;

import java.awt.Color;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;

public class ColorGriddedElevationModelRasterizer extends AbstractGriddedElevationModelRasterizer {

  private static final int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  private int minBlue;

  private Color minColour = WebColors.Black;

  private int minGreen;

  private int minRed;

  private int rangeBlue;

  private int rangeGreen;

  private int rangeRed;

  private double rangeZ;

  private Color maxColour = WebColors.White;

  private double multipleZ;

  public ColorGriddedElevationModelRasterizer() {
    super("colorGriddedElevationModelRasterizer", "style_color");
    updateValues();
  }

  public ColorGriddedElevationModelRasterizer(final GriddedElevationModel elevationModel) {
    this();
    setElevationModel(elevationModel);
  }

  public ColorGriddedElevationModelRasterizer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  public Color getMaxColour() {
    return this.maxColour;
  }

  public Color getMinColour() {
    return this.minColour;
  }

  @Override
  public String getName() {
    return "Colour";
  }

  @Override
  public int getValue(final int gridX, final int gridY) {
    final int colour;
    final double elevation = this.elevationModel.getElevation(gridX, gridY);
    if (Double.isNaN(elevation)) {
      colour = NULL_COLOUR;
    } else {
      final double elevationPercent = (elevation - this.minZ) * this.multipleZ;
      final int red = this.minRed + (int)Math.round(elevationPercent * this.rangeRed);
      final int green = this.minGreen + (int)Math.round(elevationPercent * this.rangeGreen);
      final int blue = this.minBlue + (int)Math.round(elevationPercent * this.rangeBlue);
      colour = WebColors.colorToRGB(255, red, green, blue);
    }
    return colour;
  }

  public void setMaxColour(final Color maxColour) {
    this.maxColour = maxColour;
    updateValues();
  }

  public void setMinColour(final Color minColour) {
    this.minColour = minColour;
    updateValues();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "minColour", this.minColour);
    addToMap(map, "maxColour", this.maxColour);
    if (Double.isFinite(this.minZ)) {
      double modelMinZ = Double.NaN;
      double modelMaxZ = Double.NaN;
      final GriddedElevationModel elevationModel = getElevationModel();
      if (elevationModel != null) {
        modelMinZ = elevationModel.getMinZ();
        modelMaxZ = elevationModel.getMaxZ();
      }
      addToMap(map, "minZ", this.minZ, modelMinZ);
      addToMap(map, "maxZ", this.maxZ, modelMaxZ);
    }
    return map;
  }

  @Override
  protected void updateValues() {
    this.minRed = this.minColour.getRed();
    this.rangeRed = this.maxColour.getRed() - this.minRed;
    this.minGreen = this.minColour.getGreen();
    this.rangeGreen = this.maxColour.getGreen() - this.minGreen;
    this.minBlue = this.minColour.getBlue();
    this.rangeBlue = this.maxColour.getBlue() - this.minBlue;
    if (Double.isFinite(this.minZ)) {
      this.rangeZ = this.maxZ - this.minZ;
    } else {
      this.rangeZ = 0;
    }
    if (this.rangeZ == 0) {
      this.multipleZ = 0;
    } else {
      this.multipleZ = 1 / this.rangeZ;
    }
  }
}
