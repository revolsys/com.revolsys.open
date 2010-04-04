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
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

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

  public Writer<DataObject> createDataObjectWriter(
    DataObjectMetaData metaData,
    File file) {
    try {
      final FileOutputStream out = new FileOutputStream(file);
      String baseName = FileUtil.getBaseName(file);
      return createDataObjectWriter(baseName, metaData, out);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error writing to file:" + file, e);
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
