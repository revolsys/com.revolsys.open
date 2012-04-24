package com.revolsys.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.AbstractDataObjectIoFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;
import com.vividsolutions.jts.geom.Geometry;

public class DirectoryDataObjectStore extends AbstractDataObjectStore {

  private boolean createMissingTables = true;

  private Map<QName, Writer<DataObject>> writers = new HashMap<QName, Writer<DataObject>>();

  private File directory;

  private String fileExtension;

  private Writer<DataObject> writer;

  private boolean createMissingDataStore = true;

  public DirectoryDataObjectStore(File directory, String fileExtension) {
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
      writers = null;
    }
    if (writer != null) {
      writer.close();
      writer = null;
    }
    super.close();
  }

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
      final QName typeName = objectMetaData.getName();
      final String schemaName = typeName.getNamespaceURI();
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
        this, schema, typeName);
      for (final Attribute attribute : objectMetaData.getAttributes()) {
        final Attribute newAttribute = new Attribute(attribute);
        newMetaData.addAttribute(newAttribute);
      }
      schema.addMetaData(newMetaData);
    }
    return metaData;
  }

  public synchronized Writer<DataObject> getWriter() {
    if (writer == null) {
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
    final QName typeName = metaData.getName();
    Writer<DataObject> writer = writers.get(typeName);
    if (writer == null) {
      final File subDirectory = new File(getDirectory(),
        typeName.getNamespaceURI());
      final File file = new File(subDirectory, typeName.getLocalPart() + "."
        + getFileExtension());
      final Resource resource = new FileSystemResource(file);
      writer = AbstractDataObjectIoFactory.dataObjectWriter(metaData, resource);
      writers.put(typeName, writer);
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

  protected DataObjectMetaData loadMetaData(
    final String schemaName,
    final File file) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<QName, DataObjectMetaData> metaDataMap) {
    final String schemaName = schema.getName();
    final File subDirectory = new File(directory, schemaName);
    final File[] files = subDirectory.listFiles(new ExtensionFilenameFilter(
      fileExtension));
    if (files != null) {
      for (final File file : files) {
        final DataObjectMetaData metaData = loadMetaData(schemaName, file);
        if (metaData != null) {
          final QName typeName = metaData.getName();
          metaDataMap.put(typeName, metaData);
        }
      }
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    final File[] directories = directory.listFiles(new DirectoryFilenameFilter());
    if (directories != null) {
      for (final File subDirectory : directories) {
        final String directoryName = subDirectory.getName();
        addSchema(new DataObjectStoreSchema(this, directoryName));
      }
    }
  }

  public Reader<DataObject> query(
    final QName typeName,
    final BoundingBox boundingBox) {
    throw new UnsupportedOperationException();
  }

  public Reader<DataObject> query(final QName typeName, final Geometry geometry) {
    throw new UnsupportedOperationException();
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
