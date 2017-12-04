package com.revolsys.elevation.cloud.las;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.PointCloudReadFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class LasPointCloudFactory extends AbstractIoFactory implements PointCloudReadFactory {

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

}
