package com.revolsys.record.io.format.pointz;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Buffers;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class PointZGeometryWriter extends AbstractWriter<Geometry> implements GeometryWriter {
  private boolean initialized;

  private final Resource resource;

  private WritableByteChannel out;

  private double scaleXy;

  private double scaleZ;

  private GeometryFactory geometryFactory;

  private final ByteBuffer buffer = ByteBuffer.allocateDirect(PointZIoFactory.RECORD_SIZE * 1000);

  private int writtenCount = 0;

  public PointZGeometryWriter(final Resource resource) {
    this.resource = resource;
    setGeometryFactory(GeometryFactory.fixed(0, 1000.0, 1000.0));

  }

  @Override
  public void close() {
    if (this.out != null) {
      try {
        if (this.out.isOpen()) {
          Buffers.writeAll(this.out, this.buffer);
          this.out.close();
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  private void initialize() throws IOException {
    if (!this.initialized) {
      this.initialized = true;
      this.out = this.resource.newWritableByteChannel();

      final int coordinateSystemId = this.geometryFactory.getCoordinateSystemId();
      this.buffer.put(PointZIoFactory.FILE_TYPE_POINTZ_BYTES); // File type
      this.buffer.putShort(PointZIoFactory.VERSION); // version
      this.buffer.putInt(coordinateSystemId);
      this.buffer.putDouble(this.scaleXy);
      this.buffer.putDouble(this.scaleZ);
      Buffers.writeAll(this.out, this.buffer);
    }
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.scaleXy = geometryFactory.getScaleXY();
    if (this.scaleXy <= 0) {
      this.scaleXy = 1000;
    }
    this.scaleZ = geometryFactory.getScaleZ();
    if (this.scaleZ <= 0) {
      this.scaleZ = 1000;
    }
  }

  @Override
  public String toString() {
    if (this.resource == null) {
      return super.toString();
    } else {
      return this.resource.toString();
    }
  }

  public void write(final double x, final double y, final double z) {
    final ByteBuffer buffer = this.buffer;
    final double scaleXy = this.scaleXy;
    final double scaleZ = this.scaleZ;
    try {
      initialize();
      if (this.writtenCount == 1000) {
        Buffers.writeAll(this.out, buffer);
        this.writtenCount = 0;
      }
      this.writtenCount++;
      if (Double.isFinite(x)) {
        final int intValue = (int)Math.round(x * scaleXy);
        buffer.putInt(intValue);
      } else {
        buffer.putInt(Integer.MIN_VALUE);
      }
      if (Double.isFinite(y)) {
        final int intValue = (int)Math.round(y * scaleXy);
        buffer.putInt(intValue);
      } else {
        buffer.putInt(Integer.MIN_VALUE);
      }
      if (Double.isFinite(z)) {
        final int intValue = (int)Math.round(z * scaleZ);
        buffer.putInt(intValue);
      } else {
        buffer.putInt(Integer.MIN_VALUE);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void write(final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final double x = point.getX();
      final double y = point.getY();
      final double z = point.getZ();
      write(x, y, z);
    } else {
      throw new IllegalArgumentException("Only points supported");
    }
  }
}
