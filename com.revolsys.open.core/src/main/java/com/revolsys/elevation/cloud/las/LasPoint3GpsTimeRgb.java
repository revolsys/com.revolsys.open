package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.endian.EndianInput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint3GpsTimeRgb extends LasPoint2Rgb implements LastPointGpsTime {
  public static LasPoint3GpsTimeRgb newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint3GpsTimeRgb(pointCloud, recordDefinition, in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private double gpsTime;

  public LasPoint3GpsTimeRgb(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) throws IOException {
    super(pointCloud, recordDefinition, in);
  }

  @Override
  public double getGpsTime() {
    return this.gpsTime;
  }

  @Override
  protected void read(final LasPointCloud pointCloud, final EndianInput in) throws IOException {
    super.read(pointCloud, in);
    this.gpsTime = in.readLEDouble();
  }
}
