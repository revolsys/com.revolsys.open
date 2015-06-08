package com.revolsys.format.directory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordIo;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Path;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.Property;

public class DirectoryRecordWriter extends AbstractRecordWriter {
  private File directory;

  private Map<String, Writer<Record>> writers = new HashMap<>();

  private Map<String, RecordDefinition> recordDefinitionMap = new HashMap<>();

  private boolean useNamespaceAsSubDirectory;

  private Statistics statistics;

  private String nameSuffix = "";

  private String fileExtension = "";

  public DirectoryRecordWriter() {
  }

  public DirectoryRecordWriter(final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  @PreDestroy
  public void close() {
    if (this.writers != null) {
      for (final Writer<Record> writer : this.writers.values()) {
        try {
          writer.close();
        } catch (final RuntimeException e) {
          LoggerFactory.getLogger(getClass()).error("Error closing " + writer, e);
        }
      }

      this.writers = null;
      this.recordDefinitionMap = null;
    }
    if (this.statistics != null) {
      this.statistics.disconnect();
      this.statistics = null;
    }
  }

  @Override
  public void flush() {
    for (final Writer<Record> writer : this.writers.values()) {
      try {
        writer.flush();
      } catch (final RuntimeException e) {
        LoggerFactory.getLogger(getClass()).error("Error flusing " + writer, e);
      }
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  private File getDirectory(final RecordDefinition recordDefinition) {
    if (this.useNamespaceAsSubDirectory) {
      final String typePath = recordDefinition.getPath();
      final String schemaName = Path.getPath(typePath);
      if (Property.hasValue(schemaName)) {
        final File childDirectory = new File(this.directory, schemaName);
        if (!childDirectory.mkdirs()) {
          if (!childDirectory.isDirectory()) {
            throw new IllegalArgumentException("Unable to create directory " + childDirectory);
          }
        }
        return childDirectory;
      }
    }
    return this.directory;
  }

  public String getFileExtension() {
    return this.fileExtension;
  }

  private String getFileName(final RecordDefinition recordDefinition) {
    return recordDefinition.getName();
  }

  public String getNameSuffix() {
    return this.nameSuffix;
  }

  public RecordDefinition getRecordDefinition(final String path) {
    return this.recordDefinitionMap.get(path);
  }

  public Statistics getStatistics() {
    return this.statistics;
  }

  private Writer<Record> getWriter(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final String path = recordDefinition.getPath();
    Writer<Record> writer = this.writers.get(path);
    if (writer == null) {
      final File directory = getDirectory(recordDefinition);
      directory.mkdirs();
      final String fileName = getFileName(recordDefinition);
      final File file = new File(directory, fileName + this.nameSuffix + "." + this.fileExtension);
      final FileSystemResource resource = new FileSystemResource(file);
      writer = RecordIo.recordWriter(recordDefinition, resource);
      if (writer == null) {
        throw new IllegalArgumentException("Unable to create writer for " + resource);
      } else {
        final Map<String, Object> properties = getProperties();
        Property.set(writer, properties);
        final Geometry geometry = record.getGeometryValue();
        if (geometry != null) {
          final GeometryFactory geometryFactory = geometry.getGeometryFactory();
          setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
        }
        this.writers.put(path, writer);
        RecordDefinition writerRecordDefinition = recordDefinition;
        if (writer instanceof AbstractRecordWriter) {
          final AbstractRecordWriter recordWriter = (AbstractRecordWriter)writer;
          writerRecordDefinition = recordWriter.getRecordDefinition();
          if (writerRecordDefinition == null) {
            writerRecordDefinition = recordDefinition;
          }
        }
        this.recordDefinitionMap.put(path, writerRecordDefinition);
      }
    }
    return writer;
  }

  public boolean isUseNamespaceAsSubDirectory() {
    return this.useNamespaceAsSubDirectory;
  }

  public void setDirectory(final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
    this.statistics = new Statistics("Write " + baseDirectory.getAbsolutePath());
    this.statistics.connect();
  }

  public void setFileExtension(final String fileExtension) {
    this.fileExtension = fileExtension;
  }

  public void setLogCounts(final boolean logCounts) {
    this.statistics.setLogCounts(false);
  }

  public void setNameSuffix(final String nameSuffix) {
    this.nameSuffix = nameSuffix;
  }

  public void setStatistics(final Statistics statistics) {
    if (this.statistics != statistics) {
      this.statistics = statistics;
      statistics.connect();
    }
  }

  public void setUseNamespaceAsSubDirectory(final boolean useNamespaceAsSubDirectory) {
    this.useNamespaceAsSubDirectory = useNamespaceAsSubDirectory;
  }

  @Override
  public String toString() {
    return this.directory.getAbsolutePath();
  }

  @Override
  public void write(final Record record) {
    if (record != null) {
      final Writer<Record> writer = getWriter(record);
      writer.write(record);
      this.statistics.add(record);
    }
  }
}
