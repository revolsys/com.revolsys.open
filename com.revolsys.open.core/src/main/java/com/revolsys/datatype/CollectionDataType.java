package com.revolsys.datatype;

import java.util.Collection;
import java.util.List;

import com.revolsys.collection.list.Lists;

public class CollectionDataType extends SimpleDataType {

  private final DataType contentType;

  public CollectionDataType(final String name, final Class<?> javaClass,
    final DataType contentType) {
    super(name, javaClass);
    this.contentType = contentType;
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    if (this == DataTypes.LIST) {
      return Lists.equalsNotNull((List<?>)value1, (List<?>)value2);
    } else {
      return super.equalsNotNull(value1, value2);
    }
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    if (this == DataTypes.LIST) {
      return Lists.equalsNotNull((List<?>)value1, (List<?>)value2, excludeFieldNames);
    } else {
      return super.equalsNotNull(value1, value2, excludeFieldNames);
    }
  }

  public DataType getContentType() {
    return this.contentType;
  }

  @Override
  protected Object toObjectDo(final Object value) {
    if (value instanceof Collection) {
      try {
        final Collection<?> collection = (Collection<?>)value;
        @SuppressWarnings({
          "unchecked", "rawtypes"
        })
        final Collection<Object> newCollection = (Collection)getJavaClass().newInstance();
        newCollection.addAll(collection);
        return newCollection;
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else {
      return super.toObjectDo(value);
    }
  }
}
