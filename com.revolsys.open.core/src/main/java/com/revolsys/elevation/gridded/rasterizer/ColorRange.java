package com.revolsys.elevation.gridded.rasterizer;

import java.awt.Color;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;

public class ColorRange extends BaseObjectWithProperties
  implements Cloneable, MapSerializer, Comparable<ColorRange> {

  private static final int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  private int blue;

  private Color color = WebColors.Black;

  private int green;

  private int red;

  private int rangeBlue;

  private int rangeGreen;

  private int rangeRed;

  private double rangeZ;

  private double percent;

  private double maxPercent;

  private Color maxColor = WebColors.White;

  private int maxColourInt = this.maxColor.getRGB();

  private int colourInt = this.color.getRGB();

  private double multipleZ;

  private double minZ;

  private double maxZ;

  public ColorRange() {
    updateValues();
  }

  public ColorRange(final double percent, final Color color) {
    this.percent = percent;
    setColor(color);
  }

  public ColorRange(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @Override
  public ColorRange clone() {
    return (ColorRange)super.clone();
  }

  @Override
  public int compareTo(final ColorRange range) {
    return Double.compare(this.percent, range.percent);
  }

  public Color getColor() {
    return this.color;
  }

  public Color getMaxColor() {
    return this.maxColor;
  }

  public int getMaxColourInt() {
    return this.maxColourInt;
  }

  public double getMaxPercent() {
    return this.maxPercent;
  }

  public int getMinColourInt() {
    return this.colourInt;
  }

  public double getPercent() {
    return this.percent;
  }

  public int getValue(final double elevation) {
    if (Double.isNaN(elevation)) {
      return NULL_COLOUR;
    } else if (elevation <= this.minZ) {
      return this.colourInt;
    } else if (elevation > this.maxZ) {
      return this.maxColourInt;
    } else {
      return getValueFast(elevation);
    }
  }

  public int getValueFast(final double elevation) {
    if (elevation <= this.minZ) {
      return this.colourInt;
    } else if (elevation > this.maxZ) {
      return NULL_COLOUR;
    } else {
      final double elevationPercent = (elevation - this.minZ) * this.multipleZ;
      final int red = this.red + (int)Math.round(elevationPercent * this.rangeRed);
      final int green = this.green + (int)Math.round(elevationPercent * this.rangeGreen);
      final int blue = this.blue + (int)Math.round(elevationPercent * this.rangeBlue);
      final int colour = WebColors.colorToRGB(255, red, green, blue);
      return colour;
    }
  }

  public boolean inRange(final double elevation) {
    return this.percent <= elevation && elevation <= this.maxPercent;
  }

  public void setColor(Color color) {
    this.red = color.getRed();
    this.green = color.getGreen();
    this.blue = color.getBlue();
    if (color.getAlpha() != 255) {
      color = WebColors.newAlpha(color, 255);
    }
    this.color = WebColors.newAlpha(color, 255);
    this.colourInt = color.getRGB();
    updateValues();
  }

  public void setMaxColor(final Color maxColour) {
    this.maxColor = WebColors.newAlpha(maxColour, 255);
    this.maxColourInt = this.maxColor.getRGB();
    updateValues();
  }

  public void setMaxPercent(final double maxPercent) {
    this.maxPercent = maxPercent;
    updateValues();
  }

  public void setMinMaxZ(final double minZ, final double maxZ) {
    this.minZ = minZ;
    this.maxZ = maxZ;
    if (Double.isFinite(this.minZ)) {
      this.rangeZ = maxZ - minZ;
    } else {
      this.rangeZ = 0;
    }
  }

  public void setPercent(final double percent) {
    this.percent = percent;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap("griddedElevationModelColorRamp");
    addToMap(map, "color", this.color);
    if (Double.isFinite(this.percent)) {
      map.add("percent", this.percent);
    }
    return map;
  }

  public void updateValues() {
    this.rangeRed = this.maxColor.getRed() - this.red;
    this.rangeGreen = this.maxColor.getGreen() - this.green;
    this.rangeBlue = this.maxColor.getBlue() - this.blue;
    if (this.rangeZ == 0) {
      this.multipleZ = 0;
    } else {
      this.multipleZ = 1 / this.rangeZ;
    }
  }
}
