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
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public abstract class AbstractDataObjectWriterFactory extends AbstractIoFactory
  implements DataObjectWriterFactory {

  public static Writer<DataObject> dataObjectWriter(
    DataObjectMetaData metaData, final Resource resource) {
    final DataObjectWriterFactory writerFactory = getDataObjectWriterFactory(resource);
    if (writerFactory == null) {
      return null;
    } else {
      final Writer<DataObject> writer = writerFactory.createDataObjectWriter(
        metaData, resource);
      return writer;
    }
  }

  protected static DataObjectWriterFactory getDataObjectWriterFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;
    final DataObjectWriterFactory writerFactory = ioFactoryRegistry.getFactoryByResource(
      DataObjectWriterFactory.class, resource);
    return writerFactory;
  }

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  private boolean geometrySupported;

  private boolean customAttributionSupported;

  public AbstractDataObjectWriterFactory(String name,
    boolean geometrySupported, boolean customAttributionSupported) {
    super(name);
    this.geometrySupported = geometrySupported;
    this.customAttributionSupported = customAttributionSupported;
  }

  public boolean isCustomAttributionSupported() {
    return customAttributionSupported;
  }

  public boolean isGeometrySupported() {
    return geometrySupported;
  }

  public Set<CoordinateSystem> getCoordinateSystems() {
    return coordinateSystems;
  }

  protected void setCoordinateSystems(Set<CoordinateSystem> coordinateSystems) {
    this.coordinateSystems = coordinateSystems;
  }

  public boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem) {
    return coordinateSystems.contains(coordinateSystem);
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

  public Writer<DataObject> createDataObjectWriter(String baseName,
    DataObjectMetaData metaData, OutputStream outputStream) {
    return createDataObjectWriter(baseName, metaData, outputStream,
      Charset.defaultCharset());
  }

  protected void setCoordinateSystems(CoordinateSystem... coordinateSystems) {
    setCoordinateSystems(new LinkedHashSet<CoordinateSystem>(
      Arrays.asList(coordinateSystems)));
  }
}
