package com.revolsys.elevation.cloud.las;

import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.PointCloudReaderFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class LasPointCloudFactory extends AbstractIoFactory implements PointCloudReaderFactory {

  public LasPointCloudFactory() {
    super("LASer Point Cloud");
    addMediaTypeAndFileExtension("application/vnd.las", "las");
    addMediaTypeAndFileExtension("application/vnd.laz", "laz");
    addFileExtension("las.zip");
    addFileExtension("las.gz");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <P extends Point, PC extends PointCloud<P>> PC readPointCloud(final Resource resource) {
    return (PC)new LasPointCloud(resource);
  }

}
