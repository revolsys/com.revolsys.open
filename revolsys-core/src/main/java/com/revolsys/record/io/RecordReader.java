package com.revolsys.record.io;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Function;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.FileNameProxy;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.zip.ZipRecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public interface RecordReader extends Reader<Record>, RecordDefinitionProxy {
  public static class Builder {
    private JsonObject properties = JsonObject.hash();

    private final RecordReaderFactory readerFactory;

    private RecordFactory<? extends Record> recordFactory = ArrayRecord.FACTORY;

    private Object source;

    private Builder(final RecordReaderFactory readerFactory) {
      this.readerFactory = readerFactory;
    }

    public Builder addProperty(final String name, final Object value) {
      this.properties.addValue(name, value);
      return this;
    }

    public RecordReader build() {
      if (this.readerFactory == null) {
        return null;
      } else {
        final Resource resource = this.readerFactory.getZipResource(this.source);
        return this.readerFactory.newRecordReader(resource, this.recordFactory, this.properties);
      }
    }

    public Builder setGeometryFactory(final GeometryFactory geometryFactory) {
      this.properties.addValue("geometryFactory", geometryFactory);
      return this;
    }

    public Builder setProperties(final JsonObject properties) {
      if (properties == null) {
        this.properties = JsonObject.hash();
      } else {
        this.properties = properties;
      }
      return this;
    }

    public Builder setRecordFactory(final RecordFactory<? extends Record> recordFactory) {
      this.recordFactory = recordFactory;
      return this;
    }

    public Builder setSource(final Object source) {
      this.source = source;
      return this;
    }
  }

  static Builder builder(final Object source) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class, source);
    return new Builder(readerFactory).setSource(source);

  }

  static Builder builderFileName(final FileNameProxy fileNameProxy) {
    final RecordReaderFactory readerFactory;
    if (fileNameProxy == null) {
      readerFactory = null;
    } else {
      final String fileName = fileNameProxy.getFileName();
      readerFactory = IoFactory.factoryByFileName(RecordReaderFactory.class, fileName);
    }
    return new Builder(readerFactory);
  }

  static Builder builderFileName(final String fileName) {
    final RecordReaderFactory readerFactory = IoFactory.factoryByFileName(RecordReaderFactory.class,
      fileName);
    return new Builder(readerFactory);
  }

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

  static RecordReader newRecordReader(final Object source, final GeometryFactory geometryFactory) {
    final JsonObject properties = JsonObject.hash("geometryFactory", geometryFactory);
    return newRecordReader(source, ArrayRecord.FACTORY, properties);
  }

  static RecordReader newRecordReader(final Object source, final MapEx properties) {
    return newRecordReader(source, ArrayRecord.FACTORY, properties);
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
    return newRecordReader(source, recordFactory, JsonObject.EMPTY);
  }

  static RecordReader newRecordReader(final Object source,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class, source);
    if (readerFactory == null) {
      return null;
    } else {
      final Resource resource = readerFactory.getZipResource(source);
      final RecordReader reader = readerFactory.newRecordReader(resource, recordFactory,
        properties);
      return reader;
    }
  }

  static <V> RecordReader newRecordReader(final RecordDefinition recordDefinition,
    final Iterable<V> iterable, final Function<V, Record> converter) {
    final Iterator<V> iterator = iterable.iterator();
    return newRecordReader(recordDefinition, iterator, converter);
  }

  static <V> RecordReader newRecordReader(final RecordDefinition recordDefinition,
    final Iterator<V> iterator, final Function<V, Record> converter) {
    return new AbstractRecordReader(recordDefinition) {
      @Override
      protected Record getNext() throws NoSuchElementException {
        final V value = iterator.next();
        return converter.apply(value);
      }
    };
  }

  static RecordReader newZipRecordReader(final Object source, final String fileExtension) {
    final Resource resource = Resource.getResource(source);
    return new ZipRecordReader(resource, fileExtension, ArrayRecord.FACTORY);
  }

  static RecordReader newZipRecordReader(final Object source, final String baseName,
    final String fileExtension) {
    final Resource resource = Resource.getResource(source);
    return new ZipRecordReader(resource, baseName, fileExtension, ArrayRecord.FACTORY);
  }

  default ClockDirection getPolygonRingDirection() {
    return ClockDirection.NONE;
  }

  default Map<Identifier, Record> readRecordsById() {
    try (
      BaseCloseable closeable = this) {
      final Map<Identifier, Record> recordsById = new TreeMap<>(Identifier.comparator());
      for (final Record record : this) {
        final Identifier identifier = record.getIdentifier();
        recordsById.put(identifier, record);
      }
      return recordsById;
    }
  }
}
