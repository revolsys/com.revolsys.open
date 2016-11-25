package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class LasPoint6GpsTime extends LasPoint1GpsTime implements LasPointGpsTime {
  private static final long serialVersionUID = 1L;

  public static LasPoint6GpsTime newLasPoint(final LasPointCloud pointCloud,
    final RecordDefinition recordDefinition, final ByteBuffer buffer) {
    try {
      return new LasPoint6GpsTime(pointCloud, recordDefinition, buffer);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public LasPoint6GpsTime(final LasPointCloud pointCloud, final double x, final double y,
    final double z) {
    super(pointCloud, x, y, z);
  }

  public LasPoint6GpsTime(final LasPointCloud pointCloud, final RecordDefinition recordDefinition,
    final ByteBuffer buffer) throws IOException {
    super(pointCloud, recordDefinition, buffer);
  }
}
