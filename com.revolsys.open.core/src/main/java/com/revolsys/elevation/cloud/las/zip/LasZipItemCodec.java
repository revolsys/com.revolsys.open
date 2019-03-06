/*
 * Copyright 2007-2012, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public interface LasZipItemCodec {
  default int getVersion() {
    return 1;
  }

  void init(LasPoint firstPoint);

  void read(LasPoint point);

  default void write(final LasPoint point) {
    throw new UnsupportedOperationException("Not yet implemented: " + getClass());
  }

  static int I32_QUANTIZE(final float n) {
    return n >= 0 ? (int)(n + 0.5f) : (int)(n - 0.5f);
  }

  static int U32_ZERO_BIT_0(final int n) {
    return n & 0xFFFFFFFE;
  }

  static int U8_CLAMP(final int n) {
    if (n < 0) {
      return 0;
    } else if (n > 255) {
      return 255;
    } else {
      return n;
    }
  }

  static int U8_FOLD(final int n) {
    if (n < 0) {
      return n + 256;
    } else if (n > 255) {
      return n - 256;
    } else {
      return n;
    }
  }
}
