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
package com.revolsys.elevation.cloud.las.decoder;

import java.nio.ByteBuffer;

public class U64I64F64 {

  public static U64I64F64[] newU64I64F64Array(final int size) {
    final U64I64F64[] array = new U64I64F64[size];
    for (int i = 0; i < size; i++) {
      array[i] = new U64I64F64();
    }
    return array;
  }

  private final ByteBuffer data = ByteBuffer.allocate(8);

  public double getF64() {
    return this.data.getDouble(0);
  }

  public long getI64() {
    return this.data.getLong(0);
  }

  public long getU64() {
    return this.data.getLong(0);
  }

  public void setF64(final double value) {
    this.data.putDouble(0, value);
  }

  public void setI64(final long value) {
    this.data.putLong(0, value);
  }

  public void setU64(final long value) {
    this.data.putLong(0, value);
  }
}
