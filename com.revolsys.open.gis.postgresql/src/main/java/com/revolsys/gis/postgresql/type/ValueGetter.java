package com.revolsys.gis.postgresql.type;

public abstract class ValueGetter {
  public static class NDR extends ValueGetter {
    public static final byte NUMBER = 1;

    public NDR(final ByteGetter data) {
      super(data, NUMBER);
    }

    @Override
    protected int getInt(final int index) {
      return (this.data.get(index + 3) << 24) + (this.data.get(index + 2) << 16)
        + (this.data.get(index + 1) << 8) + this.data.get(index);
    }

    @Override
    protected long getLong(final int index) {
      return ((long)this.data.get(index + 7) << 56) + ((long)this.data.get(index + 6) << 48)
        + ((long)this.data.get(index + 5) << 40) + ((long)this.data.get(index + 4) << 32)
        + ((long)this.data.get(index + 3) << 24) + ((long)this.data.get(index + 2) << 16)
        + ((long)this.data.get(index + 1) << 8) + ((long)this.data.get(index) << 0);
    }
  }

  public static class XDR extends ValueGetter {
    public static final byte NUMBER = 0;

    public XDR(final ByteGetter data) {
      super(data, NUMBER);
    }

    @Override
    protected int getInt(final int index) {
      return (this.data.get(index) << 24) + (this.data.get(index + 1) << 16)
        + (this.data.get(index + 2) << 8) + this.data.get(index + 3);
    }

    @Override
    protected long getLong(final int index) {
      return ((long)this.data.get(index) << 56) + ((long)this.data.get(index + 1) << 48)
        + ((long)this.data.get(index + 2) << 40) + ((long)this.data.get(index + 3) << 32)
        + ((long)this.data.get(index + 4) << 24) + ((long)this.data.get(index + 5) << 16)
        + ((long)this.data.get(index + 6) << 8) + ((long)this.data.get(index + 7) << 0);
    }
  }

  public static ValueGetter valueGetterForEndian(final ByteGetter bytes) {
    if (bytes.get(0) == ValueGetter.XDR.NUMBER) { // XDR
      return new ValueGetter.XDR(bytes);
    } else if (bytes.get(0) == ValueGetter.NDR.NUMBER) {
      return new ValueGetter.NDR(bytes);
    } else {
      throw new IllegalArgumentException("Unknown Endian type:" + bytes.get(0));
    }
  }

  ByteGetter data;

  int position;

  public final byte endian;

  public ValueGetter(final ByteGetter data, final byte endian) {
    this.data = data;
    this.endian = endian;
  }

  /**
   * Get a byte, should be equal for all endians
   */
  public byte getByte() {
    return (byte)this.data.get(this.position++);
  }

  /**
   * Get a double.
   */
  public double getDouble() {
    final long bitrep = getLong();
    return Double.longBitsToDouble(bitrep);
  }

  public int getInt() {
    final int res = getInt(this.position);
    this.position += 4;
    return res;
  }

  /** Get a 32-Bit integer */
  protected abstract int getInt(int index);

  public long getLong() {
    final long res = getLong(this.position);
    this.position += 8;
    return res;
  }

  /**
   * Get a long value. This is not needed directly, but as a nice side-effect
   * from GetDouble.
   */
  protected abstract long getLong(int index);
}
