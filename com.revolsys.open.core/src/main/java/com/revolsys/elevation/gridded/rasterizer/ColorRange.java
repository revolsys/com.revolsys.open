package com.revolsys.elevation.gridded.rasterizer;

import java.awt.Color;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;

public class ColorRange extends BaseObjectWithProperties implements MapSerializer {

  private static final int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  private int minBlue;

  private Color minColour = WebColors.Black;

  private int minGreen;

  private int minRed;

  private int rangeBlue;

  private int rangeGreen;

  private int rangeRed;

  private double rangeZ;

  private double minZ;

  private double maxZ;

  private Color maxColour = WebColors.White;

  private int maxColourInt = this.maxColour.getRGB();

  private int minColourInt = this.minColour.getRGB();

  private double multipleZ;

  public ColorRange() {
    updateValues();
  }

  public ColorRange(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  public Color getMaxColour() {
    return this.maxColour;
  }

  public int getMaxColourInt() {
    return this.maxColourInt;
  }

  public Color getMinColour() {
    return this.minColour;
  }

  public int getMinColourInt() {
    return this.minColourInt;
  }

  public int getValue(final double elevation) {
    if (Double.isNaN(elevation)) {
      return NULL_COLOUR;
    } else if (elevation <= this.minZ) {
      return this.minColourInt;
    } else if (elevation > this.maxZ) {
      return this.maxColourInt;
    } else {
      return getValueFast(elevation);
    }
  }

  public int getValueFast(final double elevation) {
    final double elevationPercent = (elevation - this.minZ) * this.multipleZ;
    final int red = this.minRed + (int)Math.round(elevationPercent * this.rangeRed);
    final int green = this.minGreen + (int)Math.round(elevationPercent * this.rangeGreen);
    final int blue = this.minBlue + (int)Math.round(elevationPercent * this.rangeBlue);
    final int colour = WebColors.colorToRGB(255, red, green, blue);
    return colour;
  }

  public boolean inRange(final double elevation) {
    return this.minZ <= elevation && elevation <= this.maxZ;
  }

  public void setMaxColour(final Color maxColour) {
    this.maxColour = WebColors.newAlpha(maxColour, 255);
    this.maxColourInt = this.maxColour.getRGB();
    updateValues();
  }

  public void setMinColour(final Color minColour) {
    this.minColour = WebColors.newAlpha(minColour, 255);
    this.minColourInt = this.minColour.getRGB();
    updateValues();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addToMap(map, "minColour", this.minColour);
    addToMap(map, "maxColour", this.maxColour);
    if (Double.isFinite(this.minZ)) {
      map.add("minZ", this.minZ);
      map.add("maxZ", this.maxZ);
    }
    return map;
  }

  public void updateValues() {
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
