package com.revolsys.io.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;

public class CsvDirectoryWriter extends AbstractRecordWriter {
  private File directory;

  private final Map<RecordDefinition, CsvRecordWriter> writers = new HashMap<RecordDefinition, CsvRecordWriter>();

  private final Map<String, RecordDefinition> recordDefinitionMap = new HashMap<>();

  public CsvDirectoryWriter() {
  }

  public CsvDirectoryWriter(final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  public void close() {
    for (final CsvRecordWriter writer : this.writers.values()) {
      FileUtil.closeSilent(writer);
    }
    this.writers.clear();
    this.recordDefinitionMap.clear();
  }

  @Override
  public void flush() {
    for (final CsvRecordWriter writer : this.writers.values()) {
      writer.flush();
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  public RecordDefinition getRecordDefinition(final String path) {
    return this.recordDefinitionMap.get(path);
  }

  private CsvRecordWriter getWriter(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    CsvRecordWriter writer = this.writers.get(recordDefinition);
    if (writer == null) {
      try {
        final String path = recordDefinition.getPath();
        final File file = new File(this.directory, path.toString() + ".csv");
        writer = new CsvRecordWriter(recordDefinition, new FileWriter(file));
        final Geometry geometry = record.getGeometryValue();
        if (geometry != null) {
          writer.setProperty(IoConstants.GEOMETRY_FACTORY,
            geometry.getGeometryFactory());
        }
        this.writers.put(recordDefinition, writer);
        this.recordDefinitionMap.put(path, recordDefinition);
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
    return this.directory.getAbsolutePath();
  }

  @Override
  public void write(final Record object) {
    final CsvRecordWriter writer = getWriter(object);
    writer.write(object);
  }

}
