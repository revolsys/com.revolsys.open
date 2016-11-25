package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.revolsys.io.Buffers;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint7GpsTimeRgb extends LasPoint6GpsTime implements LasPointRgb {
  private static final long serialVersionUID = 1L;

  public static LasPoint7GpsTimeRgb newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final ByteBuffer buffer) {
    try {
      return new LasPoint7GpsTimeRgb(pointCloud, recordDefinition, buffer);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private int red;

  private int green;

  private int blue;

  public LasPoint7GpsTimeRgb(final LasPointCloud pointCloud, final double x, final double y,
    final double z) {
    super(pointCloud, x, y, z);
  }

  public LasPoint7GpsTimeRgb(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final ByteBuffer buffer) throws IOException {
    super(pointCloud, recordDefinition, buffer);
  }

  @Override
  public int getBlue() {
    return this.blue;
  }

  @Override
  public int getGreen() {
    return this.green;
  }

  @Override
  public int getRed() {
    return this.red;
  }

  @Override
  protected void read(final LasPointCloud pointCloud, final ByteBuffer buffer) throws IOException {
    super.read(pointCloud, buffer);
    this.red = Buffers.getLEUnsignedShort(buffer);
    this.green = Buffers.getLEUnsignedShort(buffer);
    this.blue = Buffers.getLEUnsignedShort(buffer);
  }

  @Override
  protected void write(final LasPointCloud pointCloud, final EndianOutput out) {
    super.write(pointCloud, out);
    out.writeLEUnsignedShort(this.red);
    out.writeLEUnsignedShort(this.green);
    out.writeLEUnsignedShort(this.blue);
  }
}
