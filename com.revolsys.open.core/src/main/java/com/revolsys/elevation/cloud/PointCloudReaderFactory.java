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

  // TODO figure out how to do this with decent memory usage
  // @Override
  // default GriddedElevationModel newGriddedElevationModel(final Resource resource,
  // final Map<String, ? extends Object> properties) {
  // final PointCloud<?> pointCloud = openPointCloud(resource);
  // if (pointCloud == null) {
  // return null;
  // } else {
  // return pointCloud.newGriddedElevationModel(properties);
  // }
  // }

  <P extends Point, PC extends PointCloud<P>> PC readPointCloud(Resource resource);
}
