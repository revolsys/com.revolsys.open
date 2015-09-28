package com.revolsys.record.io;

import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class RecordIo {
  public static void copyRecords(final Iterable<Record> reader, final Writer<Record> writer) {
    if (reader != null && writer != null) {
      for (final Record record : reader) {
        writer.write(record);
      }
    }
  }

  public static void copyRecords(final Object source, final Object target) {
    try (
      RecordReader reader = RecordReader.newRecordReader(source)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + source);
      } else {
        copyRecords(reader, target);
      }
    }

  }

  public static void copyRecords(final Object source, final Writer<Record> writer) {
    try (
      RecordReader reader = RecordReader.newRecordReader(source)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + source);
      } else {
        copyRecords((Iterable<Record>)reader, writer);
      }
    }

  }

  public static void copyRecords(final RecordReader reader, final Object target) {
    if (reader != null) {
      final RecordDefinition recordDefinition = reader.getRecordDefinition();
      try (
        RecordWriter writer = RecordWriter.newRecordWriter(recordDefinition, target)) {
        if (writer == null) {
          throw new IllegalArgumentException("Unable to create writer " + target);
        } else {
          copyRecords((Iterable<Record>)reader, writer);
        }
      }
    }
  }

}
