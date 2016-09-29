package com.revolsys.elevation.gridded;

import java.util.Map;

import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModelReadFactory extends ReadIoFactory {
  GriddedElevationModel newGriddedElevationModel(Resource resource,
    Map<String, ? extends Object> properties);
}
