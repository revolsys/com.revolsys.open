package com.revolsys.data.io;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public abstract class AbstractRecordIoFactory extends
AbstractRecordReaderFactory implements RecordWriterFactory {

  protected static RecordWriterFactory getRecordWriterFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final RecordWriterFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      RecordWriterFactory.class, resource);
    return readerFactory;
  }

  public static Writer<Record> recordWriter(final RecordDefinition metaData,
    final Resource resource) {
    final RecordWriterFactory writerFactory = getRecordWriterFactory(resource);
    if (writerFactory == null) {
      return null;
    } else {
      final Writer<Record> writer = writerFactory.createRecordWriter(metaData,
        resource);
      return writer;
    }
  }

  private final boolean geometrySupported;

  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractRecordIoFactory(final String name, final boolean binary,
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
  @Override
  public Writer<Record> createRecordWriter(final RecordDefinition metaData,
    final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createRecordWriter(baseName, metaData, out);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream) {
    return createRecordWriter(baseName, metaData, outputStream,
      FileUtil.UTF8);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return this.coordinateSystems;
  }

  @Override
  public boolean isCoordinateSystemSupported(
    final CoordinateSystem coordinateSystem) {
    return this.coordinateSystems.contains(coordinateSystem);
  }

  @Override
  public boolean isGeometrySupported() {
    return this.geometrySupported;
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
