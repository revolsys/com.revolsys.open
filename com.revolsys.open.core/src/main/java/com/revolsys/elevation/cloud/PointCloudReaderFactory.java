package com.revolsys.elevation.cloud;

import com.revolsys.geometry.model.Point;
import com.revolsys.io.IoFactory;
import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface PointCloudReaderFactory extends ReadIoFactory {
  static <P extends Point, PC extends PointCloud<P>> PC openPointCloud(final Resource resource) {
    final PointCloudReaderFactory factory = IoFactory.factory(PointCloudReaderFactory.class,
      resource);
    if (factory == null) {
      return null;
    } else {
      final PC pointCloud = factory.readPointCloud(resource);
      return pointCloud;
    }
  }

  <P extends Point, PC extends PointCloud<P>> PC readPointCloud(Resource resource);
}
