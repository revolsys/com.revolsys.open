package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.ArrayRecord;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class MetaDataConvertDataObjectReader extends AbstractReader<DataObject>
  implements DataObjectReader, Iterator<DataObject> {

  private final DataObjectMetaData metaData;

  private final Reader<DataObject> reader;

  private boolean open;

  private Iterator<DataObject> iterator;

  public MetaDataConvertDataObjectReader(final DataObjectMetaData metaData,
    final Reader<DataObject> reader) {
    this.metaData = metaData;
    this.reader = reader;
  }

  @Override
  public void close() {
    reader.close();
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  public boolean hasNext() {
    if (!open) {
      open();
    }
    return iterator.hasNext();
  }

  @Override
  public Iterator<DataObject> iterator() {
    return this;
  }

  @Override
  public DataObject next() {
    if (hasNext()) {
      final DataObject source = iterator.next();
      final DataObject target = new ArrayRecord(metaData);
      for (final Attribute attribute : metaData.getAttributes()) {
        final String name = attribute.getName();
        final Object value = source.getValue(name);
        if (value != null) {
          final DataType dataType = metaData.getAttributeType(name);
          final Object convertedValue = StringConverterRegistry.toObject(
            dataType, value);
          target.setValue(name, convertedValue);
        }
      }
      return target;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void open() {
    open = true;
    this.iterator = reader.iterator();
  }

  @Override
  public void remove() {
    iterator.remove();
  }
}
