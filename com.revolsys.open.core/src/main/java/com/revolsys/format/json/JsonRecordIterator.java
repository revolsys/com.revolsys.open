package com.revolsys.format.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.FileUtil;

public class JsonRecordIterator extends AbstractIterator<Record>implements RecordReader {

  private JsonMapIterator iterator;

  private RecordDefinition recordDefinition;

  public JsonRecordIterator(final RecordDefinition recordDefinition, final InputStream in) {
    this(recordDefinition, FileUtil.createUtf8Reader(in));
  }

  public JsonRecordIterator(final RecordDefinition recordDefinition, final Reader in) {
    this(recordDefinition, in, false);
  }

  public JsonRecordIterator(final RecordDefinition recordDefinition, final Reader in,
    final boolean single) {
    this.recordDefinition = recordDefinition;
    try {
      this.iterator = new JsonMapIterator(in, single);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot open " + in, e);
    }
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(this.iterator);
    this.iterator = null;
    this.recordDefinition = null;
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.iterator.hasNext()) {
      final Map<String, Object> map = this.iterator.next();
      final Record object = new ArrayRecord(this.recordDefinition);
      for (final FieldDefinition attribute : this.recordDefinition.getFields()) {
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

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }
}
