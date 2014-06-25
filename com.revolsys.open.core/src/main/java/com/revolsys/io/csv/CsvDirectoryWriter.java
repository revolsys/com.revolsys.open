package com.revolsys.io.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;

public class CsvDirectoryWriter extends AbstractWriter<DataObject> {
  private File directory;

  private final Map<DataObjectMetaData, CsvDataObjectWriter> writers = new HashMap<DataObjectMetaData, CsvDataObjectWriter>();

  private final Map<String, DataObjectMetaData> metaDataMap = new HashMap<>();

  public CsvDirectoryWriter() {
  }

  public CsvDirectoryWriter(final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  public void close() {
    for (final CsvDataObjectWriter writer : writers.values()) {
      FileUtil.closeSilent(writer);
    }
    writers.clear();
    metaDataMap.clear();
  }

  @Override
  public void flush() {
    for (final CsvDataObjectWriter writer : writers.values()) {
      writer.flush();
    }
  }

  public File getDirectory() {
    return directory;
  }

  public DataObjectMetaData getMetaData(final String path) {
    return metaDataMap.get(path);
  }

  private CsvDataObjectWriter getWriter(final DataObject record) {
    final DataObjectMetaData metaData = record.getMetaData();
    CsvDataObjectWriter writer = writers.get(metaData);
    if (writer == null) {
      try {
        final String path = metaData.getPath();
        final File file = new File(directory, path.toString() + ".csv");
        writer = new CsvDataObjectWriter(metaData, new FileWriter(file));
        final Geometry geometry = record.getGeometryValue();
        if (geometry != null) {
          writer.setProperty(IoConstants.GEOMETRY_FACTORY,
            geometry.getGeometryFactory());
        }
        writers.put(metaData, writer);
        metaDataMap.put(path, metaData);
      } catch (final IOException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
    return writer;
  }

  public void setDirectory(final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
  }

  @Override
  public String toString() {
    return directory.getAbsolutePath();
  }

  @Override
  public void write(final DataObject object) {
    final CsvDataObjectWriter writer = getWriter(object);
    writer.write(object);
  }

}
