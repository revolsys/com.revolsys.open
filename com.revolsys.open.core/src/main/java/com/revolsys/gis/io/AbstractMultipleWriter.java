package com.revolsys.gis.io;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;

public abstract class AbstractMultipleWriter extends AbstractWriter<Record> {
  private static final Logger LOG = Logger.getLogger(AbstractMultipleWriter.class);

  private final Map<RecordDefinition, Writer<Record>> writers = new HashMap<RecordDefinition, Writer<Record>>();

  public AbstractMultipleWriter() {
  }

  @Override
  public void close() {
    for (final Writer<Record> writer : writers.values()) {
      try {
        writer.close();
      } catch (final Throwable t) {
        LOG.error("Unable to close writer", t);
      }
    }
  }

  protected abstract Writer<Record> createWriter(
    final RecordDefinition metaData);

  private Writer<Record> getWriter(final RecordDefinition metaData) {
    Writer<Record> writer = writers.get(metaData);
    if (writer == null) {
      writer = createWriter(metaData);
      writers.put(metaData, writer);
    }
    return writer;
  }

  @Override
  public void write(final Record object) {
    final RecordDefinition metaData = object.getMetaData();
    final Writer<Record> writer = getWriter(metaData);
    writer.write(object);
  }
}
