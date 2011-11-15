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
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectAndGeometryIoFactory extends
  AbstractDataObjectAndGeometryReaderFactory implements
  DataObjectWriterFactory, GeometryWriterFactory {

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractDataObjectAndGeometryIoFactory(final String name,
    boolean binary, boolean customAttributionSupported) {
    super(name, binary);
    this.customAttributionSupported = customAttributionSupported;
  }

  public boolean isGeometrySupported() {
    return true;
  }

  private boolean customAttributionSupported;

  public boolean isCustomAttributionSupported() {
    return customAttributionSupported;
  }

  private boolean singleFile = true;
  
  
  public boolean isSingleFile() {
    return singleFile;
  }

  protected void setSingleFile(boolean singleFile) {
    this.singleFile = singleFile;
  }

  /**
   * Create a writer to write to the specified resource.
   * 
   * @param metaData The metaData for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  public Writer<DataObject> createDataObjectWriter(DataObjectMetaData metaData,
    final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createDataObjectWriter(baseName, metaData, out);
  }

  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream) {
    return createDataObjectWriter(baseName, metaData, outputStream,
      Charset.defaultCharset());
  }

  public Writer<Geometry> createGeometryWriter(final Resource resource) {
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      DataObjectUtil.GEOMETRY_META_DATA, resource);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out) {
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      baseName, DataObjectUtil.GEOMETRY_META_DATA, out);
    return createGeometryWriter(dataObjectWriter);
  }

  public Writer<Geometry> createGeometryWriter(final String baseName,
    final OutputStream out, final Charset charset) {
    final Writer<DataObject> dataObjectWriter = createDataObjectWriter(
      baseName, DataObjectUtil.GEOMETRY_META_DATA, out, charset);
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
