package com.revolsys.record.io.format.json;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;

public class JsonRecordIoFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory {
  public JsonRecordIoFactory() {
    super("JavaScript Object Notation");
    addMediaTypeAndFileExtension("application/json", "json");
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new JsonRecordWriter(recordDefinition, writer);
  }

}
