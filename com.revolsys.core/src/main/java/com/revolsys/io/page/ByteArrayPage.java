package com.revolsys.io.page;

public class ByteArrayPage implements Page {
  private final int index;

  private final byte[] content;

  private int offset = 0;

  private PageManager pageManager;

  public ByteArrayPage(final PageManager pageManager, final int index,
    final int size) {
    this.index = index;
    content = new byte[size];
    this.pageManager = pageManager;
  }

  public PageManager getPageManager() {
    return pageManager;
  }

  public void clear() {
    clearBytes(0);
  }

  public void clearBytes(int startIndex) {
    setOffset(startIndex);
    for (int i = startIndex; i < getSize(); i++) {
      writeByte(0);
    }
    setOffset(startIndex);
  }

  public int compareTo(final Page page) {
    final int index = getIndex();
    final int index2 = page.getIndex();
    if (index == index2) {
      return 0;
    } else if (index < index2) {
      return -1;
    } else {
      return 1;
    }
  }

  public void flush() {
    pageManager.write(this);
  }

  public byte[] getContent() {
    return content;
  }

  public int getIndex() {
    return index;
  }

  public int getOffset() {
    return offset;
  }

  public int getSize() {
    return content.length;
  }

  @Override
  public int hashCode() {
    return getIndex();
  }

  public byte readByte() {
    if (getOffset() + 1 > getSize()) {
      throw new ArrayIndexOutOfBoundsException(
        "Unable to read past end of record");
    }
    final int b1 = readNextByte();
    return (byte)b1;
  }

  public byte[] readBytes(final int size) {
    final byte[] bytes = new byte[size];
    System.arraycopy(getContent(), getOffset(), bytes, 0, size);
    setOffset(getOffset() + size);
    return bytes;
  }

  public double readDouble() {
    final long l = readLong();
    return Double.longBitsToDouble(l);
  }

  public float readFloat() {
    final int i = readInt();
    return Float.intBitsToFloat(i);
  }

  public int readInt() {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException(
        "Unable to read past end of record");
    } else {
      final int b1 = readNextByte();
      final int b2 = readNextByte();
      final int b3 = readNextByte();
      final int b4 = readNextByte();
      final int i = (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
      return i;
    }
  }

  public long readLong() {
    if (getOffset() + 8 > getSize()) {
      throw new ArrayIndexOutOfBoundsException(
        "Unable to read past end of record");
    } else {
      final int b1 = readNextByte();
      final int b2 = readNextByte();
      final int b3 = readNextByte();
      final int b4 = readNextByte();
      final int b5 = readNextByte();
      final int b6 = readNextByte();
      final int b7 = readNextByte();
      final int b8 = readNextByte();
      return (((long)b1 << 56) + ((long)(b2 & 255) << 48)
        + ((long)(b3 & 255) << 40) + ((long)(b4 & 255) << 32)
        + ((long)(b5 & 255) << 24) + ((b6 & 255) << 16) + ((b7 & 255) << 8) + ((b8 & 255) << 0));

    }
  }

  private int readNextByte() {
    final byte b = content[offset];
    offset++;
    return b & 0xff;
  }

  public short readShort() {
    if (getOffset() + 2 > getSize()) {
      throw new ArrayIndexOutOfBoundsException(
        "Unable to read past end of record");
    } else {
      final int b1 = readNextByte();
      final int b2 = readNextByte();
      return (short)((b1 << 8) + (b2 << 0));
    }
  }

  public void setContent(final Page page) {
    final byte[] copyContent = page.getContent();
    System.arraycopy(copyContent, 0, content, 0, copyContent.length);
  }

  public void setOffset(final int offset) {
    if (offset > getSize()) {
      throw new IllegalArgumentException("Cannot set offset past end of file ");
    } else {
      this.offset = offset;
    }
  }

  public void writeByte(final byte b) {
    if (getOffset() > getSize()) {
      throw new ArrayIndexOutOfBoundsException(
        "Unable to write past end of record");
    } else {
      writeByte((int)b);
    }
  }

  private void writeByte(final int b) {
    content[offset++] = (byte)b;
  }

  public void writeBytes(final byte[] bytes) {
    for (int i = 0; i < bytes.length; i++) {
      final int b = bytes[i];
      writeByte(b);
    }
  }

  public final void writeDouble(final double d) {
    writeLong(Double.doubleToLongBits(d));
  }

  public final void writeFloat(final float f) {
    writeInt(Float.floatToIntBits(f));
  }

  public void writeInt(final int i) {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException(
        "Unable to write past end of record");
    } else {
      writeByte((i >>> 24) & 0xFF);
      writeByte((i >>> 16) & 0xFF);
      writeByte((i >>> 8) & 0xFF);
      writeByte((i >>> 0) & 0xFF);
    }
  }

  public final void writeLong(final long l) {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException(
        "Unable to write past end of record");
    } else {
      writeByte((byte)(l >>> 56));
      writeByte((byte)(l >>> 48));
      writeByte((byte)(l >>> 40));
      writeByte((byte)(l >>> 32));
      writeByte((byte)(l >>> 24));
      writeByte((byte)(l >>> 16));
      writeByte((byte)(l >>> 8));
      writeByte((byte)(l >>> 0));
    }
  }

  public final void writeShort(final short s) {
    if (getOffset() + 2 > getSize()) {
      throw new ArrayIndexOutOfBoundsException(
        "Unable to write past end of record");
    } else {
      writeByte((s >>> 8) & 0xFF);
      writeByte((s >>> 0) & 0xFF);
    }
  }
}
