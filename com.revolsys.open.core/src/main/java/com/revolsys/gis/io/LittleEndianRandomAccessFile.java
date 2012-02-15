/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LittleEndianRandomAccessFile extends RandomAccessFile implements
  EndianInputOutput {

  public LittleEndianRandomAccessFile(final File file, final String mode)
    throws FileNotFoundException {
    super(file, mode);
  }

  public LittleEndianRandomAccessFile(final String name, final String mode)
    throws FileNotFoundException {
    super(name, mode);
  }

  public void flush() {
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.format.core.io.EndianInputOutput#readLEDouble()
   */
  public double readLEDouble() throws IOException {
    final long value = readLELong();
    return Double.longBitsToDouble(value);
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.format.core.io.EndianInputOutput#readLEInt()
   */
  public int readLEInt() throws IOException {
    final int b1 = read();
    final int b2 = read();
    final int b3 = read();
    final int b4 = read();
    final int value = (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;

    return value;
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.format.core.io.EndianInputOutput#readLELong()
   */
  public long readLELong() throws IOException {
    long value = 0;
    for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
      value |= ((long)(read() & 0xff)) << shiftBy;
    }
    return value;
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.format.core.io.EndianInputOutput#readLEShort()
   */
  public short readLEShort() throws IOException {
    final int b1 = read();
    final int b2 = read();
    final int value = (b2 << 8) + b1;
    return (short)value;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.revolsys.gis.format.core.io.EndianInputOutput#writeLEDouble(double)
   */
  public void writeLEDouble(final double d) throws IOException {
    final long l = Double.doubleToLongBits(d);
    writeLELong(l);
  }

  public void writeLEFloat(final float f) throws IOException {
    final int i = Float.floatToIntBits(f);
    writeLEInt(i);
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.format.core.io.EndianInputOutput#writeLEInt(int)
   */
  public void writeLEInt(final int i) throws IOException {
    write(i & 0xFF);
    write((i >>> 8) & 0xFF);
    write((i >>> 16) & 0xFF);
    write((i >>> 24) & 0xFF);
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.format.core.io.EndianInputOutput#writeLELong(long)
   */
  public void writeLELong(final long l) throws IOException {
    write((int)l & 0xFF);
    write((int)(l >>> 8) & 0xFF);
    write((int)(l >>> 16) & 0xFF);
    write((int)(l >>> 24) & 0xFF);
    write((int)(l >>> 32) & 0xFF);
    write((int)(l >>> 40) & 0xFF);
    write((int)(l >>> 48) & 0xFF);
    write((int)(l >>> 56) & 0xFF);
  }

  /*
   * (non-Javadoc)
   * @see com.revolsys.gis.format.core.io.EndianInputOutput#writeLEShort(short)
   */
  public void writeLEShort(final short s) throws IOException {
    write(s & 0xFF);
    write((s >>> 8) & 0xFF);
  }

  public void writeShort(final short s) throws IOException {
    write((s >>> 8) & 0xFF);
    write(s & 0xFF);
  }
}
