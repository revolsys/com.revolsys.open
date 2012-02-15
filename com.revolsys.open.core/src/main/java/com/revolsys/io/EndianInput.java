package com.revolsys.io;

import java.io.IOException;

public interface EndianInput {

  void close() throws IOException;

  /**
   * Read a byte.
   * 
   * @return The long.
   * @throws IOException If an I/O error occurs.
   */
  int read() throws IOException;

  /**
   * Read a byte.
   * 
   * @return The long.
   * @throws IOException If an I/O error occurs.
   */
  int read(byte[] buf) throws IOException;

  /**
   * Read a big endian double.
   * 
   * @return The double.
   * @throws IOException If an I/O error occurs.
   */
  double readDouble() throws IOException;

  /**
   * Read a big endian int.
   * 
   * @return The int.
   * @throws IOException If an I/O error occurs.
   */
  int readInt() throws IOException;

  /**
   * Read a little endian double.
   * 
   * @return The double.
   * @throws IOException If an I/O error occurs.
   */
  double readLEDouble() throws IOException;

  /**
   * Read a little endian int.
   * 
   * @return The int.
   * @throws IOException If an I/O error occurs.
   */
  int readLEInt() throws IOException;

  /**
   * Read a little endian long.
   * 
   * @return The long.
   * @throws IOException If an I/O error occurs.
   */
  long readLELong() throws IOException;

  /**
   * Read a little endian short.
   * 
   * @return The short.
   * @throws IOException If an I/O error occurs.
   */
  short readLEShort() throws IOException;

  /**
   * Read a big endian long.
   * 
   * @return The long.
   * @throws IOException If an I/O error occurs.
   */
  long readLong() throws IOException;

  /**
   * Read a big endian short.
   * 
   * @return The short.
   * @throws IOException If an I/O error occurs.
   */
  short readShort() throws IOException;

  int skipBytes(int i) throws IOException;
}
