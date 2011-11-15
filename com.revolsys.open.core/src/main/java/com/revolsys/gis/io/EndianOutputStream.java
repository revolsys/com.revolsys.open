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

  private final byte writeBuffer[] = new byte[1024];

  private long written = 0;

  public EndianOutputStream(final OutputStream out) {
    this.out = out;
  }

  @Override
  public void flush() {
    try {
      out.flush();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() throws IOException {
    flush();
    out.close();
    super.close();
  }

  public long getFilePointer() throws IOException {
    return written;
  }

  public long length() throws IOException {
    return written;
  }

  @Override
  public void write(final byte[] b) throws IOException {
    out.write(b);
    written += b.length;
  }

  @Override
  public void write(final byte[] b, final int off, final int len)
    throws IOException {
    out.write(b, off, len);
    written += len;
  }

  @Override
  public void write(final int b) throws IOException {
    out.write(b);
    written++;
  }

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

  public void writeDouble(final double d) throws IOException {
    final long l = Double.doubleToLongBits(d);
    writeLong(l);
  }

  public void writeFloat(final float f) throws IOException {
    final int i = Float.floatToIntBits(f);
    writeInt(i);
  }

  public void writeInt(final int i) throws IOException {
    writeBuffer[0] = (byte)(i >>> 24);
    writeBuffer[1] = (byte)(i >>> 16);
    writeBuffer[2] = (byte)(i >>> 8);
    writeBuffer[3] = (byte)(i >>> 0);
    write(writeBuffer, 0, 4);
  }

  public void writeLEDouble(final double d) throws IOException {
    final long l = Double.doubleToLongBits(d);
    writeLELong(l);
  }

  public void writeLEFloat(final float f) throws IOException {
    final int i = Float.floatToIntBits(f);
    writeLEInt(i);
  }

  public void writeLEInt(final int i) throws IOException {
    writeBuffer[0] = (byte)(i >>> 0);
    writeBuffer[1] = (byte)(i >>> 8);
    writeBuffer[2] = (byte)(i >>> 16);
    writeBuffer[3] = (byte)(i >>> 24);
    write(writeBuffer, 0, 4);
  }

  public void writeLELong(final long l) throws IOException {
    writeBuffer[0] = (byte)(l >>> 0);
    writeBuffer[1] = (byte)(l >>> 8);
    writeBuffer[2] = (byte)(l >>> 16);
    writeBuffer[3] = (byte)(l >>> 24);
    writeBuffer[4] = (byte)(l >>> 32);
    writeBuffer[5] = (byte)(l >>> 40);
    writeBuffer[6] = (byte)(l >>> 48);
    writeBuffer[7] = (byte)(l >>> 56);
    write(writeBuffer, 0, 8);
  }

  public void writeLEShort(final short s) throws IOException {
    writeBuffer[0] = (byte)(s >>> 0);
    writeBuffer[1] = (byte)(s >>> 8);
    write(writeBuffer, 0, 2);
  }

  public void writeLong(final long l) throws IOException {
    writeBuffer[0] = (byte)(l >>> 56);
    writeBuffer[1] = (byte)(l >>> 48);
    writeBuffer[2] = (byte)(l >>> 40);
    writeBuffer[3] = (byte)(l >>> 32);
    writeBuffer[4] = (byte)(l >>> 24);
    writeBuffer[5] = (byte)(l >>> 16);
    writeBuffer[6] = (byte)(l >>> 8);
    writeBuffer[7] = (byte)(l >>> 0);
    write(writeBuffer, 0, 8);
  }

  public void writeShort(final short s) throws IOException {
    writeBuffer[0] = (byte)(s >>> 8);
    writeBuffer[1] = (byte)(s >>> 0);
    write(writeBuffer, 0, 2);
  }
}
