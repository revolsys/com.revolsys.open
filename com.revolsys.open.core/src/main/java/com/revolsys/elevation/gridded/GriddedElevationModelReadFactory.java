package com.revolsys.elevation.gridded;

import java.util.Map;

import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModelReadFactory extends ReadIoFactory {
  default GriddedElevationModel newGriddedElevationModel(final Resource resource,
    final Map<String, ? extends Object> properties) {
    try (
      GriddedElevationModelReader reader = newGriddedElevationModelReader(resource, properties)) {
      return reader.read();
    }
  }

  GriddedElevationModelReader newGriddedElevationModelReader(Resource resource,
    Map<String, ? extends Object> properties);
}
