package com.revolsys.io.kml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;

public class KmlDataObjectWriterFactory extends
  AbstractDataObjectAndGeometryWriterFactory {
  public KmlDataObjectWriterFactory() {
    super(Kml22Constants.FORMAT_DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(Kml22Constants.MEDIA_TYPE,
      Kml22Constants.FILE_EXTENSION);
    setCoordinateSystems(EpsgCoordinateSystems.getCoordinateSystem(4326));
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);
    return new KmlDataObjectWriter(writer);
  }
}
