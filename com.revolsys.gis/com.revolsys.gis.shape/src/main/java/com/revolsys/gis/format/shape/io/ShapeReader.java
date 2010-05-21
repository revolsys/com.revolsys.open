package com.revolsys.gis.format.shape.io;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class ShapeReader extends AbstractReader<DataObject> implements
  DataObjectReader {

  private ShapeIterator iterator;

  public ShapeReader(
    final Resource resource,
    final DataObjectFactory factory) {
    try {
      iterator = new ShapeIterator(resource, factory);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void close() {
    iterator.close();
  }

  public DataObjectMetaData getMetaData() {
    return iterator.getMetaData();
  }

  @Override
  public Map<String, Object> getProperties() {
    return iterator.getProperties();
  }

  public Iterator<DataObject> iterator() {
    return iterator;
  }

}
