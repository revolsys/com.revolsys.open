package com.revolsys.io.json;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.data.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Writer;

public class JsonRecordIoFactory extends AbstractRecordAndGeometryWriterFactory {
  public static final Record toRecord(final RecordDefinition metaData,
    final String string) {
    final StringReader in = new StringReader(string);
    final JsonRecordIterator iterator = new JsonRecordIterator(metaData, in,
      true);
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

  public static List<Record> toRecordList(final RecordDefinition metaData,
    final String string) {
    final StringReader in = new StringReader(string);
    final JsonRecordIterator iterator = new JsonRecordIterator(metaData, in);
    try {
      final List<Record> objects = new ArrayList<Record>();
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
    final RecordDefinition metaData = object.getMetaData();
    final StringWriter writer = new StringWriter();
    final JsonRecordWriter dataObjectWriter = new JsonRecordWriter(metaData,
      writer);
    dataObjectWriter.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY,
      Boolean.TRUE);
    dataObjectWriter.write(object);
    dataObjectWriter.close();
    return writer.toString();
  }

  public static String toString(final RecordDefinition metaData,
    final List<? extends Map<String, Object>> list) {
    final StringWriter writer = new StringWriter();
    final JsonRecordWriter dataObjectWriter = new JsonRecordWriter(metaData,
      writer);
    for (final Map<String, Object> map : list) {
      final Record object = new ArrayRecord(metaData);
      object.setValues(map);
      dataObjectWriter.write(object);
    }
    dataObjectWriter.close();
    return writer.toString();
  }

  public static String toString(final RecordDefinition metaData,
    final Map<String, ? extends Object> parameters) {
    final Record object = new ArrayRecord(metaData);
    object.setValues(parameters);
    return toString(object);
  }

  public JsonRecordIoFactory() {
    super("JavaScript Object Notation", true, true);
    addMediaTypeAndFileExtension("application/json", "json");
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new JsonRecordWriter(metaData, writer);
  }

}
