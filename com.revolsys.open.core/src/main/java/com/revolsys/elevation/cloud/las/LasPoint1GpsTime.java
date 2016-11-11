package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.endian.EndianInput;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint1GpsTime extends LasPoint0Core implements LasPointGpsTime {
  private static final long serialVersionUID = 1L;

  public static double getCurrentGpsTime() {
    return System.currentTimeMillis() / 1000.0 - 315964800;
  }

  public static LasPoint1GpsTime newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint1GpsTime(pointCloud, recordDefinition, in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private double gpsTime;

  public LasPoint1GpsTime(final LasPointCloud pointCloud, final double x, final double y,
    final double z) {
    super(pointCloud, x, y, z);
    this.gpsTime = getCurrentGpsTime();
  }

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

  @Override
  protected void write(final LasPointCloud pointCloud, final EndianOutput out) {
    super.write(pointCloud, out);
    out.writeLEDouble(this.gpsTime);
  }
}
