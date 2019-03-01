package com.revolsys.elevation.cloud.las.zip.compress;

public interface LasWriteItemCompressed {
  default boolean chunk_bytes() {
    return false;
  }

  default boolean chunk_sizes() {
    return false;
  };

  boolean init(byte[] item, int context);;

}
