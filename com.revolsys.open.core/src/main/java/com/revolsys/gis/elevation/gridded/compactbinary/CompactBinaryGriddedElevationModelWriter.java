package com.revolsys.gis.elevation.gridded.compactbinary;

import com.revolsys.gis.elevation.gridded.GriddedElevationModel;
import com.revolsys.gis.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.io.AbstractWriter;
import com.revolsys.spring.resource.Resource;

public class CompactBinaryGriddedElevationModelWriter extends AbstractWriter<GriddedElevationModel>
  implements GriddedElevationModelWriter {

  private Resource resource;

  public CompactBinaryGriddedElevationModelWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    this.resource = null;
  }

  @Override
  public void write(final GriddedElevationModel model) {
    if (this.resource == null) {
      throw new IllegalStateException("Writer is closed");
    } else {

    }
  }

}
