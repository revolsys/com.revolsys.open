package com.revolsys.record.io.format.directory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.util.Property;
import com.revolsys.util.count.LabelCountMap;

public class DirectoryRecordWriter extends AbstractRecordWriter {
  private File directory;

  private String fileExtension = "";

  private String nameSuffix = "";

  private Map<String, RecordDefinition> recordDefinitionMap = new HashMap<>();

  private LabelCountMap labelCountMap;

  private boolean useNamespaceAsSubDirectory;

  private Map<String, Writer<Record>> writers = new HashMap<>();

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
          Logs.error(this, "Error closing " + writer, e);
        }
      }

      this.writers = null;
      this.recordDefinitionMap = null;
    }
    if (this.labelCountMap != null) {
      this.labelCountMap.disconnect();
      this.labelCountMap = null;
    }
  }

  @Override
  public void flush() {
    for (final Writer<Record> writer : this.writers.values()) {
      try {
        writer.flush();
      } catch (final RuntimeException e) {
        Logs.error(this, "Error flusing " + writer, e);
      }
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  private File getDirectory(final RecordDefinition recordDefinition) {
    if (this.useNamespaceAsSubDirectory) {
      final String typePath = recordDefinition.getPath();
      final String schemaName = PathUtil.getPath(typePath);
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

  public LabelCountMap getStatistics() {
    return this.labelCountMap;
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
      writer = RecordWriter.newRecordWriter(recordDefinition, resource);
      if (writer == null) {
        throw new IllegalArgumentException("Unable to create writer for " + resource);
      } else {
        final Map<String, Object> properties = getProperties();
        writer.setProperties(properties);
        final Geometry geometry = record.getGeometry();
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
    this.labelCountMap = new LabelCountMap("Write " + baseDirectory.getAbsolutePath());
    this.labelCountMap.connect();
  }

  public void setFileExtension(final String fileExtension) {
    this.fileExtension = fileExtension;
  }

  public void setLogCounts(final boolean logCounts) {
    this.labelCountMap.setLogCounts(false);
  }

  public void setNameSuffix(final String nameSuffix) {
    this.nameSuffix = nameSuffix;
  }

  public void setStatistics(final LabelCountMap labelCountMap) {
    if (this.labelCountMap != labelCountMap) {
      this.labelCountMap = labelCountMap;
      labelCountMap.connect();
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
      this.labelCountMap.addCount(record);
    }
  }
}
