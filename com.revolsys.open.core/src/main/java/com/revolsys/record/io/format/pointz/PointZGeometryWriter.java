package com.revolsys.record.io.format.pointz;

import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.spring.resource.Resource;

public class PointZGeometryWriter extends AbstractWriter<Geometry> implements GeometryWriter {
  private static final String VERSION = "   0.0.1";

  private boolean initialized;

  private final Resource resource;

  private EndianOutput out;

  private double scaleXy;

  private double scaleZ;

  private GeometryFactory geometryFactory = GeometryFactory.fixed(0, 1000.0, 1000.0);

  public PointZGeometryWriter(final Resource resource) {
    this.resource = resource;

  }

  @Override
  public void close() {
    if (this.out != null) {
      this.out.close();
    }
  }

  @Override
  public void flush() {
    if (this.out != null) {
      this.out.flush();
    }
  }

  private void initialize() {
    if (!this.initialized) {
      this.initialized = true;
      this.out = this.resource.newBufferedOutputStream(EndianOutputStream::new);

      this.out.writeChars("POINTZ"); // File type
      this.out.writeChars(VERSION); // version
      final int coordinateSystemId = this.geometryFactory.getCoordinateSystemId();
      this.out.writeInt(coordinateSystemId);
      this.out.writeDouble(this.scaleXy);
      this.out.writeDouble(this.scaleZ);
    }
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.scaleXy = geometryFactory.getScaleXy();
    this.scaleZ = geometryFactory.getScaleZ();
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
    initialize();
    this.out.writeInt((int)Math.round(x * this.scaleXy));
    this.out.writeInt((int)Math.round(y * this.scaleXy));
    this.out.writeInt((int)Math.round(z * this.scaleZ));
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
