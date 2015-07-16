package com.revolsys.data.record.io;

import java.io.File;
import java.nio.file.Path;

import org.springframework.core.io.Resource;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Paths;
import com.revolsys.io.Writer;
import com.revolsys.util.Property;

public interface RecordWriter extends Writer<Record> {
  static Writer<Record> create(final RecordDefinition recordDefinition, final File file) {
    if (file == null) {
      return null;
    } else {
      final Path path = Paths.get(file);
      return create(recordDefinition, path);
    }
  }

  static Writer<Record> create(final RecordDefinition recordDefinition, final Path path) {
    final RecordWriterFactory writerFactory = IoFactory.factory(RecordWriterFactory.class, path);
    if (writerFactory == null) {
      return null;
    } else {
      final Writer<Record> writer = writerFactory.createRecordWriter(recordDefinition, path);
      return writer;
    }
  }

  static Writer<Record> create(final RecordDefinition recordDefinition, final Resource resource) {
    final RecordWriterFactory writerFactory = IoFactory.factory(RecordWriterFactory.class,
      resource);
    if (writerFactory == null) {
      return null;
    } else {
      final Writer<Record> writer = writerFactory.createRecordWriter(recordDefinition, resource);
      return writer;
    }
  }

  static boolean isWritable(final File file) {
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(file)) {
      if (isWritable(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  static boolean isWritable(final String fileNameExtension) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(RecordWriterFactory.class, fileNameExtension);
  }

  default RecordDefinition getRecordDefinition() {
    return null;
  }

  default boolean isIndent() {
    return BooleanStringConverter.isTrue(getProperty(IoConstants.INDENT));
  }

  default boolean isValueWritable(final Object value) {
    return Property.hasValue(value) || isWriteNulls();
  }

  default boolean isWriteNulls() {
    return BooleanStringConverter.isTrue(getProperty(IoConstants.WRITE_NULLS));
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
      setIndent(BooleanStringConverter.isTrue(value));
    }
  }

  default void setWriteNulls(final boolean writeNulls) {
    setProperty(IoConstants.WRITE_NULLS, Boolean.valueOf(writeNulls));
  }
}
