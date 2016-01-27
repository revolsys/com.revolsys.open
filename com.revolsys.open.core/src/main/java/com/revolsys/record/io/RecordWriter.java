package com.revolsys.record.io;

import java.io.File;
import java.util.Map;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Booleans;
import com.revolsys.util.Property;

public interface RecordWriter extends Writer<Record> {
  static boolean isWritable(final File file) {
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(file)) {
      if (isWritable(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  static boolean isWritable(final String fileNameExtension) {
    return IoFactory.isAvailable(RecordWriterFactory.class, fileNameExtension);
  }

  static RecordWriter newRecordWriter(final Record record, final Object target) {
    if (record != null) {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      return newRecordWriter(recordDefinition, target);
    }
    return null;
  }

  static RecordWriter newRecordWriter(final RecordDefinition recordDefinition,
    final Object target) {
    final Resource resource = Resource.getResource(target);
    final RecordWriterFactory writerFactory = IoFactory.factory(RecordWriterFactory.class,
      resource);
    if (writerFactory == null) {
      return null;
    } else {
      final RecordWriter writer = writerFactory.newRecordWriter(recordDefinition, resource);
      return writer;
    }
  }

  default RecordDefinition getRecordDefinition() {
    return null;
  }

  default boolean isIndent() {
    return Booleans.isTrue(getProperty(IoConstants.INDENT));
  }

  default boolean isValueWritable(final Object value) {
    return Property.hasValue(value) || isWriteNulls() || value instanceof Geometry;
  }

  default boolean isWriteNulls() {
    return Booleans.isTrue(getProperty(IoConstants.WRITE_NULLS));
  }

  default Record newRecord() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition);
  }

  default Record newRecord(final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition, values);
  }

  default void setIndent(final boolean indent) {

    final Boolean indentObject = Boolean.valueOf(indent);
    if (getProperty(IoConstants.INDENT) != indentObject) {
      setProperty(IoConstants.INDENT, indentObject);
    }
  }

  @Override
  default void setProperty(final String name, final Object value) {
    Writer.super.setProperty(name, value);
    if (IoConstants.INDENT.equals(name)) {
      setIndent(Booleans.isTrue(value));
    }
  }

  default void setWriteNulls(final boolean writeNulls) {
    setProperty(IoConstants.WRITE_NULLS, Boolean.valueOf(writeNulls));
  }
}
