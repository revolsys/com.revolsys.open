package com.revolsys.gis.io;

import java.io.IOException;

public interface EndianOutput {
  void close() throws IOException;

  void flush();

  long getFilePointer() throws IOException;

  long length() throws IOException;

  void write(byte[] bytes) throws IOException;

  void write(byte[] bytes, int offset, int length) throws IOException;

  /**
   * Write a big endian int.
   *
   * @param i The int.
   * @throws IOException If an I/O error occurs.
   */
  void write(int i) throws IOException;

  void writeBytes(String s) throws IOException;

  /**
   * Write a big endian double.
   *
   * @param d The double.
   * @throws IOException If an I/O error occurs.
   */
  void writeDouble(double d) throws IOException;

  /**
   * Write a big endian float.
   *
   * @param f The float.
   * @throws IOException If an I/O error occurs.
   */
  void writeFloat(float f) throws IOException;

  /**
   * Write a big endian int.
   *
   * @param i The int.
   * @throws IOException If an I/O error occurs.
   */
  void writeInt(int i) throws IOException;

  /**
   * Write a little endian double.
   *
   * @param d The double.
   * @throws IOException If an I/O error occurs.
   */
  void writeLEDouble(double d) throws IOException;

  /**
   * Write a little endian float.
   *
   * @param f The float.
   * @throws IOException If an I/O error occurs.
   */
  void writeLEFloat(float f) throws IOException;

  /**
   * Write a little endian int.
   *
   * @param i The int.
   * @throws IOException If an I/O error occurs.
   */
  void writeLEInt(int i) throws IOException;

  /**
   * Write a little endian int.
   *
   * @param l The long.
   * @throws IOException If an I/O error occurs.
   */
  void writeLELong(long l) throws IOException;

  /**
   * Write a little endian short.
   *
   * @param s The short.
   * @throws IOException If an I/O error occurs.
   */
  void writeLEShort(short s) throws IOException;

  /**
   * Write a big endian int.
   *
   * @param l The long.
   * @throws IOException If an I/O error occurs.
   */
  void writeLong(long l) throws IOException;

  /**
   * Write a big endian short.
   *
   * @param s The short.
   * @throws IOException If an I/O error occurs.
   */
  void writeShort(short s) throws IOException;
}
