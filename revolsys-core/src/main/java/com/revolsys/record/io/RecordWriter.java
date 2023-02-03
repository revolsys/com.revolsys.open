package com.revolsys.record.io;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

import reactor.core.publisher.Flux;

public interface RecordWriter extends Writer<Record>, RecordDefinitionProxy {

  public static class Builder {
    static <R extends Record> Flux<R> fluxWrite(final RecordDefinitionProxy recordDefinition,
      final Supplier<Object> targetSupplier, final Consumer<RecordWriter> writerInitalizer,
      final Flux<R> records) {
      // TODO have writers use Mono to write asynchronously
      return BaseCloseable.fluxUsing(() -> new LazyRecordWriter(recordDefinition, () -> {
        final RecordWriter writer = newRecordWriter(recordDefinition, targetSupplier.get());
        if (writerInitalizer != null) {
          writerInitalizer.accept(writer);
        }
        return writer;
      }), //
        (writer) -> records.doOnNext(writer::write) //
      );
    }

    private JsonObject properties = JsonObject.hash();

    private RecordWriterFactory factory;

    private RecordDefinition recordDefinition;

    private Resource target;

    private Supplier<Object> targetSupplier;

    private Consumer<RecordWriter> initalizer;

    private Builder() {
    }

    private Builder(final RecordWriterFactory factory) {
      this.factory = factory;
    }

    public RecordWriter build() {
      if (this.recordDefinition != null) {
        Resource resource = this.target;
        if (resource == null && this.targetSupplier != null) {
          this.target = resource = Resource.getResource(this.targetSupplier.get());
        }
        if (resource != null) {
          RecordWriterFactory factory = this.factory;
          if (factory == null) {
            factory = IoFactory.factory(RecordWriterFactory.class, this.target);

          }
          if (factory != null) {
            final RecordWriter writer = factory.newRecordWriter(this.recordDefinition, resource);
            writer.setProperties(this.properties);
            if (this.initalizer != null) {
              this.initalizer.accept(writer);
            }
            return writer;
          }
        }
      }
      return null;
    }

    public Builder factory(final RecordWriterFactory factory) {
      this.factory = factory;
      return this;
    }

    public <R extends Record> Flux<R> fluxWrite(final Flux<R> records) {
      // TODO have writers use Mono to write asynchronously
      final Callable<LazyRecordWriter> supplier = () -> new LazyRecordWriter(this.recordDefinition,
        this::build);
      final Function<LazyRecordWriter, Publisher<R>> action = (writer) -> {
        if (writer == null) {
          throw new IllegalStateException("Unable to create writer for :" + this.target);
        } else {
          return records.doOnNext(writer::write);
        }
      };
      return BaseCloseable.fluxUsing(supplier, action);
    }

    public Builder initalizer(final Consumer<RecordWriter> initalizer) {
      this.initalizer = initalizer;
      return this;
    }

    public Builder property(final String name, final Object value) {
      this.properties.addValue(name, value);
      return this;
    }

    public Builder recordDefinition(final RecordDefinitionProxy recordDefinition) {
      if (recordDefinition != null) {
        this.recordDefinition = recordDefinition.getRecordDefinition();
      }
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

    public Builder target(final Object target) {
      if (target != null) {
        this.target = Resource.getResource(target);
      }
      return this;
    }

    public Builder targetSupplier(final Supplier<Object> target) {
      if (target != null) {
        this.targetSupplier = target;
      }
      return this;
    }

  }

  static Builder builder() {
    return new Builder();
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
    return IoFactory.isAvailable(RecordWriterFactory.class, fileNameExtension);
  }

  static RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Object target) {
    if (recordDefinition != null) {
      final RecordDefinition definition = recordDefinition.getRecordDefinition();
      if (definition != null) {
        final Resource resource = Resource.getResource(target);
        final RecordWriterFactory writerFactory = IoFactory.factory(RecordWriterFactory.class,
          resource);
        if (writerFactory != null) {
          final RecordWriter writer = writerFactory.newRecordWriter(definition, resource);
          return writer;
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  static <R extends RecordWriter> R newRecordWriter(final String fileExtension,
    final RecordDefinitionProxy recordDefinition, final Object target) {
    if (recordDefinition != null) {
      final RecordDefinition definition = recordDefinition.getRecordDefinition();
      if (definition != null) {
        final Resource resource = Resource.getResource(target);
        final RecordWriterFactory writerFactory = IoFactory
          .factoryByFileExtension(RecordWriterFactory.class, fileExtension);
        if (writerFactory != null) {
          final RecordWriter writer = writerFactory.newRecordWriter(definition, resource);
          return (R)writer;
        }
      }
    }
    return null;
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return GeometryFactory.DEFAULT_2D;
    } else {
      return recordDefinition.getGeometryFactory();
    }
  }

  default ClockDirection getPolygonRingDirection() {
    return ClockDirection.NONE;
  }

  @Override
  default RecordDefinition getRecordDefinition() {
    return null;
  }

  boolean isIndent();

  default boolean isValueWritable(final Object value) {
    return Property.hasValue(value) || isWriteNulls() || value instanceof Geometry;
  }

  boolean isWriteCodeValues();

  boolean isWriteNulls();

  default Record newRecord() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition);
  }

  default Record newRecord(final Iterable<? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition, values);
  }

  default Record newRecord(final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition, values);
  }

  default Record newRecord(final Object... values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return new ArrayRecord(recordDefinition, values);
  }

  void setIndent(final boolean indent);

  void setWriteCodeValues(boolean writeCodeValues);

  void setWriteNulls(boolean writeNulls);

  default void write(final Iterable<? extends Object> values) {
    final Record record = newRecord(values);
    write(record);
  }

  default void write(final Map<String, ? extends Object> map) {
    final Record record = newRecord(map);
    write(record);
  }

  default void write(final Object... values) {
    final Record record = newRecord(values);
    write(record);
  }

  default int writeAll(final Iterable<? extends Record> records) {
    int i = 0;
    for (final Record record : records) {
      write(record);
      i++;
    }
    return i;
  }

  default void writeNewRecord(final Record record) {
    final Record writeRecord = newRecord(record);
    write(writeRecord);
  }

  default void writeNewRecords(final Iterable<? extends Record> records) {
    for (final Record record : records) {
      writeNewRecord(record);
    }
  }
}
