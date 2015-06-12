package com.revolsys.format.html;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

public class XhtmlRecordWriterFactory extends AbstractRecordAndGeometryWriterFactory {
  public XhtmlRecordWriterFactory() {
    super("XHMTL");
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream, final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new XhtmlRecordWriter(recordDefinition, writer);
  }
}
