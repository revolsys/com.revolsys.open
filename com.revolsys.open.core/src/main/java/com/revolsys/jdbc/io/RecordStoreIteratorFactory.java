package com.revolsys.jdbc.io;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.util.Property;

public class RecordStoreIteratorFactory {

  private Reference<Object> factory;

  private String methodName;

  public RecordStoreIteratorFactory() {
  }

  public RecordStoreIteratorFactory(final Object factory, final String methodName) {
    this.factory = new WeakReference<Object>(factory);
    this.methodName = methodName;
  }

  public AbstractIterator<Record> createIterator(
    final RecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    final Object factory = this.factory.get();
    if (factory != null && Property.hasValue(methodName)) {
      return Property.invoke(factory, methodName, recordStore, query, properties);
    } else {
      throw new UnsupportedOperationException(
        "Creating query iterators not supported");
    }
  }

}
