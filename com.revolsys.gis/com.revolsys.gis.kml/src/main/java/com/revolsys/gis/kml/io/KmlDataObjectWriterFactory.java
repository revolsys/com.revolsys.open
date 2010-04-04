package com.revolsys.gis.kml.io;

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
    super(Kml22Constants.DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.MEDIA_TYPE,
      Kml22Constants.FILE_EXTENSION);
    setCoordinateSystems(EpsgCoordinateSystems.getCoordinateSystem(4326));
  }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);
    return new KmlWriter(writer);
  }
}
