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

  /** The factory instance. */
  public static final XmlDataObjectIoFactory INSTANCE = new XmlDataObjectIoFactory();

  public XmlDataObjectIoFactory() {
    super("XML", true, true);
    addMediaTypeAndFileExtension("text/xml", "xml");
  }

  public Writer<DataObject> createDataObjectWriter(
    final String baseName,
    final DataObjectMetaData metaData,
    final OutputStream outputStream,
    final Charset charset) {
    return new XmlDataObjectWriter(metaData, new OutputStreamWriter(
      outputStream, charset));
  }

}
