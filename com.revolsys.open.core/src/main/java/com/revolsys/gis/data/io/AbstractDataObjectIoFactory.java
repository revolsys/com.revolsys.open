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
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public abstract class AbstractDataObjectIoFactory extends
  AbstractDataObjectReaderFactory implements DataObjectWriterFactory {

  public static Writer<DataObject> dataObjectWriter(
    final DataObjectMetaData metaData,
    final Resource resource) {
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
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final DataObjectWriterFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      DataObjectWriterFactory.class, resource);
    return readerFactory;
  }

  private final boolean geometrySupported;

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractDataObjectIoFactory(final String name, final boolean binary,
    final boolean geometrySupported, final boolean customAttributionSupported) {
    super(name, binary);
    this.geometrySupported = geometrySupported;
    setCustomAttributionSupported(customAttributionSupported);
  }

  /**
   * Create a writer to write to the specified resource.
   * 
   * @param metaData The metaData for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  public Writer<DataObject> createDataObjectWriter(
    final DataObjectMetaData metaData,
    final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createDataObjectWriter(baseName, metaData, out);
  }

  public Writer<DataObject> createDataObjectWriter(
    final String baseName,
    final DataObjectMetaData metaData,
    final OutputStream outputStream) {
    return createDataObjectWriter(baseName, metaData, outputStream,
      Charset.defaultCharset());
  }

  public Set<CoordinateSystem> getCoordinateSystems() {
    return coordinateSystems;
  }

  public boolean isCoordinateSystemSupported(
    final CoordinateSystem coordinateSystem) {
    return coordinateSystems.contains(coordinateSystem);
  }

  public boolean isGeometrySupported() {
    return geometrySupported;
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
