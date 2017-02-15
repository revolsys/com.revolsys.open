package com.revolsys.record.io.format.pointz;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.Buffers;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class PointZGeometryReader extends AbstractIterator<Geometry> implements GeometryReader {
  private final Resource resource;

  private ReadableByteChannel in;

  private double scaleXy;

  private double scaleZ;

  private GeometryFactory geometryFactory;

  private int bufferRecordReadCount = 0;

  private int bufferRecordCount = 0;

  // must be multiple of 12
  private ByteBuffer buffer = ByteBuffer.allocateDirect(8184);

  public PointZGeometryReader(final Resource resource) {
    this.resource = resource;
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    final ReadableByteChannel in = this.in;
    if (in != null) {
      try {
        in.close();
      } catch (final IOException e) {
      }
      this.in = null;
    }
    this.buffer = null;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  protected Geometry getNext() {
    if (this.bufferRecordReadCount >= this.bufferRecordCount) {
      if (!readBuffer()) {
        throw new NoSuchElementException();
      }
    }
    this.bufferRecordReadCount++;
    final int xInt = this.buffer.getInt();
    final int yInt = this.buffer.getInt();
    final int zInt = this.buffer.getInt();
    final double x = xInt / this.scaleXy;
    final double y = yInt / this.scaleXy;
    final double z = zInt / this.scaleZ;
    return this.geometryFactory.point(x, y, z);
  }

  @Override
  protected void initDo() {
    try {
      super.initDo();
      this.in = this.resource.newReadableByteChannel();
      this.buffer.limit(PointZIoFactory.HEADER_SIZE);
      Buffers.readAll(this.in, this.buffer);
      final byte[] fileTypeBytes = new byte[6];
      this.buffer.get(fileTypeBytes);
      final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8); // File
                                                                                 // type
      if (!PointZIoFactory.FILE_TYPE_POINTZ.equals(fileType)) {
        throw new IllegalArgumentException("File must start with the text: "
          + PointZIoFactory.FILE_TYPE_POINTZ + " not " + fileType);
      }
      @SuppressWarnings("unused")
      final short version = this.buffer.getShort();
      final int coordinateSystemId = this.buffer.getInt();
      this.scaleXy = this.buffer.getDouble();

      this.scaleZ = this.buffer.getDouble();
      this.geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, this.scaleXy,
        this.scaleXy, this.scaleZ);
      readBuffer();
    } catch (final IOException e) {
      try {
        throw Exceptions.wrap("Error opening: " + PointZGeometryReader.this.resource, e);
      } finally {
        close();
      }
    }
  }

  private boolean readBuffer() {
    try {
      final ReadableByteChannel in = this.in;
      final ByteBuffer buffer = this.buffer;
      buffer.clear();
      final int recordSize = PointZIoFactory.RECORD_SIZE;
      int bufferCount = 0;
      do {
        final int readCount = in.read(buffer);
        if (readCount == -1) {
          return false;
        }
        bufferCount += readCount;
      } while (bufferCount == 0 || bufferCount % recordSize != 0);
      this.bufferRecordReadCount = 0;
      this.bufferRecordCount = bufferCount / recordSize;
      buffer.flip();
      return true;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
