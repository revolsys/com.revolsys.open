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
    RecordDefinition metaData = RecordUtil.createGeometryMetaData();
    final Writer<Record> dataObjectWriter = createRecordWriter(
      metaData, resource);
    return createGeometryWriter(dataObjectWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out) {
    RecordDefinition metaData = RecordUtil.createGeometryMetaData();
    final Writer<Record> dataObjectWriter = createRecordWriter(
      baseName, metaData, out);
    return createGeometryWriter(dataObjectWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out, final Charset charset) {
    RecordDefinition metaData = RecordUtil.createGeometryMetaData();
    final Writer<Record> dataObjectWriter = createRecordWriter(
      baseName, metaData, out, charset);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(
    final Writer<Record> dataObjectWriter) {
    final Writer<Geometry> geometryWriter = new RecordWriterGeometryWriter(
      dataObjectWriter);
    return geometryWriter;
  }
}
