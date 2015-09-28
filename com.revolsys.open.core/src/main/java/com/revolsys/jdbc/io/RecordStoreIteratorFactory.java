package com.revolsys.jdbc.io;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.record.Record;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordStore;
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

  public AbstractIterator<Record> newIterator(final RecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    final Object factory = this.factory.get();
    if (factory != null && Property.hasValue(this.methodName)) {
      return Property.invoke(factory, this.methodName, recordStore, query, properties);
    } else {
      throw new UnsupportedOperationException("Creating query iterators not supported");
    }
  }

}
