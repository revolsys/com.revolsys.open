/*
 * Copyright 2005-2015, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.elevation.cloud.las.zip;

public interface MyDefs {

  byte U8_MIN = 0x0;

  byte U8_MAX = (byte)0xFF; // 255

  char U8_MAX_PLUS_ONE = 0x0100; // 256

  static int U32_ZERO_BIT_0(final int n) {
    return n & 0xFFFFFFFE;
  }

  static int U8_CLAMP(final int n) {
    return n <= U8_MIN ? U8_MIN : n >= Byte.toUnsignedInt(U8_MAX) ? U8_MAX : (byte)n;
  }

  static byte U8_FOLD(final int n) {
    return (byte)(n < U8_MIN ? n + U8_MAX_PLUS_ONE
      : n > Byte.toUnsignedInt(U8_MAX) ? n - U8_MAX_PLUS_ONE : n);
  }
}
