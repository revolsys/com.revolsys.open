package com.revolsys.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.AbstractDataObjectIoFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;
import com.vividsolutions.jts.geom.Geometry;

public class AbstractDirectoryDataObjectStore extends AbstractDataObjectStore {
  private Map<QName, Writer<DataObject>> writers = new HashMap<QName, Writer<DataObject>>();

  private File directory;

  public File getDirectory() {
    return directory;
  }

  public void setDirectory(File directory) {
    this.directory = directory;
  }

  private String fileExtension;

  protected void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public Writer<DataObject> createWriter() {
    return new DirectoryDataObjectStoreWriter();
  }

  @Override
  public void close() {
    directory = null;
    if (writers != null) {
      for (Writer<DataObject> writer : writers.values()) {
        writer.close();
      }
      writers = null;
    }
  }

  public synchronized void insert(DataObject object) {
    DataObjectMetaData metaData = object.getMetaData();
    QName typeName = metaData.getName();
    Writer<DataObject> writer = writers.get(typeName);
    if (writer == null) {
      File subDirectory = new File(getDirectory(), typeName.getNamespaceURI());
      File file = new File(subDirectory, typeName.getLocalPart() + "."
        + getFileExtension());
      Resource resource = new FileSystemResource(file);
      writer = AbstractDataObjectIoFactory.dataObjectWriter(metaData, resource);
    }
    writer.write(object);
    addStatistic("Insert", object);
  }

  public Reader<DataObject> query(QName typeName, BoundingBox boundingBox) {
    throw new UnsupportedOperationException();
  }

  public Reader<DataObject> query(QName typeName, Geometry geometry) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    DataObjectStoreSchema schema,
    Map<QName, DataObjectMetaData> metaDataMap) {
    String schemaName = schema.getName();
    File subDirectory = new File(directory, schemaName);
    File[] files = subDirectory.listFiles(new ExtensionFilenameFilter(
      fileExtension));
    if (files != null) {
      for (File file : files) {
        DataObjectMetaData metaData = loadMetaData(schemaName, file);
        if (metaData != null) {
          QName typeName = metaData.getName();
          metaDataMap.put(typeName, metaData);
        }
      }
    }
  }

  protected DataObjectMetaData loadMetaData(String schemaName, File file) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void loadSchemas(Map<String, DataObjectStoreSchema> schemaMap) {
    File[] directories = directory.listFiles(new DirectoryFilenameFilter());
    if (directories != null) {
      for (File subDirectory : directories) {
        String directoryName = subDirectory.getName();
        addSchema(new DataObjectStoreSchema(this, directoryName));
      }
    }
  }
}
