package com.revolsys.data.io;

import java.io.File;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.io.IoFactoryRegistry;

public class FileDataObjectReaderFactory extends
  AbstractFactoryBean<DataObjectReader> {

  public static DataObjectReader dataObjectReader(final File file) {
    final Resource resource = new FileSystemResource(file);
    return dataObjectReader(resource);
  }

  public static DataObjectReader dataObjectReader(final Resource resource) {
    final DataObjectReaderFactory readerFactory = getDataObjectReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final DataObjectReader reader = readerFactory.createDataObjectReader(resource);
      return reader;
    }
  }

  public static DataObjectReader dataObjectReader(final Resource resource,
    final RecordFactory factory) {
    final DataObjectReaderFactory readerFactory = getDataObjectReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final DataObjectReader reader = readerFactory.createDataObjectReader(
        resource, factory);
      return reader;
    }
  }

  protected static DataObjectReaderFactory getDataObjectReaderFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final DataObjectReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      DataObjectReaderFactory.class, resource);
    return readerFactory;
  }

  private RecordFactory factory = new ArrayRecordFactory();

  private Resource resource;

  @Override
  public DataObjectReader createInstance() throws Exception {
    return AbstractDataObjectReaderFactory.dataObjectReader(resource, factory);
  }

  @Override
  protected void destroyInstance(final DataObjectReader reader)
    throws Exception {
    reader.close();
    factory = null;
    resource = null;
  }

  public RecordFactory getFactory() {
    return factory;
  }

  @Override
  public Class<?> getObjectType() {
    return DataObjectReader.class;
  }

  public Resource getResource() {
    return resource;
  }

  public void setFactory(final RecordFactory factory) {
    this.factory = factory;
  }

  @Required
  public void setResource(final Resource resource) {
    this.resource = resource;
  }

}
