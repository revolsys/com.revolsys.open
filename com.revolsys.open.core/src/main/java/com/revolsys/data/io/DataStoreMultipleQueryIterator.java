package com.revolsys.data.io;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.AbstractMultipleIterator;
import com.revolsys.data.record.Record;

public class DataStoreMultipleQueryIterator extends
  AbstractMultipleIterator<Record> {

  private RecordStoreQueryReader reader;

  private int queryIndex = 0;

  public DataStoreMultipleQueryIterator(final RecordStoreQueryReader reader) {
    this.reader = reader;
  }

  @Override
  public void doClose() {
    super.doClose();
    reader = null;
  }

  @Override
  public AbstractIterator<Record> getNextIterator()
    throws NoSuchElementException {
    if (reader == null) {
      throw new NoSuchElementException();
    } else {
      final AbstractIterator<Record> iterator = reader.createQueryIterator(queryIndex);
      queryIndex++;
      return iterator;
    }
  }

}
