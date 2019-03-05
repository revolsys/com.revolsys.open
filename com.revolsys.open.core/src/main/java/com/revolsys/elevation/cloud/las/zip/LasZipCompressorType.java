package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.collection.map.IntHashMap;

public enum LasZipCompressorType {
  POINTWISE(1), //
  POINTWISE_CHUNKED(2), //
  LAYERED_CHUNKED(3) //
  ;

  private static final IntHashMap<LasZipCompressorType> FORMAT_BY_ID = new IntHashMap<>();
  static {
    for (final LasZipCompressorType code : values()) {
      FORMAT_BY_ID.put(code.id, code);
    }
  }

  public static LasZipCompressorType getById(final int id) {
    final LasZipCompressorType code = FORMAT_BY_ID.get(id);
    if (code == null) {
      throw new IllegalArgumentException("Unsupported Las Point compressor=" + id);
    } else {
      return code;
    }
  }

  private int id;

  private LasZipCompressorType(final int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }
}
