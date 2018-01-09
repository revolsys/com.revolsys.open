package com.revolsys.elevation.gridded.byn;

import java.util.Map;

import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class Byn extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory {

  public static final String MEDIA_TYPE = "image/x-gc-ca-byn";

  public static final String FILE_EXTENSION = "byn";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final int HEADER_SIZE = 80;

  public static int bufferSize(final int width, final int height) {
    return HEADER_SIZE + width * height * 4;
  }

  public Byn() {
    super("BYN Geoid Model");

    addMediaTypeAndFileExtension(MEDIA_TYPE, FILE_EXTENSION);
    addFileExtension(FILE_EXTENSION_ZIP);
    addFileExtension(FILE_EXTENSION_GZ);
  }

  @Override
  public BynReader newGriddedElevationModelReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    return new BynReader(resource, properties);
  }

}
