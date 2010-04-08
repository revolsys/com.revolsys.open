package com.revolsys.gis.format.shape.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.vividsolutions.jts.geom.Geometry;

public class ShapeDirectoryWriter extends AbstractWriter<DataObject> {
  private File directory;

  private boolean useZeroForNull = true;

  private final Map<DataObjectMetaData, ShapeFileWriter> writers = new HashMap<DataObjectMetaData, ShapeFileWriter>();

  public ShapeDirectoryWriter() {
  }

  public ShapeDirectoryWriter(
    final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  public void close() {
    for (final ShapeFileWriter writer : writers.values()) {
      try {
        writer.close();
      } catch (final RuntimeException e) {
        e.printStackTrace();
      }
    }
  }

  public File getDirectory() {
    return directory;
  }

  public void flush() {
    // TODO Auto-generated method stub

  }

  public String toString() {
    return directory.getAbsolutePath();
  }

  private ShapeFileWriter getWriter(
    final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    ShapeFileWriter writer = writers.get(metaData);
    if (writer == null) {
      try {

        writer = new ShapeFileWriter(new File(directory, metaData.getName()
          .getLocalPart()
          + ".shp"), metaData);
        writer.setUseZeroForNull(useZeroForNull);
        final Geometry geometry = object.getGeometryValue();
        if (geometry != null) {
          writer.setProperty("srid", geometry.getSRID());
        }
        writers.put(metaData, writer);
      } catch (final IOException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
    return writer;
  }

  public boolean isUseZeroForNull() {
    return useZeroForNull;
  }

  public void setDirectory(
    final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
  }

  public void setUseZeroForNull(
    final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  public void write(
    final DataObject object) {

    final ShapeFileWriter writer = getWriter(object);
    writer.write(object);
  }

}
