/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/com.revolsys.gis/trunk/com.revolsys.gis.core/src/main/java/com/revolsys/gis/format/core/io/LittleEndianRandomAccessFile.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2008-06-03 07:01:55 -0700 (Tue, 03 Jun 2008) $
 * $Revision: 1314 $

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

import java.io.IOException;
import java.io.OutputStream;

public class EndianOutputStream extends OutputStream implements EndianOutput {

  private final OutputStream out;

  private final byte writeBuffer[] = new byte[8];

  private long written = 0;

  public EndianOutputStream(final OutputStream out) {
    this.out = out;
  }

  @Override
  public void close() throws IOException {
    flush();
    this.out.close();
    super.close();
  }

  @Override
  public void flush() {
    try {
      this.out.flush();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long getFilePointer() throws IOException {
    return this.written;
  }

  @Override
  public long length() throws IOException {
    return this.written;
  }

  @Override
  public void write(final byte[] b) throws IOException {
    this.out.write(b);
    this.written += b.length;
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    this.out.write(b, off, len);
    this.written += len;
  }

  @Override
  public void write(final int b) throws IOException {
    this.out.write(b);
    this.written++;
  }

  @Override
  public final void writeBytes(final String s) throws IOException {
    final int len = s.length();
    final byte[] buffer = new byte[len];
    int count = 0;
    for (int i = 0; i < len; i++) {
      if (count == buffer.length) {
        write(buffer, 0, count);
        count = 0;
      }
      buffer[count] = (byte)s.charAt(i);
      count++;
    }
    if (count > 0) {
      write(buffer, 0, count);
    }
  }

  @Override
  public void writeDouble(final double d) throws IOException {
    final long l = Double.doubleToLongBits(d);
    writeLong(l);
  }

  @Override
  public void writeFloat(final float f) throws IOException {
    final int i = Float.floatToIntBits(f);
    writeInt(i);
  }

  @Override
  public void writeInt(final int i) throws IOException {
    this.writeBuffer[0] = (byte)(i >>> 24);
    this.writeBuffer[1] = (byte)(i >>> 16);
    this.writeBuffer[2] = (byte)(i >>> 8);
    this.writeBuffer[3] = (byte)(i >>> 0);
    write(this.writeBuffer, 0, 4);
  }

  @Override
  public void writeLEDouble(final double d) throws IOException {
    final long l = Double.doubleToLongBits(d);
    writeLELong(l);
  }

  @Override
  public void writeLEFloat(final float f) throws IOException {
    final int i = Float.floatToIntBits(f);
    writeLEInt(i);
  }

  @Override
  public void writeLEInt(final int i) throws IOException {
    this.writeBuffer[0] = (byte)(i >>> 0);
    this.writeBuffer[1] = (byte)(i >>> 8);
    this.writeBuffer[2] = (byte)(i >>> 16);
    this.writeBuffer[3] = (byte)(i >>> 24);
    write(this.writeBuffer, 0, 4);
  }

  @Override
  public void writeLELong(final long l) throws IOException {
    this.writeBuffer[0] = (byte)(l >>> 0);
    this.writeBuffer[1] = (byte)(l >>> 8);
    this.writeBuffer[2] = (byte)(l >>> 16);
    this.writeBuffer[3] = (byte)(l >>> 24);
    this.writeBuffer[4] = (byte)(l >>> 32);
    this.writeBuffer[5] = (byte)(l >>> 40);
    this.writeBuffer[6] = (byte)(l >>> 48);
    this.writeBuffer[7] = (byte)(l >>> 56);
    write(this.writeBuffer, 0, 8);
  }

  @Override
  public void writeLEShort(final short s) throws IOException {
    this.writeBuffer[0] = (byte)(s >>> 0);
    this.writeBuffer[1] = (byte)(s >>> 8);
    write(this.writeBuffer, 0, 2);
  }

  @Override
  public void writeLong(final long l) throws IOException {
    this.writeBuffer[0] = (byte)(l >>> 56);
    this.writeBuffer[1] = (byte)(l >>> 48);
    this.writeBuffer[2] = (byte)(l >>> 40);
    this.writeBuffer[3] = (byte)(l >>> 32);
    this.writeBuffer[4] = (byte)(l >>> 24);
    this.writeBuffer[5] = (byte)(l >>> 16);
    this.writeBuffer[6] = (byte)(l >>> 8);
    this.writeBuffer[7] = (byte)(l >>> 0);
    write(this.writeBuffer, 0, 8);
  }

  @Override
  public void writeShort(final short s) throws IOException {
    this.writeBuffer[0] = (byte)(s >>> 8);
    this.writeBuffer[1] = (byte)(s >>> 0);
    write(this.writeBuffer, 0, 2);
  }
}
