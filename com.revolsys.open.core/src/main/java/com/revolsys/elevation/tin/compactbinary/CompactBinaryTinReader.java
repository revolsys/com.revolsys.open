package com.revolsys.elevation.tin.compactbinary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.revolsys.elevation.tin.IntArrayScaleTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryTinReader implements BaseCloseable {

  public static TriangulatedIrregularNetwork read(final Resource resource) {
    try (
      final CompactBinaryTinReader compactBinaryTinReader = new CompactBinaryTinReader(resource)) {
      final TriangulatedIrregularNetwork tin = compactBinaryTinReader
        .newTriangulatedIrregularNetwork();
      return tin;
    }
  }

  private final Resource resource;

  private int triangleCount;

  private GeometryFactory geometryFactory;

  private byte[] bytes = new byte[36 * 1000];

  private ByteBuffer buffer = ByteBuffer.wrap(this.bytes);

  private BoundingBox boundingBox;

  private InputStream in;

  private double scaleFactorXY;

  private double scaleFactorZ;

  public CompactBinaryTinReader(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    try {
      this.in.close();
    } catch (final IOException e) {

    } finally {
      this.boundingBox = null;
      this.buffer = null;
      this.bytes = null;
      this.in = null;
      this.triangleCount = 0;
    }
  }

  public void forEachTriangle(final TriangleConsumer action) {
    open();
    try {
      int triangleToReadCount = this.triangleCount;
      while (triangleToReadCount > 0) {
        int readCount;
        if (triangleToReadCount < 1000) {
          readCount = triangleToReadCount;
        } else {
          readCount = 1000;
        }
        this.buffer.rewind();
        this.in.read(this.bytes, 0, 36 * readCount);
        for (int readIndex = 0; readIndex < readCount; readIndex++) {
          final double x1 = this.buffer.getInt() / this.scaleFactorXY;
          final double y1 = this.buffer.getInt() / this.scaleFactorXY;
          final double z1 = this.buffer.getInt() / this.scaleFactorZ;
          final double x2 = this.buffer.getInt() / this.scaleFactorXY;
          final double y2 = this.buffer.getInt() / this.scaleFactorXY;
          final double z2 = this.buffer.getInt() / this.scaleFactorZ;
          final double x3 = this.buffer.getInt() / this.scaleFactorXY;
          final double y3 = this.buffer.getInt() / this.scaleFactorXY;
          final double z3 = this.buffer.getInt() / this.scaleFactorZ;
          action.accept(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        }
        triangleToReadCount -= readCount;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.resource, e);
    }
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    open();
    try {
      final int[] triangleXCoordinates = new int[this.triangleCount * 3];
      final int[] triangleYCoordinates = new int[this.triangleCount * 3];
      final int[] triangleZCoordinates = new int[this.triangleCount * 3];
      int triangleToReadCount = this.triangleCount;
      int coordinateIndex = 0;
      while (triangleToReadCount > 0) {
        int readCount;
        if (triangleToReadCount < 1000) {
          readCount = triangleToReadCount;
        } else {
          readCount = 1000;
        }
        this.buffer.rewind();
        this.in.read(this.bytes, 0, 36 * readCount);
        for (int readIndex = 0; readIndex < readCount; readIndex++) {
          for (int i = 0; i < 3; i++) {
            triangleXCoordinates[coordinateIndex] = this.buffer.getInt();
            triangleYCoordinates[coordinateIndex] = this.buffer.getInt();
            triangleZCoordinates[coordinateIndex] = this.buffer.getInt();
            coordinateIndex++;
          }
        }
        triangleToReadCount -= readCount;
      }
      return new IntArrayScaleTriangulatedIrregularNetwork(this.geometryFactory, this.boundingBox,
        this.triangleCount, triangleXCoordinates, triangleYCoordinates, triangleZCoordinates);
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.resource, e);
    }
  }

  public void open() {
    if (this.in == null) {
      this.in = this.resource.newInputStream();
      readHeader();
    }
  }

  private void readHeader() {
    try {
      this.in.read(this.bytes, 0, CompactBinaryTin.HEADER_SIZE);

      final byte[] fileTypeBytes = new byte[6];
      this.buffer.get(fileTypeBytes);
      @SuppressWarnings("unused")
      final String fileType = new String(fileTypeBytes, StandardCharsets.UTF_8); // File
                                                                                 // type
      final short version = this.buffer.getShort();
      final int coordinateSystemId = this.buffer.getInt(); // Coordinate System
                                                           // ID
      this.scaleFactorXY = this.buffer.getDouble();
      this.scaleFactorZ = this.buffer.getDouble();
      this.geometryFactory = GeometryFactory.fixed(coordinateSystemId, 3, this.scaleFactorXY,
        this.scaleFactorZ);
      final double minX = this.buffer.getDouble();
      final double minY = this.buffer.getDouble();
      final double maxX = this.buffer.getDouble();
      final double maxY = this.buffer.getDouble();
      this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY, maxX, maxY);

      this.triangleCount = this.buffer.getInt();
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to read: " + this.resource, e);
    }
  }
}
