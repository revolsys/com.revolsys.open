package com.revolsys.elevation.cloud;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.IoFactory;
import com.revolsys.spring.resource.Resource;

public interface PointCloudWriteFactory extends IoFactory {
  boolean writePointCloud(PointCloud<?> pointCloud, Resource resource, MapEx properties);
}
