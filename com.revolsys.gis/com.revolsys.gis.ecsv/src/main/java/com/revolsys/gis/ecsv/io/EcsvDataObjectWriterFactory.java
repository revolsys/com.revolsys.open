package com.revolsys.gis.ecsv.io;


import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;

public class EcsvDataObjectWriterFactory extends 
  AbstractDataObjectAndGeometryWriterFactory {

  /** The factory instance. */
  public static final EcsvDataObjectWriterFactory INSTANCE = new EcsvDataObjectWriterFactory();

  public EcsvDataObjectWriterFactory() {
    super(EcsvConstants.DESCRIPTION);
    addMediaTypeAndFileExtension(EcsvConstants.MEDIA_TYPE, EcsvConstants.FILE_EXTENSION);
  }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset) {
    return new EcsvWriter(metaData, new OutputStreamWriter(outputStream,
      charset));
  }
}
