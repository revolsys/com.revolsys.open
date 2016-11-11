package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.endian.EndianInput;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint6GpsTime extends LasPoint1GpsTime implements LasPointGpsTime {
  private static final long serialVersionUID = 1L;

  public static LasPoint6GpsTime newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final EndianInput in) {
    try {
      return new LasPoint6GpsTime(pointCloud, recordDefinition, in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public LasPoint6GpsTime(final LasPointCloud pointCloud, final double x, final double y,
    final double z) {
    super(pointCloud, x, y, z);
  }

  public LasPoint6GpsTime(final LasPointCloud pointCloud, final RecordDefinition recordDefinition,
    final EndianInput in) throws IOException {
    super(pointCloud, recordDefinition, in);
  }
}
