package com.revolsys.data.record.io;

import java.util.Map;

import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;

public class RecordWriterGeometryWriter extends AbstractWriter<Geometry> {
  private final Writer<Record> writer;

  public RecordWriterGeometryWriter(final Writer<Record> writer) {
    this.writer = writer;
  }

  @Override
  public void close() {
    this.writer.close();
  }

  @Override
  public void flush() {
    this.writer.flush();
  }

  @Override
  public Map<String, Object> getProperties() {
    return this.writer.getProperties();
  }

  @Override
  public <V> V getProperty(final String name) {
    return (V)this.writer.getProperty(name);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    this.writer.setProperty(name, value);
  }

  @Override
  public void write(final Geometry geometry) {
    final RecordDefinition recordDefinition = Records.createGeometryRecordDefinition();
    final Record object = new ArrayRecord(recordDefinition);
    object.setGeometryValue(geometry);
    this.writer.write(object);
  }

}
