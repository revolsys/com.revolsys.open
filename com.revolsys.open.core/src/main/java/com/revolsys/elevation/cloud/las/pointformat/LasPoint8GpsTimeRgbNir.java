package com.revolsys.elevation.cloud.las.pointformat;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.endian.EndianOutput;

public class LasPoint8GpsTimeRgbNir extends LasPoint7GpsTimeRgb implements LasPointNir {
  private static final long serialVersionUID = 1L;

  private int nir;

  public LasPoint8GpsTimeRgbNir(final LasPointCloud pointCloud) {
    super(pointCloud);
  }

  @Override
  public int getNir() {
    return this.nir;
  }

  @Override
  public LasPointFormat getPointFormat() {
    return LasPointFormat.ExtendedGpsTimeRgbNir;
  }

  @Override
  public void read(final LasPointCloud pointCloud, final ChannelReader reader) {
    super.read(pointCloud, reader);
    this.nir = reader.getUnsignedShort();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "nir", this.nir);
    return map;
  }

  @Override
  public void write(final EndianOutput out) {
    super.write(out);
    out.writeLEUnsignedShort(this.nir);
  }

}
