package com.revolsys.elevation.cloud.las;

import java.nio.ByteOrder;
import java.util.List;

import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class LasPointCloudWriter extends BaseObjectWithProperties implements BaseCloseable {

  private final Resource resource;

  private ChannelWriter out;

  public LasPointCloudWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    final ChannelWriter out = this.out;
    if (out != null) {
      this.out = null;
      out.close();
    }
  }

  protected void writeHeader(final LasPointCloudHeader header) {
    header.writeHeader(this.out);
  }

  public boolean writePointCloud(final PointCloud<?> pointCloud) {
    if (pointCloud instanceof LasPointCloud) {
      final LasPointCloud lasPointCloud = (LasPointCloud)pointCloud;
      this.out = this.resource.newChannelWriter(8192, ByteOrder.LITTLE_ENDIAN);

      final LasPointCloudHeader header = lasPointCloud.getHeader();
      writeHeader(header);
      final ChannelWriter out = this.out;

      final List<LasPoint> points = lasPointCloud.getPoints();
      writePoints(out, points);
      return true;
    } else {
      return false;
    }
  }

  protected void writePoints(final ChannelWriter out, final List<LasPoint> points) {
    for (final LasPoint point : points) {
      point.writeLasPoint(out);
    }
  }
}
