package com.revolsys.data.io;

import java.io.File;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.io.IoFactoryRegistry;

public class FileRecordReaderFactory extends
  AbstractFactoryBean<RecordReader> {

  public static RecordReader recordReader(final File file) {
    final Resource resource = new FileSystemResource(file);
    return recordReader(resource);
  }

  public static RecordReader recordReader(final Resource resource) {
    final RecordReaderFactory readerFactory = getRecordReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource);
      return reader;
    }
  }

  public static RecordReader recordReader(final Resource resource,
    final RecordFactory factory) {
    final RecordReaderFactory readerFactory = getRecordReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(
        resource, factory);
      return reader;
    }
  }

  protected static RecordReaderFactory getRecordReaderFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final RecordReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      RecordReaderFactory.class, resource);
    return readerFactory;
  }

  private RecordFactory factory = new ArrayRecordFactory();

  private Resource resource;

  @Override
  public RecordReader createInstance() throws Exception {
    return AbstractRecordReaderFactory.recordReader(resource, factory);
  }

  @Override
  protected void destroyInstance(final RecordReader reader)
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
    return RecordReader.class;
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
