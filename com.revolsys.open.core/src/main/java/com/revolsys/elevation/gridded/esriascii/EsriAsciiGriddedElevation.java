package com.revolsys.elevation.gridded.esriascii;

import java.util.Map;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class EsriAsciiGriddedElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelReadFactory, GriddedElevationModelWriterFactory {
  public static final String FILE_EXTENSION = "asc";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public static final String PROPERTY_READ_DATA = "readData";

  public EsriAsciiGriddedElevation() {
    super("ESRI ASCII Grid");
    addMediaTypeAndFileExtension("image/x-esri-ascii-grid", FILE_EXTENSION);
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public GriddedElevationModel newGriddedElevationModel(final Resource resource,
    final Map<String, ? extends Object> properties) {
    try (
      EsriAsciiGriddedElevationModelReader reader = new EsriAsciiGriddedElevationModelReader(
        resource, properties)) {
      return reader.read();
    }
  }

  @Override
  public GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource) {
    return new EsriAsciiGriddedElevationModelWriter(resource);
  }

}
