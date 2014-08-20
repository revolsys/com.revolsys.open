package com.revolsys.io.directory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.io.AbstractRecordStore;
import com.revolsys.data.io.RecordIoFactories;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreSchema;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.io.FileUtil;
import com.revolsys.io.ObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class DirectoryRecordStore extends AbstractRecordStore {

  private boolean createMissingTables = true;

  private final Map<String, Writer<Record>> writers = new HashMap<>();

  private File directory;

  private List<String> fileExtensions;

  private Writer<Record> writer;

  private boolean createMissingRecordStore = true;

  private final Map<RecordDefinition, File> filesByRecordDefinition = new HashMap<>();

  private final Map<File, String> typePathByFile = new HashMap<>();

  public DirectoryRecordStore(final File directory,
    final Collection<String> fileExtensions) {
    this.directory = directory;
    this.fileExtensions = new ArrayList<>(fileExtensions);
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
  public AbstractIterator<Record> createIterator(final Query query,
    final Map<String, Object> properties) {
    // TODO Auto-generated method stub
    return super.createIterator(query, properties);
  }

  @Override
  public Writer<Record> createWriter() {
    return new DirectoryRecordStoreWriter(this);
  }

  @Override
  public void delete(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    if (recordStore == this) {
      throw new UnsupportedOperationException("Deleting records not supported");
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  protected File getFile(final String path) {
    final RecordDefinition recordDefinition = getRecordDefinition(path);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Table does not exist " + path);
    }
    final File file = this.filesByRecordDefinition.get(recordDefinition);
    if (file == null) {
      throw new IllegalArgumentException("File does not exist for " + path);
    }
    return file;
  }

  public String getFileExtension() {
    return getFileExtensions().get(0);
  }

  public List<String> getFileExtensions() {
    return this.fileExtensions;
  }

  @Override
  public RecordDefinition getRecordDefinition(
    final RecordDefinition recordDefinition) {
    final RecordDefinition storeRecordDefinition = super.getRecordDefinition(recordDefinition);
    if (storeRecordDefinition == null && this.createMissingTables) {
      final String typePath = recordDefinition.getPath();
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
      final RecordDefinitionImpl newRecordDefinition = new RecordDefinitionImpl(
        this, schema, typePath);
      for (final Attribute attribute : recordDefinition.getAttributes()) {
        final Attribute newAttribute = new Attribute(attribute);
        newRecordDefinition.addAttribute(newAttribute);
      }
      schema.addRecordDefinition(newRecordDefinition);
    }
    return storeRecordDefinition;
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
  public synchronized void insert(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    Writer<Record> writer = this.writers.get(typePath);
    if (writer == null) {
      final String schemaName = PathUtil.getPath(typePath);
      final File subDirectory = new File(getDirectory(), schemaName);
      final String fileExtension = getFileExtension();
      final File file = new File(subDirectory, recordDefinition.getTypeName()
        + "." + fileExtension);
      final Resource resource = new FileSystemResource(file);
      writer = RecordIoFactories.recordWriter(recordDefinition, resource);
      if (writer instanceof ObjectWithProperties) {
        final ObjectWithProperties properties = writer;
        properties.setProperties(getProperties());
      }
      this.writers.put(typePath, writer);
    }
    writer.write(record);
    addStatistic("Insert", record);
  }

  public boolean isCreateMissingRecordStore() {
    return this.createMissingRecordStore;
  }

  public boolean isCreateMissingTables() {
    return this.createMissingTables;
  }

  protected RecordDefinition loadRecordDefinition(final String schemaName,
    final File file) {
    try (
      RecordReader recordReader = RecordIoFactories.recordReader(file)) {
      final String typePath = schemaName + "/" + FileUtil.getBaseName(file);
      recordReader.setProperty("typePath", typePath);
      final RecordDefinition recordDefinition = recordReader.getRecordDefinition();
      if (recordDefinition != null) {
        this.filesByRecordDefinition.put(recordDefinition, file);
        this.typePathByFile.put(file, typePath);
      }
      return recordDefinition;
    }
  }

  @Override
  protected void loadSchemaRecordDefinitions(final RecordStoreSchema schema,
    final Map<String, RecordDefinition> recordDefinitionMap) {
    final String schemaName = schema.getPath();
    final File subDirectory = new File(this.directory, schemaName);
    final FilenameFilter filter = new ExtensionFilenameFilter(
      this.fileExtensions);
    final File[] files = subDirectory.listFiles(filter);
    if (files != null) {
      for (final File file : files) {
        final RecordDefinition recordDefinition = loadRecordDefinition(
          schemaName, file);
        if (recordDefinition != null) {
          final String typePath = recordDefinition.getPath();
          recordDefinitionMap.put(typePath, recordDefinition);
        }
      }
    }
  }

  @Override
  protected void loadSchemas(final Map<String, RecordStoreSchema> schemaMap) {
    final FilenameFilter filter = new DirectoryFilenameFilter();
    final File[] directories = this.directory.listFiles(filter);
    if (directories != null) {
      for (final File subDirectory : directories) {
        final String directoryName = FileUtil.getFileName(subDirectory);
        final RecordStoreSchema schema = new RecordStoreSchema(this, "/"
          + directoryName);
        addSchema(schema);
      }
    }
  }

  @Override
  public Reader<Record> query(final String path) {
    final File file = getFile(path);
    final RecordReader reader = RecordIoFactories.recordReader(file);
    if (reader != null) {
      final String typePath = this.typePathByFile.get(file);
      reader.setProperty("typePath", typePath);
    }
    return reader;
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

  protected void setFileExtensions(final List<String> fileExtensions) {
    this.fileExtensions = fileExtensions;
  }

  protected void superDelete(final Record record) {
    super.delete(record);
  }

  protected void superUpdate(final Record record) {
    super.update(record);
  }

  @Override
  public String toString() {
    final String fileExtension = getFileExtension();
    return fileExtension + " " + this.directory;
  }

  @Override
  public void update(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    if (recordStore == this) {
      switch (record.getState()) {
        case Deleted:
        break;
        case Persisted:
        break;
        case Modified:
          throw new UnsupportedOperationException();
        default:
          insert(record);
        break;
      }
    } else {
      insert(record);
    }
  }
}
