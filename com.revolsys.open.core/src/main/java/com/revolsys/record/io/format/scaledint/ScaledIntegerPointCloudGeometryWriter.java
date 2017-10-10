package com.revolsys.record.io.format.scaledint;

import java.io.IOException;

import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;

public class ScaledIntegerPointCloudGeometryWriter extends AbstractWriter<Geometry> implements GeometryWriter {
  private boolean initialized;

  private final Resource resource;

  private ChannelWriter writer;

  private double scaleXy;

  private double scaleZ;

  private GeometryFactory geometryFactory;

  public ScaledIntegerPointCloudGeometryWriter(final Resource resource) {
    this.resource = resource;
    setGeometryFactory(GeometryFactory.fixed3d(0, 1000.0, 1000.0, 1000.0));

  }

  @Override
  public void close() {
    final ChannelWriter writer = this.writer;
    this.writer = null;
    if (writer != null) {
      writer.close();
    }
  }

  private void initialize() throws IOException {
    if (!this.initialized) {
      this.initialized = true;
      this.writer = this.resource.newChannelWriter();

      final int coordinateSystemId = this.geometryFactory.getCoordinateSystemId();
      this.writer.putBytes(ScaledIntegerPointCloud.FILE_TYPE_HEADER_BYTES); // File type
      this.writer.putShort(ScaledIntegerPointCloud.VERSION); // version
      this.writer.putInt(coordinateSystemId);
      this.writer.putDouble(this.scaleXy);
      this.writer.putDouble(this.scaleZ);
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
    try {
      initialize();
      final ChannelWriter writer = this.writer;
      final double scaleXy = this.scaleXy;
      final double scaleZ = this.scaleZ;
      if (Double.isFinite(x)) {
        final int intValue = (int)Math.round(x * scaleXy);
        writer.putInt(intValue);
      } else {
        writer.putInt(Integer.MIN_VALUE);
      }
      if (Double.isFinite(y)) {
        final int intValue = (int)Math.round(y * scaleXy);
        writer.putInt(intValue);
      } else {
        writer.putInt(Integer.MIN_VALUE);
      }
      if (Double.isFinite(z)) {
        final int intValue = (int)Math.round(z * scaleZ);
        writer.putInt(intValue);
      } else {
        writer.putInt(Integer.MIN_VALUE);
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
