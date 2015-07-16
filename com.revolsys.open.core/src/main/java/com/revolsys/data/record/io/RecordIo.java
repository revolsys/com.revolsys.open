package com.revolsys.data.record.io;

import java.io.File;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;

public class RecordIo {
  public static void copyRecords(final File sourceFile, final File targetFile) {
    try (
      RecordReader reader = RecordReader.create(sourceFile)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + sourceFile);
      } else {
        copyRecords(reader, targetFile);
      }
    }

  }

  public static void copyRecords(final File sourceFile, final Writer<Record> writer) {
    try (
      RecordReader reader = RecordReader.create(sourceFile)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + sourceFile);
      } else {
        copyRecords(reader, writer);
      }
    }

  }

  public static void copyRecords(final Reader<Record> reader, final Writer<Record> writer) {
    if (reader != null && writer != null) {
      for (final Record record : reader) {
        writer.write(record);
      }
    }
  }

  public static void copyRecords(final RecordReader reader, final File targetFile) {
    if (reader != null) {
      final RecordDefinition recordDefinition = reader.getRecordDefinition();
      try (
        Writer<Record> writer = RecordWriter.create(recordDefinition, targetFile)) {
        if (writer == null) {
          throw new IllegalArgumentException("Unable to create writer " + targetFile);
        } else {
          copyRecords(reader, writer);
        }
      }
    }
  }

}
