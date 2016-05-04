package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.record.io.format.esri.rest.AbstractMapWrapper;

public class LevelOfDetail extends AbstractMapWrapper {
  public LevelOfDetail() {
  }

  public Integer getLevel() {
    return getIntValue("level");
  }

  public Double getResolution() {
    return getDoubleValue("resolution");
  }

  public Double getScale() {
    return getDoubleValue("scale");
  }

  @Override
  public String toString() {
    return getLevel() + ", " + getResolution() + ", " + getScale();
  }
}
