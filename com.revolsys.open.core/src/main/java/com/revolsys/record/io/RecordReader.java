package com.revolsys.record.io;

import java.io.File;
import java.nio.file.Path;

import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Reader;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public interface RecordReader extends Reader<Record> {
  static RecordReader empty() {
    return new ListRecordReader(null);
  }

  static RecordReader empty(final RecordDefinition recordDefinition) {
    return new ListRecordReader(recordDefinition);
  }

  static boolean isReadable(final Object source) {
    return IoFactoryRegistry.isAvailable(RecordReaderFactory.class, source);
  }

  /**
   * Construct a new {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link Path}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static RecordReader newRecordReader(final Object source) {
    final RecordFactory<ArrayRecord> recordFactory = ArrayRecord.FACTORY;
    return newRecordReader(source, recordFactory);
  }

  /**
   * Construct a new {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link Path}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @param recordFactory The factory used to create records.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static RecordReader newRecordReader(final Object source,
    final RecordFactory<? extends Record> recordFactory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class, source);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.newRecordReader(source, recordFactory);
      return reader;
    }
  }

  RecordDefinition getRecordDefinition();
}
