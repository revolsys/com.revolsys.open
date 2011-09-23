package com.revolsys.gis.data.io;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.io.IoFactoryRegistry;

public class FileDataObjectReaderFactory implements
  FactoryBean<DataObjectReader> {

  public static DataObjectReader dataObjectReader(final Resource resource,
    final DataObjectFactory factory) {
    final DataObjectReaderFactory readerFactory = getDataObjectReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final DataObjectReader reader = readerFactory.createDataObjectReader(
        resource, factory);
      return reader;
    }
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

  protected static DataObjectReaderFactory getDataObjectReaderFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;
    final DataObjectReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      DataObjectReaderFactory.class, resource);
    return readerFactory;
  }

  private DataObjectFactory factory = new ArrayDataObjectFactory();

  private Resource resource;

  public DataObjectFactory getFactory() {
    return factory;
  }

  public DataObjectReader getObject() throws Exception {
    return AbstractDataObjectReaderFactory.dataObjectReader(resource, factory);
  }

  public Class<?> getObjectType() {
    return DataObjectReader.class;
  }

  public Resource getResource() {
    return resource;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setFactory(final DataObjectFactory factory) {
    this.factory = factory;
  }

  @Required
  public void setResource(final Resource resource) {
    this.resource = resource;
  }

}
