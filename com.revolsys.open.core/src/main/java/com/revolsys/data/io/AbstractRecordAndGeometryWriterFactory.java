package com.revolsys.data.io;

import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;

public abstract class AbstractRecordAndGeometryWriterFactory extends
  AbstractRecordWriterFactory implements GeometryWriterFactory {

  public AbstractRecordAndGeometryWriterFactory(final String name,
    final boolean geometrySupported, final boolean customAttributionSupported) {
    super(name, geometrySupported, customAttributionSupported);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final Resource resource) {
    RecordDefinition recordDefinition = RecordUtil.createGeometryMetaData();
    final Writer<Record> recordWriter = createRecordWriter(
      recordDefinition, resource);
    return createGeometryWriter(recordWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out) {
    RecordDefinition recordDefinition = RecordUtil.createGeometryMetaData();
    final Writer<Record> recordWriter = createRecordWriter(
      baseName, recordDefinition, out);
    return createGeometryWriter(recordWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out, final Charset charset) {
    RecordDefinition recordDefinition = RecordUtil.createGeometryMetaData();
    final Writer<Record> recordWriter = createRecordWriter(
      baseName, recordDefinition, out, charset);
    return createGeometryWriter(recordWriter);
  }

  public Writer<Geometry> createGeometryWriter(
    final Writer<Record> recordWriter) {
    final Writer<Geometry> geometryWriter = new RecordWriterGeometryWriter(
      recordWriter);
    return geometryWriter;
  }
}
