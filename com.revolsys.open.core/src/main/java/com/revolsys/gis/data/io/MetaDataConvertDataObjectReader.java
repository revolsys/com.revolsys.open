package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class MetaDataConvertDataObjectReader extends AbstractReader<DataObject>
  implements DataObjectReader, Iterator<DataObject> {

  private DataObjectMetaData metaData;

  private Reader<DataObject> reader;

  private boolean open;

  private Iterator<DataObject> iterator;

  public MetaDataConvertDataObjectReader(DataObjectMetaData metaData,
    Reader<DataObject> reader) {
    this.metaData = metaData;
    this.reader = reader;
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
    return iterator.hasNext();
  }

  public DataObject next() {
    if (hasNext()) {
      DataObject source = iterator.next();
      DataObject target = new ArrayDataObject(metaData);
      for (Attribute attribute : metaData.getAttributes()) {
        String name = attribute.getName();
        Object value = source.getValue(name);
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
    iterator.remove();
  }

  public void close() {
    reader.close();
  }

  public void open() {
    open = true;
    this.iterator = reader.iterator();
  }
}
