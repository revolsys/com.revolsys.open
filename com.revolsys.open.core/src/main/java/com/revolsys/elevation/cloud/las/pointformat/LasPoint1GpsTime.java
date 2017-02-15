package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutput;

public class LasPoint1GpsTime extends LasPoint0Core implements LasPointGpsTime {
  private static final long serialVersionUID = 1L;

  public static double getCurrentGpsTime() {
    return System.currentTimeMillis() / 1000.0 - 315964800;
  }

  private double gpsTime;

  public LasPoint1GpsTime(final LasPointCloud pointCloud) {
    super(pointCloud);
    this.gpsTime = getCurrentGpsTime();
  }

  @Override
  public double getGpsTime() {
    return this.gpsTime;
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.GpsTime;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final ChannelReader reader) {
    super.read(pointCloud, reader);
    this.gpsTime = reader.getDouble();
  }

  @Override
  public void setGpsTime(final double gpsTime) {
    this.gpsTime = gpsTime;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "gpsTime", this.gpsTime);
    return map;
  }

  @Override
  public void write(final EndianOutput out) {
    super.write(out);
    out.writeLEDouble(this.gpsTime);
  }
}
