package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.revolsys.io.Buffers;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint8GpsTimeRgbNir extends LasPoint7GpsTimeRgb implements LasPointNir {
  private static final long serialVersionUID = 1L;

  public static LasPoint8GpsTimeRgbNir newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final ByteBuffer buffer) {
    try {
      return new LasPoint8GpsTimeRgbNir(pointCloud, recordDefinition, buffer);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private int nir;

  public LasPoint8GpsTimeRgbNir(final LasPointCloud pointCloud, final double x, final double y,
    final double z) {
    super(pointCloud, x, y, z);
  }

  public LasPoint8GpsTimeRgbNir(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final ByteBuffer buffer) throws IOException {
    super(pointCloud, recordDefinition, buffer);
  }

  @Override
  public int getNir() {
    return this.nir;
  }

  @Override
  protected void read(final LasPointCloud pointCloud, final ByteBuffer buffer) throws IOException {
    super.read(pointCloud, buffer);
    this.nir = Buffers.getLEUnsignedShort(buffer);
  }

  @Override
  protected void write(final LasPointCloud pointCloud, final EndianOutput out) {
    super.write(pointCloud, out);
    out.writeLEUnsignedShort(this.nir);
  }
}
