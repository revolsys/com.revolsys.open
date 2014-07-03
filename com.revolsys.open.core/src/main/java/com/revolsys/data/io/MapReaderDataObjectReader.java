package com.revolsys.data.io;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class MapReaderDataObjectReader extends AbstractReader<Record>
  implements DataObjectReader, Iterator<Record> {

  private final RecordDefinition metaData;

  private final Reader<Map<String, Object>> mapReader;

  private boolean open;

  private Iterator<Map<String, Object>> mapIterator;

  public MapReaderDataObjectReader(final RecordDefinition metaData,
    final Reader<Map<String, Object>> mapReader) {
    this.metaData = metaData;
    this.mapReader = mapReader;
  }

  @Override
  public void close() {
    mapReader.close();
  }

  @Override
  public RecordDefinition getMetaData() {
    return metaData;
  }

  @Override
  public boolean hasNext() {
    if (!open) {
      open();
    }
    return mapIterator.hasNext();
  }

  @Override
  public Iterator<Record> iterator() {
    return this;
  }

  @Override
  public Record next() {
    if (hasNext()) {
      final Map<String, Object> source = mapIterator.next();
      final Record target = new ArrayRecord(metaData);
      for (final Attribute attribute : metaData.getAttributes()) {
        final String name = attribute.getName();
        final Object value = source.get(name);
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
    this.mapIterator = mapReader.iterator();
  }

  @Override
  public void remove() {
    mapIterator.remove();
  }
}
