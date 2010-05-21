package com.revolsys.gis.format.xbase.io;

import java.io.IOException;
import java.util.Iterator;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class XBaseReader extends AbstractReader<DataObject> implements
  DataObjectReader {
  private XbaseIterator iterator;

  public XBaseReader(
    final Resource resource,
    final DataObjectFactory factory) {
    try {
      this.iterator = new XbaseIterator(resource, factory);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open resource: " + resource, e);
    }
  }


  public void close() {
    iterator.close();
  }

  public DataObjectMetaData getMetaData() {
    return iterator.getMetaData();
  }

  public Iterator<DataObject> iterator() {
    return iterator;
  }
}
