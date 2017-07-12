package com.revolsys.elevation.cloud.las;

import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.spring.resource.Resource;

public class LasReader {

  public PointCloud readPointCloud(final Resource resource) {
    final LasPointCloud lasPointCloud = new LasPointCloud(resource);
    lasPointCloud.read();
    return lasPointCloud;
  }
}
