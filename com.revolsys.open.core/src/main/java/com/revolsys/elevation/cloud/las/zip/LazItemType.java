package com.revolsys.elevation.cloud.las.zip;

public enum LazItemType {
  BYTE, // Only used for non-standard record sizes. Not supported
  SHORT, // Not used
  INT, // Not used
  LONG, // Not used
  FLOAT, // Not used
  DOUBLE, // Not used
  POINT10, //
  GPSTIME11, //
  RGB12, //
  WAVEPACKET13, //
  POINT14, //
  RGBNIR14;

  private static final LazItemType[] TYPES = LazItemType.values();

  public static LazItemType fromOrdinal(final int i) {
    return TYPES[i];
  }
}
