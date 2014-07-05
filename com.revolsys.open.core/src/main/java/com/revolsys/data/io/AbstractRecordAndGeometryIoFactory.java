package com.revolsys.data.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.spring.SpringUtil;

public abstract class AbstractRecordAndGeometryIoFactory extends
  AbstractRecordAndGeometryReaderFactory implements
  RecordWriterFactory, GeometryWriterFactory {

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractRecordAndGeometryIoFactory(final String name,
    final boolean binary, final boolean customAttributionSupported) {
    super(name, binary);
    setCustomAttributionSupported(customAttributionSupported);
  }

  /**
   * Create a writer to write to the specified resource.
   * 
   * @param recordDefinition The recordDefinition for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  @Override
  public Writer<Record> createRecordWriter(
    final RecordDefinition recordDefinition, final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createRecordWriter(baseName, recordDefinition, out);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream) {
    return createRecordWriter(baseName, recordDefinition, outputStream,
      FileUtil.UTF8);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final Resource resource) {
    final RecordDefinition recordDefinition = RecordUtil.createGeometryMetaData();
    final Writer<Record> recordWriter = createRecordWriter(
      recordDefinition, resource);
    return createGeometryWriter(recordWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out) {
    final RecordDefinition recordDefinition = RecordUtil.createGeometryMetaData();
    final Writer<Record> recordWriter = createRecordWriter(
      baseName, recordDefinition, out);
    return createGeometryWriter(recordWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out, final Charset charset) {
    final RecordDefinition recordDefinition = RecordUtil.createGeometryMetaData();
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

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return coordinateSystems;
  }

  @Override
  public boolean isCoordinateSystemSupported(
    final CoordinateSystem coordinateSystem) {
    return coordinateSystems.contains(coordinateSystem);
  }

  @Override
  public boolean isGeometrySupported() {
    return true;
  }

  @Override
  protected void setCoordinateSystems(
    final CoordinateSystem... coordinateSystems) {
    setCoordinateSystems(new LinkedHashSet<CoordinateSystem>(
      Arrays.asList(coordinateSystems)));
  }

  @Override
  protected void setCoordinateSystems(
    final Set<CoordinateSystem> coordinateSystems) {
    this.coordinateSystems = coordinateSystems;
  }
}
