package com.revolsys.io.html;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

public class XhtmlDataObjectWriterFactory extends
  AbstractDataObjectAndGeometryWriterFactory {
  public XhtmlDataObjectWriterFactory() {
    super("XHMTL", true, true);
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  @Override
  public Writer<Record> createDataObjectWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new XhtmlDataObjectWriter(metaData, writer);
  }
}
