package com.revolsys.gis.postgresql.type;

public abstract class ByteGetter {
  public static class BinaryByteGetter extends ByteGetter {
    private final byte[] array;

    public BinaryByteGetter(final byte[] array) {
      this.array = array;
    }

    @Override
    public int get(final int index) {
      return this.array[index] & 0xFF;
    }
  }

  public static class StringByteGetter extends ByteGetter {
    public static byte unhex(final char c) {
      if (c >= '0' && c <= '9') {
        return (byte)(c - '0');
      }
      if (c >= 'A' && c <= 'F') {
        return (byte)(c - 'A' + 10);
      }
      if (c >= 'a' && c <= 'f') {
        return (byte)(c - 'a' + 10);
      }
      throw new IllegalArgumentException("No valid Hex char " + c);
    }

    private final String rep;

    public StringByteGetter(final String rep) {
      this.rep = rep;
    }

    @Override
    public int get(int index) {
      index *= 2;
      final int high = unhex(this.rep.charAt(index));
      final int low = unhex(this.rep.charAt(index + 1));
      return (high << 4) + low;
    }
  }

  public abstract int get(int paramInt);
}
