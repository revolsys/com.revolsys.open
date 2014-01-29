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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class EndianMappedByteBuffer implements EndianInputOutput {
  private final MappedByteBuffer buffer;

  private final RandomAccessFile randomAccessFile;

  public EndianMappedByteBuffer(final File file, final MapMode mapMode)
    throws IOException {
    String mode = "r";
    if (mapMode.equals(MapMode.READ_WRITE)) {
      mode = "rw";
    }
    randomAccessFile = new RandomAccessFile(file, mode);
    final FileChannel channel = randomAccessFile.getChannel();
    buffer = channel.map(mapMode, 0, randomAccessFile.length());
    buffer.order(ByteOrder.BIG_ENDIAN);
  }

  public EndianMappedByteBuffer(final String name, final MapMode mapMode)
    throws IOException {
    this(new File(name), mapMode);
  }

  @Override
  public void close() throws IOException {
    randomAccessFile.close();
  }

  @Override
  public void flush() {
  }

  @Override
  public long getFilePointer() throws IOException {
    return buffer.position();
  }

  @Override
  public long length() throws IOException {
    return randomAccessFile.length();
  }

  @Override
  public int read() throws IOException {
    return buffer.get();
  }

  @Override
  public int read(final byte[] bytes) throws IOException {
    buffer.get(bytes);
    return bytes.length;
  }

  @Override
  public double readDouble() throws IOException {
    return buffer.getDouble();
  }

  @Override
  public int readInt() throws IOException {
    return buffer.getInt();
  }

  @Override
  public double readLEDouble() throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      return buffer.getDouble();
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public float readLEFloat() throws IOException {
    final int value = readLEInt();
    return Float.intBitsToFloat(value);
  }

  @Override
  public int readLEInt() throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      return buffer.getInt();
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public long readLELong() throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      return buffer.getLong();
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public short readLEShort() throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      return buffer.getShort();
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public long readLong() throws IOException {
    return buffer.getLong();
  }

  @Override
  public short readShort() throws IOException {
    return buffer.getShort();
  }

  @Override
  public void seek(final long index) throws IOException {
    buffer.position((int)index);
  }

  @Override
  public void setLength(final long length) throws IOException {
    randomAccessFile.setLength(length);
  }

  @Override
  public int skipBytes(final int i) throws IOException {
    buffer.position(buffer.position() + i);
    return buffer.position();
  }

  @Override
  public void write(final byte[] bytes) throws IOException {
    buffer.put(bytes);
  }

  @Override
  public void write(final byte[] bytes, final int offset, final int length)
    throws IOException {
    buffer.put(bytes, offset, length);
  }

  @Override
  public void write(final int i) throws IOException {
    buffer.put((byte)i);
  }

  @Override
  public void writeBytes(final String s) throws IOException {
    final int len = s.length();
    final byte[] bytes = new byte[len];
    s.getBytes(0, len, bytes, 0);
    write(bytes, 0, len);
  }

  @Override
  public void writeDouble(final double value) throws IOException {
    buffer.putDouble(value);
  }

  @Override
  public void writeFloat(final float value) throws IOException {
    buffer.putFloat(value);
  }

  @Override
  public void writeInt(final int value) throws IOException {
    buffer.putInt(value);
  }

  @Override
  public void writeLEDouble(final double value) throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      buffer.putDouble(value);
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLEFloat(final float value) throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      buffer.putFloat(value);
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLEInt(final int value) throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      buffer.putInt(value);
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLELong(final long value) throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      buffer.putLong(value);
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLEShort(final short value) throws IOException {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      buffer.putShort(value);
    } finally {
      buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLong(final long value) throws IOException {
    buffer.putLong(value);
  }

  @Override
  public void writeShort(final short value) throws IOException {
    buffer.putShort(value);
  }
}
