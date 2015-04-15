package com.revolsys.data.io;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public abstract class AbstractRecordIoFactory extends
AbstractRecordReaderFactory implements RecordWriterFactory {

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
      StandardCharsets.UTF_8);
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
