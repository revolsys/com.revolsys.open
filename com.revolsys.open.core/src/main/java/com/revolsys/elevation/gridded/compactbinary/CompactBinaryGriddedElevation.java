package com.revolsys.elevation.gridded.compactbinary;

import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class CompactBinaryGriddedElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelWriterFactory {
  public CompactBinaryGriddedElevation() {
    super("");
  }

  @Override
  public GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource) {
    return new CompactBinaryGriddedElevationModelWriter(resource);
  }
}
