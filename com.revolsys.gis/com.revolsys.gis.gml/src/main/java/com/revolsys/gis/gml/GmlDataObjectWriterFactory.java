package com.revolsys.gis.gml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.io.GeometryReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.Writer;

public class GmlDataObjectWriterFactory extends
  AbstractDataObjectAndGeometryWriterFactory implements GeometryReaderFactory {
  public GmlDataObjectWriterFactory() {
    super(GmlConstants.FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(GmlConstants.MEDIA_TYPE,
      GmlConstants.FILE_EXTENSION);
  }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);
    return new GmlDataObjectWriter(metaData, writer);
  }

  public GeometryReader createGeometryReader(
    Resource resource) {
    final GmlGeometryIterator iterator = new GmlGeometryIterator(resource);
    return new GeometryReader(iterator);
  }
}
