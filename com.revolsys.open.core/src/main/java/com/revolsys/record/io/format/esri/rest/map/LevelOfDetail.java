package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.record.io.format.esri.rest.AbstractMapWrapper;

public class LevelOfDetail extends AbstractMapWrapper {
  private double resolution;

  private double scale;

  private int level;

  public LevelOfDetail() {
  }

  public int getLevel() {
    return this.level;
  }

  public double getResolution() {
    return this.resolution;
  }

  public double getScale() {
    return this.scale;
  }

  public void setLevel(final int level) {
    this.level = level;
  }

  public void setResolution(final double resolution) {
    this.resolution = resolution;
  }

  public void setScale(final double scale) {
    this.scale = scale;
  }

  @Override
  public String toString() {
    return getLevel() + ", " + getResolution() + ", " + getScale();
  }
}
