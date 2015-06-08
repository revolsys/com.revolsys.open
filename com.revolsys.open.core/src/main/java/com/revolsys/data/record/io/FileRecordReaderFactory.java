package com.revolsys.data.record.io;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.RecordFactory;

public class FileRecordReaderFactory extends AbstractFactoryBean<RecordReader> {

  private RecordFactory factory = new ArrayRecordFactory();

  private Resource resource;

  @Override
  public RecordReader createInstance() throws Exception {
    return RecordIo.recordReader(this.resource, this.factory);
  }

  @Override
  protected void destroyInstance(final RecordReader reader) throws Exception {
    reader.close();
    this.factory = null;
    this.resource = null;
  }

  public RecordFactory getFactory() {
    return this.factory;
  }

  @Override
  public Class<?> getObjectType() {
    return RecordReader.class;
  }

  public Resource getResource() {
    return this.resource;
  }

  public void setFactory(final RecordFactory factory) {
    this.factory = factory;
  }

  @Required
  public void setResource(final Resource resource) {
    this.resource = resource;
  }

}
