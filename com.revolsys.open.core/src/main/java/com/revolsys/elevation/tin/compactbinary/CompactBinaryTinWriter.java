package com.revolsys.elevation.tin.compactbinary;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

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

  private static final int BUFFER_SIZE = 1000;

  private Resource resource;

  private double scaleXY;

  private double scaleZ;

  private ByteBuffer buffer = ByteBuffer.allocateDirect(36 * 1000);

  private WritableByteChannel out;

  private int bufferTriangleCount;

  public CompactBinaryTinWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    this.resource = null;
    if (this.out != null) {
      if (this.out.isOpen()) {
        try {
          this.out.close();
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    }
    this.out = null;
    this.buffer = null;
  }

  @Override
  public void flush() {
  }

  @Override
  public void open() {
  }

  @Override
  public void write(final TriangulatedIrregularNetwork tin) {
    try {
      this.out = this.resource.newWritableByteChannel();
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

      this.buffer.put(CompactBinaryTin.FILE_TYPE_BYTES);
      this.buffer.putShort(CompactBinaryTin.VERSION);
      this.buffer.putInt(coordinateSystemId); // Coordinate System ID
      this.buffer.putDouble(this.scaleXY); // Scale XY
      this.buffer.putDouble(this.scaleZ); // Scale Z
      this.buffer.putDouble(tinBoundingBox.getMinX()); // minX
      this.buffer.putDouble(tinBoundingBox.getMinY()); // minY
      this.buffer.putDouble(tinBoundingBox.getMaxX()); // maxX
      this.buffer.putDouble(tinBoundingBox.getMaxY()); // maxY
      this.buffer.putInt(triangleCount);

      Buffers.writeAll(this.out, this.buffer);

      tin.forEachTriangle(this::writeTriangle);
      Buffers.writeAll(this.out, this.buffer);
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
      if (Double.isFinite(x)) {
        final int intX = (int)Math.round(x * scaleXy);
        buffer.putInt(intX);
      } else {
        buffer.putInt(Integer.MIN_VALUE);
      }
      if (Double.isFinite(y)) {
        final int intX = (int)Math.round(y * scaleXy);
        buffer.putInt(intX);
      } else {
        buffer.putInt(Integer.MIN_VALUE);
      }
      if (Double.isFinite(z)) {
        final int intX = (int)Math.round(z * scaleZ);
        buffer.putInt(intX);
      } else {
        buffer.putInt(Integer.MIN_VALUE);
      }
    }
    this.bufferTriangleCount++;
    if (this.bufferTriangleCount == BUFFER_SIZE) {
      try {
        Buffers.writeAll(this.out, buffer);
      } catch (final IOException e) {
        throw Exceptions.wrap("Unable to write: " + this.resource, e);
      }
      this.bufferTriangleCount = 0;
    }
  }
}
