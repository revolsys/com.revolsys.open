package com.revolsys.jdbc.io;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.query.Query;
import com.revolsys.util.Property;

public class DataStoreIteratorFactory {

  private Reference<Object> factory;

  private String methodName;

  public DataStoreIteratorFactory() {
  }

  public DataStoreIteratorFactory(final Object factory, final String methodName) {
    this.factory = new WeakReference<Object>(factory);
    this.methodName = methodName;
  }

  public AbstractIterator<DataObject> createIterator(
    final DataObjectStore dataStore, final Query query,
    final Map<String, Object> properties) {
    final Object factory = this.factory.get();
    if (factory != null && StringUtils.hasText(methodName)) {
      return Property.invoke(factory, methodName, dataStore, query, properties);
    } else {
      throw new UnsupportedOperationException(
        "Creating query iterators not supported");
    }
  }

}
