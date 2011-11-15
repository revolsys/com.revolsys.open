package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class MapReaderDataObjectReader extends AbstractReader<DataObject>
  implements DataObjectReader, Iterator<DataObject> {

  private DataObjectMetaData metaData;

  private Reader<Map<String, Object>> mapReader;

  private boolean open;

  private Iterator<Map<String, Object>> mapIterator;

  public MapReaderDataObjectReader(DataObjectMetaData metaData,
    Reader<Map<String, Object>> mapReader) {
    this.metaData = metaData;
    this.mapReader = mapReader;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public Iterator<DataObject> iterator() {
    return this;
  }

  public boolean hasNext() {
    if (!open) {
      open();
    }
    return mapIterator.hasNext();
  }

  public DataObject next() {
    if (hasNext()) {
      Map<String, Object> source = mapIterator.next();
      DataObject target = new ArrayDataObject(metaData);
      for (Attribute attribute : metaData.getAttributes()) {
        String name = attribute.getName();
        Object value = source.get(name);
        if (value != null) {
          DataType dataType = metaData.getAttributeType(name);
          Object convertedValue = StringConverterRegistry.toObject(dataType,
            value);
          target.setValue(name, convertedValue);
        }
      }
      return target;
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    mapIterator.remove();
  }

  public void close() {
    mapReader.close();
  }

  public void open() {
    open = true;
    this.mapIterator = mapReader.iterator();
  }
}
