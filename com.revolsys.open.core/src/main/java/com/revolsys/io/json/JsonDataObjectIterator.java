package com.revolsys.io.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.ArrayRecord;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.FileUtil;

public class JsonDataObjectIterator extends AbstractIterator<DataObject>
  implements DataObjectIterator {

  private DataObjectMetaData metaData;

  private JsonMapIterator iterator;

  public JsonDataObjectIterator(final DataObjectMetaData metaData,
    final InputStream in) {
    this(metaData, FileUtil.createUtf8Reader(in));
  }

  public JsonDataObjectIterator(final DataObjectMetaData metaData,
    final Reader in) {
    this(metaData, in, false);
  }

  public JsonDataObjectIterator(final DataObjectMetaData metaData,
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
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    if (iterator.hasNext()) {
      final Map<String, Object> map = iterator.next();
      final DataObject object = new ArrayRecord(metaData);
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
