package com.revolsys.elevation.gridded;

import java.util.Map;

import com.revolsys.io.IoFactory;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModelFactory extends IoFactory {
  GriddedElevationModel newGriddedElevationModel(Resource resource,
    Map<String, ? extends Object> properties);
}
