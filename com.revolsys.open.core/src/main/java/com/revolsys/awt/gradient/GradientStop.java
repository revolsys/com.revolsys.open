package com.revolsys.awt.gradient;

import java.awt.Color;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;

public class GradientStop extends BaseObjectWithProperties
  implements Cloneable, MapSerializer, Comparable<GradientStop> {

  private static final int NULL_COLOUR = WebColors.colorToRGB(0, 0, 0, 0);

  private int alpha;

  private int alphaRange;

  private int blue;

  private int blueRange;

  private Color color = WebColors.Black;

  private int colourInt = this.color.getRGB();

  private int green;

  private int greenRange;

  private Color maxColor = WebColors.White;

  private int maxColourInt = this.maxColor.getRGB();

  private double percent;

  private double percentMax;

  private int red;

  private int redRange;

  private double valueMax;

  private double valueMin;

  private double valueRange;

  private double valueRangeMultiple;

  public GradientStop() {
    updateValues();
  }

  public GradientStop(final double percent, final Color color) {
    this.percent = percent;
    setColor(color);
  }

  public GradientStop(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @Override
  public GradientStop clone() {
    return (GradientStop)super.clone();
  }

  @Override
  public int compareTo(final GradientStop range) {
    return Double.compare(this.percent, range.percent);
  }

  public Color getColor() {
    return this.color;
  }

  public int getMaxColourInt() {
    return this.maxColourInt;
  }

  public int getMinColourInt() {
    return this.colourInt;
  }

  public double getPercent() {
    return this.percent;
  }

  public double getPercentMax() {
    return this.percentMax;
  }

  public int getValue(final double elevation) {
    if (Double.isNaN(elevation)) {
      return NULL_COLOUR;
    } else if (elevation <= this.valueMin) {
      return this.colourInt;
    } else if (elevation > this.valueMax) {
      return this.maxColourInt;
    } else {
      return getValueFast(elevation);
    }
  }

  public int getValueFast(final double elevation) {
    if (elevation <= this.valueMin) {
      return this.colourInt;
    } else if (elevation > this.valueMax) {
      return NULL_COLOUR;
    } else {
      final double elevationPercent = (elevation - this.valueMin) * this.valueRangeMultiple;
      final int alpha = this.alpha + (int)Math.round(elevationPercent * this.alphaRange);
      final int red = this.red + (int)Math.round(elevationPercent * this.redRange);
      final int green = this.green + (int)Math.round(elevationPercent * this.greenRange);
      final int blue = this.blue + (int)Math.round(elevationPercent * this.blueRange);
      final int colour = WebColors.colorToRGB(alpha, red, green, blue);
      return colour;
    }
  }

  public boolean inRange(final double elevation) {
    return this.valueMin <= elevation && elevation <= this.valueMax;
  }

  public void setColor(final Color color) {
    this.alpha = color.getAlpha();
    this.red = color.getRed();
    this.green = color.getGreen();
    this.blue = color.getBlue();
    this.color = color;
    this.colourInt = color.getRGB();
    updateValues();
  }

  public void setMinMax(final double minZ, final double maxZ, final Color maxColor) {
    this.valueMin = minZ;
    this.valueMax = maxZ;
    if (Double.isFinite(this.valueMin)) {
      this.valueRange = maxZ - minZ;
    } else {
      this.valueRange = 0;
    }
    this.maxColor = maxColor;
    this.maxColourInt = this.maxColor.getRGB();
    updateValues();
  }

  public void setPercent(final double percent) {
    this.percent = percent;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap("gradientStop");
    addToMap(map, "color", this.color);
    if (Double.isFinite(this.percent)) {
      map.add("percent", this.percent);
    }
    return map;
  }

  public void updateValues() {
    if (Double.isFinite(this.valueMin) && Double.isFinite(this.valueMax)) {
      this.valueRange = 0;
      this.valueRangeMultiple = 0;
    } else {
      this.valueRange = this.valueMax - this.valueMin;
      if (this.valueRange == 0) {
        this.valueRangeMultiple = 0;
      } else {
        this.valueRangeMultiple = 1 / this.valueRange;
      }
    }
    this.alphaRange = this.maxColor.getAlpha() - this.alpha;
    this.redRange = this.maxColor.getRed() - this.red;
    this.greenRange = this.maxColor.getGreen() - this.green;
    this.blueRange = this.maxColor.getBlue() - this.blue;
  }
}
