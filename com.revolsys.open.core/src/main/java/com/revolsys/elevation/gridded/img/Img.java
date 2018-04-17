package com.revolsys.elevation.gridded.img;

import java.util.Map;

import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class Img extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory {

  public static final String FILE_EXTENSION = "img";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public Img() {
    super("USGS DEM");
    addMediaTypeAndFileExtension("image/x-img-dem", FILE_EXTENSION);
    addFileExtension(FILE_EXTENSION_ZIP);
    addFileExtension(FILE_EXTENSION_GZ);
  }

  @Override
  public GriddedElevationModelReader newGriddedElevationModelReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    return new ImgGriddedElevationReader(resource, properties);
  }
}
