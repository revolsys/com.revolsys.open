package com.revolsys.elevation.cloud.las;

public interface LasPointExtended extends LasPointGpsTime {
  boolean isOverlap();

  void setOverlap(boolean overlap);
}
