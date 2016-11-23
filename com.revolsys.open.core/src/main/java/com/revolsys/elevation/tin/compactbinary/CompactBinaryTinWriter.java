package com.revolsys.elevation.tin.compactbinary;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.io.Buffers;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class CompactBinaryTinWriter extends BaseObjectWithProperties
  implements TriangulatedIrregularNetworkWriter {

  private Resource resource;

  private double scaleXY;

  private double scaleZ;

  private ByteBuffer buffer;

  private OutputStream out;

  private byte[] bytes = new byte[36 * 1000];

  private int bufferTriangleCount;

  public CompactBinaryTinWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    this.resource = null;
    this.out = null;
    this.buffer = null;
    this.bytes = null;
  }

  @Override
  public void flush() {
  }

  @Override
  public void open() {
  }

  @Override
  public void write(final TriangulatedIrregularNetwork tin) {
    try (
      OutputStream out = this.resource.newOutputStream()) {
      this.out = out;
      final BoundingBox tinBoundingBox = tin.getBoundingBox();

      final GeometryFactory geometryFactory = tin.getGeometryFactory();

      final int coordinateSystemId = geometryFactory.getCoordinateSystemId();
      this.scaleXY = geometryFactory.getScaleXy();
      if (this.scaleXY <= 0) {
        this.scaleXY = 1000;
      }
      this.scaleZ = geometryFactory.getScaleZ();
      if (this.scaleZ <= 0) {
        this.scaleZ = 1000;
      }
      final int triangleCount = tin.getTriangleCount();

      final ByteBuffer buffer = ByteBuffer.wrap(this.bytes);
      this.buffer = buffer;
      buffer.put(CompactBinaryTin.FILE_TYPE_BYTES);
      buffer.putShort(CompactBinaryTin.VERSION);
      buffer.putInt(coordinateSystemId); // Coordinate System ID
      buffer.putDouble(this.scaleXY); // Scale XY
      buffer.putDouble(this.scaleZ); // Scale Z
      buffer.putDouble(tinBoundingBox.getMinX()); // minX
      buffer.putDouble(tinBoundingBox.getMinY()); // minY
      buffer.putDouble(tinBoundingBox.getMaxX()); // maxX
      buffer.putDouble(tinBoundingBox.getMaxY()); // maxY
      buffer.putInt(triangleCount);

      out.write(this.bytes, 0, CompactBinaryTin.HEADER_SIZE);
      buffer.rewind();

      tin.forEachTriangle(this::writeTriangle);
      if (this.bufferTriangleCount != 0) {
        try {
          final int recordSize = 36;
          out.write(this.bytes, 0, recordSize * this.bufferTriangleCount);
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
        this.bufferTriangleCount = 0;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to write: " + this.resource, e);
    }
  }

  private void writeTriangle(final Triangle triangle) {

    final ByteBuffer buffer = this.buffer;
    final double scaleXy = this.scaleXY;
    final double scaleZ = this.scaleZ;
    for (int i = 0; i < 3; i++) {
      final double x = triangle.getX(i);
      final double y = triangle.getY(i);
      final double z = triangle.getZ(i);
      Buffers.putDouble(buffer, x, scaleXy);
      Buffers.putDouble(buffer, y, scaleXy);
      Buffers.putDouble(buffer, z, scaleZ);
    }
    this.bufferTriangleCount++;
    if (this.bufferTriangleCount == 1000) {
      try {
        this.out.write(this.bytes);
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
      buffer.rewind();
      this.bufferTriangleCount = 0;
    }
  }
}
