package com.revolsys.gis.wkt;


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;

public class WktDataObjectWriterFactory extends 
  AbstractDataObjectAndGeometryWriterFactory {

  /** The factory instance. */
  public static final WktDataObjectWriterFactory INSTANCE = new WktDataObjectWriterFactory();

  public WktDataObjectWriterFactory() {
    super(WktConstants.DESCRIPTION);
    addMediaTypeAndFileExtension(WktConstants.MEDIA_TYPE, WktConstants.FILE_EXTENSION);
  }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset) {
    return new WktDataObjectWriter(metaData, new OutputStreamWriter(outputStream,
      charset));
  }
}
