package com.revolsys.elevation.cloud.las.zip;

public enum LazItemType {
  BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, POINT10, GPSTIME11, RGB12, WAVEPACKET13, POINT14, RGBNIR14;

  private static final LazItemType[] TYPES = LazItemType.values();

  public static LazItemType fromOrdinal(final int i) {
    return TYPES[i];
  }
}
