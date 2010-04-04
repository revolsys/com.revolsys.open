package com.revolsys.gis.data.io;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectAndGeometryWriterFactory extends
  AbstractDataObjectWriterFactory implements GeometryWriterFactory {

  public AbstractDataObjectAndGeometryWriterFactory(
    final String name) {
    super(name);
  }

  public Writer<Geometry> createGeometryWriter(
    final File file) {
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      DataObjectWriterGeometryWriter.META_DATA, file);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(
    String baseName,
    final OutputStream out) {
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      baseName, DataObjectWriterGeometryWriter.META_DATA, out);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(
    String baseName,
    final OutputStream out,
    final Charset charset) {
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      baseName, DataObjectWriterGeometryWriter.META_DATA, out, charset);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(
    final Writer<DataObject> dataObjectWriter) {
    final Writer<Geometry> geometryWriter = new DataObjectWriterGeometryWriter(
      dataObjectWriter);
    return geometryWriter;
  }
}
