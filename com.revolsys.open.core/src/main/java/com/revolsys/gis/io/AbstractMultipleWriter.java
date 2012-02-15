package com.revolsys.gis.io;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;

public abstract class AbstractMultipleWriter extends AbstractWriter<DataObject> {
  private static final Logger LOG = Logger.getLogger(AbstractMultipleWriter.class);

  private final Map<DataObjectMetaData, Writer<DataObject>> writers = new HashMap<DataObjectMetaData, Writer<DataObject>>();

  public AbstractMultipleWriter() {
  }

  @Override
  public void close() {
    for (final Writer<DataObject> writer : writers.values()) {
      try {
        writer.close();
      } catch (final Throwable t) {
        LOG.error("Unable to close writer", t);
      }
    }
  }

  protected abstract Writer<DataObject> createWriter(
    final DataObjectMetaData metaData);

  private Writer<DataObject> getWriter(final DataObjectMetaData metaData) {
    Writer<DataObject> writer = writers.get(metaData);
    if (writer == null) {
      writer = createWriter(metaData);
      writers.put(metaData, writer);
    }
    return writer;
  }

  public void write(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    final Writer<DataObject> writer = getWriter(metaData);
    writer.write(object);
  }
}
