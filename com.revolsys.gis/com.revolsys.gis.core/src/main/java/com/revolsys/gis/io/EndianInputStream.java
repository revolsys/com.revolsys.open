/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/rs-gis-core/trunk/src/main/java/com/revolsys/gis/format/core/io/LittleEndianRandomAccessFile.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2006-01-31 15:41:41 -0800 (Tue, 31 Jan 2006) $
 * $Revision: 76 $

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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EndianInputStream extends DataInputStream implements EndianInput {
  public EndianInputStream(
    final InputStream in) {
    super(in);
  }

  public double readLEDouble()
    throws IOException {
    final long value = readLELong();
    return Double.longBitsToDouble(value);
  }

  public int readLEInt()
    throws IOException {
    final int b1 = read();
    final int b2 = read();
    final int b3 = read();
    final int b4 = read();
    final int value = (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;

    return value;
  }

  public long readLELong()
    throws IOException {
    long value = 0;
    for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
      value |= ((long)(read() & 0xff)) << shiftBy;
    }
    return value;
  }

  public short readLEShort()
    throws IOException {
    final int b1 = read();
    final int b2 = read();
    final int value = (b2 << 8) + b1;
    return (short)value;
  }
}
