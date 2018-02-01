package com.revolsys.geometry.cs.esri;

import java.util.Arrays;

public class ByteArray {
  private final byte[] data;

  public ByteArray(final byte[] data) {
    this.data = data;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof ByteArray) {
      final ByteArray array = (ByteArray)object;
      return Arrays.equals(this.data, array.data);
    }
    return false;
  }

  public byte[] getData() {
    return this.data.clone();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.data);
  }

  @Override
  public String toString() {
    return Arrays.toString(this.data);
  }
}
