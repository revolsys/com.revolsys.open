package com.revolsys.gis.data.io;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public abstract class AbstractDataObjectWriterFactory extends AbstractIoFactory
  implements DataObjectWriterFactory {
  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractDataObjectWriterFactory(
    String name) {
    super(name);
  }

  public Set<CoordinateSystem> getCoordinateSystems() {
    return coordinateSystems;
  }

  protected void setCoordinateSystems(
    Set<CoordinateSystem> coordinateSystems) {
    this.coordinateSystems = coordinateSystems;
  }

  public boolean isCoordinateSystemSupported(
    CoordinateSystem coordinateSystem) {
    return coordinateSystems.contains(coordinateSystem);
  }

  /**
   * Create a writer to write to the specified resource.
   * 
   * @param metaData The metaData for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  public Writer<DataObject> createDataObjectWriter(
    DataObjectMetaData metaData,
    final Resource resource) {
    try {
      final OutputStream out = SpringUtil.getOutputStream(resource);
      final String fileName = resource.getFilename();
      final String baseName = FileUtil.getBaseName(fileName);
      return createDataObjectWriter(baseName, metaData, out);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Error opening resource " + resource,
        e);
    }
  }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream) {
    return createDataObjectWriter(baseName, metaData, outputStream,
      Charset.defaultCharset());
  }

  protected void setCoordinateSystems(
    CoordinateSystem... coordinateSystems) {
    setCoordinateSystems(new LinkedHashSet<CoordinateSystem>(
      Arrays.asList(coordinateSystems)));
  }
}
