package com.revolsys.gis.elevation.gridded;

import com.revolsys.io.IoFactory;
import com.revolsys.spring.resource.Resource;

public interface GriddedElevationModelWriterFactory extends IoFactory {
  GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource);
}
