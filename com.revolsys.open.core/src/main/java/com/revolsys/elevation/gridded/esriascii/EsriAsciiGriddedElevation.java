package com.revolsys.elevation.gridded.esriascii;

import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.geometry.io.PointReader;
import com.revolsys.geometry.io.PointReaderFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public class EsriAsciiGriddedElevation extends AbstractIoFactoryWithCoordinateSystem implements
  GriddedElevationModelReadFactory, GriddedElevationModelWriterFactory, PointReaderFactory {
  public static final String FILE_EXTENSION = "asc";

  public static final String FILE_EXTENSION_ZIP = FILE_EXTENSION + ".zip";

  public static final String FILE_EXTENSION_GZ = FILE_EXTENSION + ".gz";

  public static final String PROPERTY_READ_DATA = "readData";

  public static EsriAsciiGriddedElevationModelReader newReader(final Object resource) {
    return newReader(resource, MapEx.EMPTY);
  }

  public static EsriAsciiGriddedElevationModelReader newReader(final Object source,
    final Map<String, ? extends Object> properties) {
    final Resource resource = Resource.getResource(source);
    final EsriAsciiGriddedElevationModelReader reader = new EsriAsciiGriddedElevationModelReader(
      resource, properties);
    reader.init();
    return reader;
  }

  public EsriAsciiGriddedElevation() {
    super("ESRI ASCII Grid");
    addMediaTypeAndFileExtension("image/x-esri-ascii-grid", FILE_EXTENSION);
    addFileExtension(FILE_EXTENSION_ZIP);
    addFileExtension(FILE_EXTENSION_GZ);
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

  @Override
  public PointReader newPointReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    return new EsriAsciiGriddedElevationModelReader(resource, properties);
  }

}
