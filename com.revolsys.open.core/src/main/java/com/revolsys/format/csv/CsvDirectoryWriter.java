package com.revolsys.format.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.format.vrt.OgrVrtWriter;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.GeometryFactory;

public class CsvDirectoryWriter extends AbstractRecordWriter {
  private File directory;

  private final Map<RecordDefinition, CsvRecordWriter> writers = new HashMap<RecordDefinition, CsvRecordWriter>();

  private final Map<String, RecordDefinition> recordDefinitionMap = new HashMap<>();

  private boolean ewkt = true;

  public CsvDirectoryWriter() {
  }

  public CsvDirectoryWriter(final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  public void close() {
    for (final CsvRecordWriter writer : this.writers.values()) {
      writer.flush();
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

  public synchronized RecordDefinition getRecordDefinition(
    final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    RecordDefinition storeRecordDefinition = getRecordDefinition(typePath);
    if (storeRecordDefinition == null) {
      storeRecordDefinition = recordDefinition;
      this.recordDefinitionMap.put(typePath, storeRecordDefinition);
      try {
        final String path = recordDefinition.getPath();
        final File file = new File(this.directory, path.toString() + ".csv");
        file.getParentFile().mkdirs();
        final CsvRecordWriter writer = new CsvRecordWriter(recordDefinition,
          new FileWriter(file), this.ewkt);
        final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
        writer.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
        this.writers.put(recordDefinition, writer);

        final File vrtFile = new File(this.directory, path.toString() + ".vrt");
        final String typeName = recordDefinition.getName();
        OgrVrtWriter.write(vrtFile, recordDefinition, typeName + ".csv");

        EsriCoordinateSystems.createPrjFile(new FileSystemResource(file),
          recordDefinition.getGeometryFactory());

      } catch (final IOException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
    return storeRecordDefinition;
  }

  public RecordDefinition getRecordDefinition(final String path) {
    return this.recordDefinitionMap.get(path);
  }

  private CsvRecordWriter getWriter(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return getWriter(recordDefinition);
  }

  public synchronized CsvRecordWriter getWriter(
    final RecordDefinition recordDefinition) {
    final RecordDefinition storeRecordDefinition = getRecordDefinition(recordDefinition);
    return this.writers.get(storeRecordDefinition);
  }

  public boolean isEwkt() {
    return this.ewkt;
  }

  public void setDirectory(final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
  }

  public void setEwkt(final boolean ewkt) {
    this.ewkt = ewkt;
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
