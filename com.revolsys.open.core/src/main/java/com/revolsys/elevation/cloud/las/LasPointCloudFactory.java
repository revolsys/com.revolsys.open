package com.revolsys.elevation.cloud.las;

import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.PointCloudReadFactory;
import com.revolsys.elevation.cloud.PointCloudWriteFactory;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.LasZipPointCloudWriter;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class LasPointCloudFactory extends AbstractIoFactory
  implements PointCloudReadFactory, PointCloudWriteFactory {

  public LasPointCloudFactory() {
    super("LASer Point Cloud");
    addMediaTypeAndFileExtension("application/vnd.las", "las");
    addMediaTypeAndFileExtension("application/vnd.laz", "laz");
    addFileExtension("las.zip");
    addFileExtension("las.gz");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <P extends Point, PC extends PointCloud<P>> PC newPointCloud(final Resource resource,
    final MapEx properties) {
    final LasPointCloud pointCloud = new LasPointCloud(resource, properties);
    if (pointCloud.isExists()) {
      return (PC)pointCloud;
    } else {
      return null;
    }
  }

  private LasPointCloudWriter newWriter(final PointCloud<?> pointCloud, final Resource resource,
    final MapEx properties) {
    if (pointCloud instanceof LasPointCloud) {
      final LasPointCloud lasPointCloud = (LasPointCloud)pointCloud;
      final String fileNameExtension = resource.getFileNameExtension();
      LasPointCloudWriter writer;
      if ("las".equals(fileNameExtension)) {
        writer = new LasPointCloudWriter(lasPointCloud, resource, properties);
      } else if ("laz".equals(fileNameExtension)) {
        writer = new LasZipPointCloudWriter(lasPointCloud, resource, properties);
      } else {
        return null;
      }
      writer.open();
      return writer;
    } else {
      return null;
    }
  }

  @Override
  public boolean writePointCloud(final PointCloud<?> pointCloud, final Resource resource,
    final MapEx properties) {
    try (
      LasPointCloudWriter writer = newWriter(pointCloud, resource, properties)) {
      if (writer == null) {
        return false;
      } else {
        final List<LasPoint> points = ((LasPointCloud)pointCloud).getPoints();
        writer.writePoints(points);
        return true;
      }
    }
  }

}
