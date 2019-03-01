package com.revolsys.elevation.cloud.las;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.PointCloudReadFactory;
import com.revolsys.elevation.cloud.PointCloudWriteFactory;
import com.revolsys.elevation.cloud.las.zip.writer.LazPointCloudWriter;
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
    return (PC)new LasPointCloud(resource, properties);
  }

  private LasPointCloudWriter newWriter(final Resource resource) {
    final String fileNameExtension = resource.getFileNameExtension();
    if ("las".equals(fileNameExtension)) {
      return new LasPointCloudWriter(resource);
    } else if ("laz".equals(fileNameExtension)) {
      return new LazPointCloudWriter(resource);
    } else {
      return null;
    }
  }

  @Override
  public boolean writePointCloud(final PointCloud<?> pointCloud, final Resource resource,
    final MapEx properties) {
    try (
      LasPointCloudWriter writer = newWriter(resource)) {
      if (writer == null) {
        return false;
      } else {
        writer.setProperties(properties);
        writer.writePointCloud(pointCloud);
        return true;
      }
    }
  }

}
