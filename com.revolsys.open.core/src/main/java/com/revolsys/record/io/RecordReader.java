package com.revolsys.record.io;

import java.io.File;

import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.format.zip.ZipRecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public interface RecordReader extends Reader<Record>, RecordDefinitionProxy {
  static RecordReader empty() {
    return new ListRecordReader(null);
  }

  static RecordReader empty(final RecordDefinition recordDefinition) {
    return new ListRecordReader(recordDefinition);
  }

  static boolean isReadable(final Object source) {
    return IoFactory.isAvailable(RecordReaderFactory.class, source);
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
    return newRecordReader(source, ArrayRecord.FACTORY);
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

  static RecordReader newZipRecordReader(final Object source, final String fileExtension) {
    final Resource resource = Resource.getResource(source);
    return new ZipRecordReader(resource, fileExtension, ArrayRecord.FACTORY);
  }
}
