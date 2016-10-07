package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.endian.EndianInput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint1GpsTime extends LasPoint0Core implements LastPointGpsTime {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static LasPoint1GpsTime newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint1GpsTime(pointCloud, recordDefinition, in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private double gpsTime;

  public LasPoint1GpsTime(final LasPointCloud pointCloud, final RecordDefinition recordDefinition,
    final EndianInput in) throws IOException {
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
