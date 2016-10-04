package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.endian.EndianInput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint8GpsTimeRgbNir extends LasPoint7GpsTimeRgb implements LasPointNir {
  public static LasPoint8GpsTimeRgbNir newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint8GpsTimeRgbNir(pointCloud, recordDefinition, in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private int nir;

  private int green;

  private int blue;

  public LasPoint8GpsTimeRgbNir(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) throws IOException {
    super(pointCloud, recordDefinition, in);
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
  public int getNir() {
    return this.nir;
  }

  @Override
  protected void read(final LasPointCloud pointCloud, final EndianInput in) throws IOException {
    super.read(pointCloud, in);
    this.nir = in.readLEUnsignedShort();
    this.green = in.readLEUnsignedShort();
    this.blue = in.readLEUnsignedShort();
  }
}
