package com.revolsys.record.io.format.json;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;

public class JsonRecordIoFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory {
  public static final Record toRecord(final RecordDefinition recordDefinition,
    final String string) {
    final StringReader in = new StringReader(string);
    final JsonRecordIterator iterator = new JsonRecordIterator(recordDefinition, in, true);
    try {
      if (iterator.hasNext()) {
        return iterator.next();
      } else {
        return null;
      }
    } finally {
      iterator.close();
    }
  }

  public static List<Record> toRecordList(final RecordDefinition recordDefinition,
    final String string) {
    final StringReader in = new StringReader(string);
    final JsonRecordIterator iterator = new JsonRecordIterator(recordDefinition, in);
    try {
      final List<Record> objects = new ArrayList<>();
      while (iterator.hasNext()) {
        final Record object = iterator.next();
        objects.add(object);
      }
      return objects;
    } finally {
      iterator.close();
    }
  }

  public static final String toString(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final StringWriter writer = new StringWriter();
    final JsonRecordWriter recordWriter = new JsonRecordWriter(recordDefinition, writer);
    recordWriter.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, Boolean.TRUE);
    recordWriter.write(object);
    recordWriter.close();
    return writer.toString();
  }

  public static String toString(final RecordDefinition recordDefinition,
    final List<? extends Map<String, Object>> list) {
    final StringWriter writer = new StringWriter();
    final JsonRecordWriter recordWriter = new JsonRecordWriter(recordDefinition, writer);
    for (final Map<String, Object> map : list) {
      final Record object = new ArrayRecord(recordDefinition);
      object.setValues(map);
      recordWriter.write(object);
    }
    recordWriter.close();
    return writer.toString();
  }

  public static String toString(final RecordDefinition recordDefinition,
    final Map<String, ? extends Object> parameters) {
    final Record object = new ArrayRecord(recordDefinition);
    object.setValues(parameters);
    return toString(object);
  }

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
