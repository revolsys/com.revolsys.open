package com.revolsys.gis.data.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectAndGeometryIoFactory extends
  AbstractDataObjectAndGeometryReaderFactory implements DataObjectWriterFactory {

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractDataObjectAndGeometryIoFactory(
    final String name) {
    super(name);
  }

  public Writer<DataObject> createDataObjectWriter(
    final DataObjectMetaData metaData,
    final File file) {
    try {
      final FileOutputStream out = new FileOutputStream(file);
      final String baseName = FileUtil.getBaseName(file);
      return createDataObjectWriter(baseName, metaData, out);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Error writing to file:" + file, e);
    }
  }

  public Writer<DataObject> createDataObjectWriter(
    final String baseName,
    final DataObjectMetaData metaData,
    final OutputStream outputStream) {
    return createDataObjectWriter(baseName, metaData, outputStream,
      Charset.defaultCharset());
  }

  public Writer<Geometry> createGeometryWriter(
    final File file) {
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      DataObjectWriterGeometryWriter.META_DATA, file);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(
    final String baseName,
    final OutputStream out) {
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      baseName, DataObjectWriterGeometryWriter.META_DATA, out);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(
    final String baseName,
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

  public Set<CoordinateSystem> getCoordinateSystems() {
    return coordinateSystems;
  }

  public boolean isCoordinateSystemSupported(
    final CoordinateSystem coordinateSystem) {
    return coordinateSystems.contains(coordinateSystem);
  }

  protected void setCoordinateSystems(
    final CoordinateSystem... coordinateSystems) {
    setCoordinateSystems(new LinkedHashSet<CoordinateSystem>(
      Arrays.asList(coordinateSystems)));
  }

  protected void setCoordinateSystems(
    final Set<CoordinateSystem> coordinateSystems) {
    this.coordinateSystems = coordinateSystems;
  }
}
