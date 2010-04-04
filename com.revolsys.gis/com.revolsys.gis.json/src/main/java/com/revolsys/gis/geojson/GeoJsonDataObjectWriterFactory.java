package com.revolsys.gis.geojson;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;

public class GeoJsonDataObjectWriterFactory extends
  AbstractDataObjectAndGeometryWriterFactory {

  public GeoJsonDataObjectWriterFactory() {
    super(GeoJsonConstants.DESCRIPTION);
    addMediaTypeAndFileExtension(GeoJsonConstants.MEDIA_TYPE,
      GeoJsonConstants.FILE_EXTENSION);
  }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset) {
    return new GeoJsonDataObjectWriter(new OutputStreamWriter(outputStream,
      charset));
  }
}
