package com.revolsys.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectIoFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class DirectoryDataObjectStore extends AbstractDataObjectStore {

  private boolean createMissingTables = true;

  private final Map<String, Writer<DataObject>> writers = new HashMap<String, Writer<DataObject>>();

  private File directory;

  private String fileExtension;

  private Writer<DataObject> writer;

  private boolean createMissingDataStore = true;

  public DirectoryDataObjectStore(final File directory,
    final String fileExtension) {
    this.directory = directory;
    this.fileExtension = fileExtension;
  }

  @Override
  public void close() {
    directory = null;
    if (writers != null) {
      for (final Writer<DataObject> writer : writers.values()) {
        writer.close();
      }
      writers.clear();
    }
    if (writer != null) {
      writer.close();
      writer = null;
    }
    super.close();
  }

  @Override
  public Writer<DataObject> createWriter() {
    return new DirectoryDataObjectStoreWriter(this);
  }

  public File getDirectory() {
    return directory;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  @Override
  public DataObjectMetaData getMetaData(final DataObjectMetaData objectMetaData) {
    final DataObjectMetaData metaData = super.getMetaData(objectMetaData);
    if (metaData == null && createMissingTables) {
      final String typePath = objectMetaData.getPath();
      final String schemaName = PathUtil.getPath(typePath);
      DataObjectStoreSchema schema = getSchema(schemaName);
      if (schema == null && createMissingTables) {
        schema = new DataObjectStoreSchema(this, schemaName);
        addSchema(schema);
      }
      final File schemaDirectory = new File(directory, schemaName);
      if (!schemaDirectory.exists()) {
        schemaDirectory.mkdirs();
      }
      final DataObjectMetaDataImpl newMetaData = new DataObjectMetaDataImpl(
        this, schema, typePath);
      for (final Attribute attribute : objectMetaData.getAttributes()) {
        final Attribute newAttribute = new Attribute(attribute);
        newMetaData.addAttribute(newAttribute);
      }
      schema.addMetaData(newMetaData);
    }
    return metaData;
  }

  @Override
  public int getRowCount(final Query query) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized Writer<DataObject> getWriter() {
    if (writer == null && directory != null) {
      writer = new DirectoryDataObjectStoreWriter(this);
    }
    return writer;
  }

  @PostConstruct
  @Override
  public void initialize() {
    if (!directory.exists()) {
      directory.mkdirs();
    }
    super.initialize();
  }

  @Override
  public synchronized void insert(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    final String typePath = metaData.getPath();
    Writer<DataObject> writer = writers.get(typePath);
    if (writer == null) {
      final String schemaName = PathUtil.getPath(typePath);
      final File subDirectory = new File(getDirectory(), schemaName);
      final File file = new File(subDirectory, metaData.getTypeName() + "."
        + getFileExtension());
      final Resource resource = new FileSystemResource(file);
      writer = AbstractDataObjectIoFactory.dataObjectWriter(metaData, resource);
      if (writer instanceof ObjectWithProperties) {
        final ObjectWithProperties properties = writer;
        properties.setProperties(getProperties());
      }
      writers.put(typePath, writer);
    }
    writer.write(object);
    addStatistic("Insert", object);
  }

  public boolean isCreateMissingDataStore() {
    return createMissingDataStore;
  }

  public boolean isCreateMissingTables() {
    return createMissingTables;
  }

  protected DataObjectMetaData loadMetaData(final String schemaName,
    final File file) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<String, DataObjectMetaData> metaDataMap) {
    final String schemaName = schema.getPath();
    final File subDirectory = new File(directory, schemaName);
    final File[] files = subDirectory.listFiles(new ExtensionFilenameFilter(
      fileExtension));
    if (files != null) {
      for (final File file : files) {
        final DataObjectMetaData metaData = loadMetaData(schemaName, file);
        if (metaData != null) {
          final String typePath = metaData.getPath();
          metaDataMap.put(typePath, metaData);
        }
      }
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    final File[] directories = directory.listFiles(new DirectoryFilenameFilter());
    if (directories != null) {
      for (final File subDirectory : directories) {
        final String directoryName = FileUtil.getFileName(subDirectory);
        addSchema(new DataObjectStoreSchema(this, directoryName));
      }
    }
  }

  public void setCreateMissingDataStore(final boolean createMissingDataStore) {
    this.createMissingDataStore = createMissingDataStore;
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
