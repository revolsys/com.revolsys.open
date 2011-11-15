package com.revolsys.io.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.vividsolutions.jts.geom.Geometry;

public class CsvDirectoryWriter extends AbstractWriter<DataObject> {
  private File directory;

  private final Map<DataObjectMetaData, CsvDataObjectWriter> writers = new HashMap<DataObjectMetaData, CsvDataObjectWriter>();

  public CsvDirectoryWriter() {
  }

  public CsvDirectoryWriter(
    final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  public void close() {
    for (final CsvDataObjectWriter writer : writers.values()) {
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
    for (CsvDataObjectWriter writer : writers.values()) {
      writer.flush();
    }
  }

  public String toString() {
    return directory.getAbsolutePath();
  }

  private CsvDataObjectWriter getWriter(
    final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    CsvDataObjectWriter writer = writers.get(metaData);
    if (writer == null) {
      try {

        final File file = new File(directory, metaData.getName().toString()
          + ".csv");
        writer = new CsvDataObjectWriter(metaData, new FileWriter(file));
        final Geometry geometry = object.getGeometryValue();
        if (geometry != null) {
          writer.setProperty(IoConstants.GEOMETRY_FACTORY, GeometryFactory.getFactory(geometry));
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

    final CsvDataObjectWriter writer = getWriter(object);
    writer.write(object);
  }

}
