package com.revolsys.xhtml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;

public class XhtmlDataObjectIoFactory extends
  AbstractDataObjectAndGeometryWriterFactory {

  /** The factory instance. */
  public static final XhtmlDataObjectIoFactory INSTANCE = new XhtmlDataObjectIoFactory();

  public XhtmlDataObjectIoFactory() {
    super("XHTML");
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream,
    final Charset charset) {
    return new XhtmlDataObjectWriter(metaData, new OutputStreamWriter(
      outputStream, charset));
  }

}
