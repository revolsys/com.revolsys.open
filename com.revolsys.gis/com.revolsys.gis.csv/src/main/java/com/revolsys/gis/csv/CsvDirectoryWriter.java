package com.revolsys.gis.csv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.vividsolutions.jts.geom.Geometry;

public class CsvDirectoryWriter extends AbstractWriter<DataObject> {
  private File directory;

  private final Map<DataObjectMetaData, CsvWriter> writers = new HashMap<DataObjectMetaData, CsvWriter>();

  public CsvDirectoryWriter() {
  }

  public CsvDirectoryWriter(
    final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  public void close() {
    for (final CsvWriter writer : writers.values()) {
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
    for (CsvWriter writer : writers.values()) {
      writer.flush();
    }
  }

  public String toString() {
    return directory.getAbsolutePath();
  }

  private CsvWriter getWriter(
    final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    CsvWriter writer = writers.get(metaData);
    if (writer == null) {
      try {

        final File file = new File(directory, metaData.getName().toString()
          + ".csv");
        writer = new CsvWriter(metaData, new FileOutputStream(file));
        final Geometry geometry = object.getGeometryValue();
        if (geometry != null) {
          writer.setProperty(IoConstants.SRID_PROPERTY, geometry.getSRID());
        }
        writers.put(metaData, writer);
      } catch (final IOException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
    return writer;
  }

  public void setDirectory(
    final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
  }


  public void write(
    final DataObject object) {

    final CsvWriter writer = getWriter(object);
    writer.write(object);
  }

}
