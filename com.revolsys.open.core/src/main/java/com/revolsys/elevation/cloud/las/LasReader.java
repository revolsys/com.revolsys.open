package com.revolsys.elevation.cloud.las;

import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.spring.resource.Resource;

public class LasReader {

  public PointCloud readPointCloud(final Resource resource) {
    return new LasPointCloud(resource);
  }
}
