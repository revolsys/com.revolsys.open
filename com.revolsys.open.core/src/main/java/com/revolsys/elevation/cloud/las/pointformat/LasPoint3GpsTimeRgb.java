package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.ChannelWriter;

public class LasPoint3GpsTimeRgb extends LasPoint2Rgb implements LasPointGpsTime {
  private static final long serialVersionUID = 1L;

  private double gpsTime;

  public LasPoint3GpsTimeRgb(final LasPointCloud pointCloud) {
    super(pointCloud);
    this.gpsTime = LasPoint1GpsTime.getCurrentGpsTime();
  }

  @Override
  public double getGpsTime() {
    return this.gpsTime;
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.GpsTimeRgb;
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
  public void writeLasPoint(final ChannelWriter out) {
    super.writeLasPoint(out);
    out.putDouble(this.gpsTime);
  }
}
