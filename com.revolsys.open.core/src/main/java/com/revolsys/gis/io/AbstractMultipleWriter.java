package com.revolsys.gis.io;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.Writer;

public abstract class AbstractMultipleWriter extends AbstractRecordWriter {
  private static final Logger LOG = Logger.getLogger(AbstractMultipleWriter.class);

  private final Map<RecordDefinition, Writer<Record>> writers = new HashMap<RecordDefinition, Writer<Record>>();

  public AbstractMultipleWriter() {
  }

  @Override
  public void close() {
    for (final Writer<Record> writer : this.writers.values()) {
      try {
        writer.close();
      } catch (final Throwable t) {
        LOG.error("Unable to close writer", t);
      }
    }
  }

  protected abstract Writer<Record> createWriter(
    final RecordDefinition recordDefinition);

  private Writer<Record> getWriter(final RecordDefinition recordDefinition) {
    Writer<Record> writer = this.writers.get(recordDefinition);
    if (writer == null) {
      writer = createWriter(recordDefinition);
      this.writers.put(recordDefinition, writer);
    }
    return writer;
  }

  @Override
  public void write(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final Writer<Record> writer = getWriter(recordDefinition);
    writer.write(object);
  }
}
