package com.revolsys.elevation.gridded.usgsdem;

import java.util.Map;

import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class UsgsGriddedElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory {

  public static final String FILE_EXTENSION = "dem";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public static final String PROPERTY_READ_DATA = "readData";

  public UsgsGriddedElevation() {
    super("USGS DEM");
    addMediaTypeAndFileExtension("image/x-usgs-dem", FILE_EXTENSION);
    addFileExtension(FILE_EXTENSION_ZIP);
    addFileExtension(FILE_EXTENSION_GZ);
  }

  @Override
  public GriddedElevationModelReader newGriddedElevationModelReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    return new UsgsGriddedElevationReader(resource, properties);
  }
}
