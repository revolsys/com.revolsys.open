package com.revolsys.io.page;

public interface Page extends Comparable<Page> {
  void clear();

  byte[] getContent();

  int getIndex();

  int getOffset();

  int getSize();

  byte readByte();

  byte[] readBytes(int size);

  double readDouble();

  float readFloat();

  int readInt();

  long readLong();

  short readShort();

  void setContent(Page page);

  void setOffset(int i);

  void writeByte(byte b);

  void writeBytes(byte[] bytes);

  void writeBytes(byte[] bytes, int offset, int count);

  void writeDouble(double d);

  void writeFloat(float f);

  void writeInt(int i);

  void writeLong(long l);

  void writeShort(short s);

  void clearBytes(int startIndex);
  
  void flush();
  
  PageManager getPageManager();
}
