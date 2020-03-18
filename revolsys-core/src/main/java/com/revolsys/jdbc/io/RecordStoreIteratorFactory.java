package com.revolsys.jdbc.io;

import java.util.Map;

import org.jeometry.common.function.Function3;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.record.Record;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordStore;

public class RecordStoreIteratorFactory {

  private Function3<RecordStore, Query, Map<String, Object>, AbstractIterator<Record>> createFunction;

  public RecordStoreIteratorFactory() {
  }

  public RecordStoreIteratorFactory(
    final Function3<RecordStore, Query, Map<String, Object>, AbstractIterator<Record>> createFunction) {
    this.createFunction = createFunction;
  }

  public AbstractIterator<Record> newIterator(final RecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    if (this.createFunction != null) {
      return this.createFunction.apply(recordStore, query, properties);
    } else {
      throw new UnsupportedOperationException("Creating query iterators not supported");
    }
  }

}
