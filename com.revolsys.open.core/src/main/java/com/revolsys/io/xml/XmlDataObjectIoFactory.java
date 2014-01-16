package com.revolsys.io.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;

public class XmlDataObjectIoFactory extends
  AbstractDataObjectAndGeometryWriterFactory {
  public XmlDataObjectIoFactory() {
    super("XML", true, true);
    addMediaTypeAndFileExtension("text/xml", "xml");
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);
    return new XmlDataObjectWriter(metaData, writer);
  }

}
