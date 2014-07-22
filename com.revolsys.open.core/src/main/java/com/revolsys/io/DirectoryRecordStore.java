package com.revolsys.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordStore;
import com.revolsys.data.io.RecordIoFactories;
import com.revolsys.data.io.RecordStoreSchema;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class DirectoryRecordStore extends AbstractRecordStore {

  private boolean createMissingTables = true;

  private final Map<String, Writer<Record>> writers = new HashMap<String, Writer<Record>>();

  private File directory;

  private String fileExtension;

  private Writer<Record> writer;

  private boolean createMissingRecordStore = true;

  public DirectoryRecordStore(final File directory, final String fileExtension) {
    this.directory = directory;
    this.fileExtension = fileExtension;
  }

  @Override
  public void close() {
    this.directory = null;
    if (this.writers != null) {
      for (final Writer<Record> writer : this.writers.values()) {
        writer.close();
      }
      this.writers.clear();
    }
    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
    super.close();
  }

  @Override
  public Writer<Record> createWriter() {
    return new DirectoryRecordStoreWriter(this);
  }

  public File getDirectory() {
    return this.directory;
  }

  public String getFileExtension() {
    return this.fileExtension;
  }

  @Override
  public RecordDefinition getRecordDefinition(
    final RecordDefinition objectMetaData) {
    final RecordDefinition recordDefinition = super.getRecordDefinition(objectMetaData);
    if (recordDefinition == null && this.createMissingTables) {
      final String typePath = objectMetaData.getPath();
      final String schemaName = PathUtil.getPath(typePath);
      RecordStoreSchema schema = getSchema(schemaName);
      if (schema == null && this.createMissingTables) {
        schema = new RecordStoreSchema(this, schemaName);
        addSchema(schema);
      }
      final File schemaDirectory = new File(this.directory, schemaName);
      if (!schemaDirectory.exists()) {
        schemaDirectory.mkdirs();
      }
      final RecordDefinitionImpl newMetaData = new RecordDefinitionImpl(this,
        schema, typePath);
      for (final Attribute attribute : objectMetaData.getAttributes()) {
        final Attribute newAttribute = new Attribute(attribute);
        newMetaData.addAttribute(newAttribute);
      }
      schema.addMetaData(newMetaData);
    }
    return recordDefinition;
  }

  @Override
  public int getRowCount(final Query query) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized Writer<Record> getWriter() {
    if (this.writer == null && this.directory != null) {
      this.writer = new DirectoryRecordStoreWriter(this);
    }
    return this.writer;
  }

  @PostConstruct
  @Override
  public void initialize() {
    if (!this.directory.exists()) {
      this.directory.mkdirs();
    }
    super.initialize();
  }

  @Override
  public synchronized void insert(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    Writer<Record> writer = this.writers.get(typePath);
    if (writer == null) {
      final String schemaName = PathUtil.getPath(typePath);
      final File subDirectory = new File(getDirectory(), schemaName);
      final File file = new File(subDirectory, recordDefinition.getTypeName()
        + "." + getFileExtension());
      final Resource resource = new FileSystemResource(file);
      writer = RecordIoFactories.recordWriter(recordDefinition, resource);
      if (writer instanceof ObjectWithProperties) {
        final ObjectWithProperties properties = writer;
        properties.setProperties(getProperties());
      }
      this.writers.put(typePath, writer);
    }
    writer.write(object);
    addStatistic("Insert", object);
  }

  public boolean isCreateMissingRecordStore() {
    return this.createMissingRecordStore;
  }

  public boolean isCreateMissingTables() {
    return this.createMissingTables;
  }

  protected RecordDefinition loadMetaData(final String schemaName,
    final File file) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void loadSchemaRecordDefinitions(final RecordStoreSchema schema,
    final Map<String, RecordDefinition> recordDefinitionMap) {
    final String schemaName = schema.getPath();
    final File subDirectory = new File(this.directory, schemaName);
    final File[] files = subDirectory.listFiles(new ExtensionFilenameFilter(
      this.fileExtension));
    if (files != null) {
      for (final File file : files) {
        final RecordDefinition recordDefinition = loadMetaData(schemaName, file);
        if (recordDefinition != null) {
          final String typePath = recordDefinition.getPath();
          recordDefinitionMap.put(typePath, recordDefinition);
        }
      }
    }
  }

  @Override
  protected void loadSchemas(final Map<String, RecordStoreSchema> schemaMap) {
    final File[] directories = this.directory.listFiles(new DirectoryFilenameFilter());
    if (directories != null) {
      for (final File subDirectory : directories) {
        final String directoryName = FileUtil.getFileName(subDirectory);
        addSchema(new RecordStoreSchema(this, directoryName));
      }
    }
  }

  public void setCreateMissingRecordStore(final boolean createMissingRecordStore) {
    this.createMissingRecordStore = createMissingRecordStore;
  }

  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public void setDirectory(final File directory) {
    this.directory = directory;
  }

  protected void setFileExtension(final String fileExtension) {
    this.fileExtension = fileExtension;
  }
}
