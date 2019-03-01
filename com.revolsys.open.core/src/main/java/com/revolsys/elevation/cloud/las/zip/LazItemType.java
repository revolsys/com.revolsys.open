package com.revolsys.elevation.cloud.las.zip;

import java.util.HashMap;
import java.util.Map;

public enum LazItemType {
  BYTE(0), // Only used for non-standard record sizes. Not supported
  SHORT(1), // Not used
  INT(2), // Not used
  LONG(3), // Not used
  FLOAT(4), // Not used
  DOUBLE(5), // Not used
  POINT10(6), //
  GPSTIME11(7), //
  RGB12(8), //
  WAVEPACKET13(9), //
  POINT14(10), //
  RGBNIR14(11);

  private static final Map<Integer, LazItemType> TYPES = new HashMap<>();

  static {
    for (final LazItemType type : values()) {
      TYPES.put(type.id, type);
    }
  }

  public static LazItemType fromId(final int i) {
    return TYPES.get(i);
  }

  private int id;

  private LazItemType(final int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }
}
