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

import java.io.UnsupportedEncodingException;

public interface MyDefs {

  char U16_MAX = Character.MAX_VALUE;

  int U32_MAX = 0xFFFFFFFF;

  int I32_MIN = Integer.MIN_VALUE;

  byte U8_MIN = 0x0;

  byte U8_MAX = (byte)0xFF; // 255

  byte U8_MAX_MINUS_ONE = (byte)0xFE; // 254

  char U8_MAX_PLUS_ONE = 0x0100; // 256

  static byte[] asByteArray(final char[] chars) {
    return asByteArray(new String(chars));
  }

  static byte[] asByteArray(final String s) {
    try {
      return s.getBytes("US-ASCII");
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  static short I16_QUANTIZE(final float n) {
    return n >= 0 ? (short)(n + 0.5f) : (short)(n - 0.5f);
  }

  static int I32_FLOOR(final double value) {
    return (int)Math.floor(value);
  }

  static int I32_QUANTIZE(final double n) {
    return n >= 0 ? (int)(n + 0.5f) : (int)(n - 0.5f);
  }

  static long I64_FLOOR(final double value) {
    return (long)Math.floor(value);
  }

  static boolean IS_LITTLE_ENDIAN() {
    return false;
  }

  static int[] realloc(final int[] data, final int size) {
    if (data.length >= size) {
      return data;
    }
    final int[] tmp = new int[size];
    System.arraycopy(data, 0, tmp, 0, data.length);
    return tmp;
  }

  static long[] realloc(final long[] data, final int size) {
    if (data == null) {
      return new long[size];
    } else if (data.length >= size) {
      return data;
    }
    final long[] tmp = new long[size];
    System.arraycopy(data, 0, tmp, 0, data.length);
    return tmp;
  }

  static String stringFromByteArray(final byte[] bytes) {
    int idx = -1;
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] == '\0') {
        idx = i;
        break;
      }
    }
    try {
      if (idx != -1) {
        return new String(bytes, 0, idx, "US-ASCII");
      } else {
        return new String(bytes, "US-ASCII");
      }
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  static int U32_QUANTIZE(final double n) {
    return n >= 0 ? (int)(n + 0.5f) : 0;
  }

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
