package com.revolsys.elevation.gridded.img;

import java.util.Map;

import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class ImgGriddedElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory {

  public static final String FILE_EXTENSION = "img";

  public ImgGriddedElevation() {
    super("IMG DEM");
    addMediaTypeAndFileExtension("image/x-img-dem", FILE_EXTENSION);
  }

  @Override
  public GriddedElevationModelReader newGriddedElevationModelReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    return new ImgGriddedElevationReader(resource, properties);
  }
}
