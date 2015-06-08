package com.revolsys.format.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Writer;

public class XmlRecordIoFactory extends AbstractRecordAndGeometryWriterFactory {
  public XmlRecordIoFactory() {
    super("XML", true, true);
    addMediaTypeAndFileExtension("text/xml", "xml");
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);
    return new XmlRecordWriter(recordDefinition, writer);
  }

}
