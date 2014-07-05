package com.revolsys.io.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.FileUtil;

public class JsonRecordIterator extends AbstractIterator<Record>
  implements RecordIterator {

  private RecordDefinition metaData;

  private JsonMapIterator iterator;

  public JsonRecordIterator(final RecordDefinition metaData,
    final InputStream in) {
    this(metaData, FileUtil.createUtf8Reader(in));
  }

  public JsonRecordIterator(final RecordDefinition metaData,
    final Reader in) {
    this(metaData, in, false);
  }

  public JsonRecordIterator(final RecordDefinition metaData,
    final Reader in, final boolean single) {
    this.metaData = metaData;
    try {
      this.iterator = new JsonMapIterator(in, single);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot open " + in, e);
    }
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(iterator);
    iterator = null;
    metaData = null;
  }

  @Override
  public RecordDefinition getMetaData() {
    return metaData;
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (iterator.hasNext()) {
      final Map<String, Object> map = iterator.next();
      final Record object = new ArrayRecord(metaData);
      for (final Attribute attribute : metaData.getAttributes()) {
        final String name = attribute.getName();
        final Object value = map.get(name);
        if (value != null) {
          final DataType dataType = attribute.getType();
          @SuppressWarnings("unchecked")
          final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
          if (dataTypeClass.isAssignableFrom(value.getClass())) {
            object.setValue(name, value);
          } else {
            final StringConverter<Object> converter = StringConverterRegistry.getInstance()
              .getConverter(dataTypeClass);
            if (converter == null) {
              object.setValue(name, value);
            } else {
              final Object convertedValue = converter.toObject(value);
              object.setValue(name, convertedValue);
            }
          }
        }
      }
      return object;
    } else {
      throw new NoSuchElementException();
    }
  }
}
