package com.revolsys.gis.data.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;
import com.revolsys.jts.geom.Geometry;

public abstract class AbstractDataObjectAndGeometryIoFactory extends
  AbstractDataObjectAndGeometryReaderFactory implements
  DataObjectWriterFactory, GeometryWriterFactory {

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractDataObjectAndGeometryIoFactory(final String name,
    final boolean binary, final boolean customAttributionSupported) {
    super(name, binary);
    setCustomAttributionSupported(customAttributionSupported);
  }

  /**
   * Create a writer to write to the specified resource.
   * 
   * @param metaData The metaData for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  @Override
  public Writer<DataObject> createDataObjectWriter(
    final DataObjectMetaData metaData, final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createDataObjectWriter(baseName, metaData, out);
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream) {
    return createDataObjectWriter(baseName, metaData, outputStream,
      FileUtil.UTF8);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final Resource resource) {
    final DataObjectMetaData metaData = DataObjectUtil.createGeometryMetaData();
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      metaData, resource);
    return createGeometryWriter(dataObjectWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out) {
    final DataObjectMetaData metaData = DataObjectUtil.createGeometryMetaData();
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      baseName, metaData, out);
    return createGeometryWriter(dataObjectWriter);
  }

  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out, final Charset charset) {
    final DataObjectMetaData metaData = DataObjectUtil.createGeometryMetaData();
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      baseName, metaData, out, charset);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(
    final Writer<DataObject> dataObjectWriter) {
    final Writer<Geometry> geometryWriter = new DataObjectWriterGeometryWriter(
      dataObjectWriter);
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
