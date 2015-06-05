package com.revolsys.data.record.io;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.iterator.AbstractMultipleIterator;
import com.revolsys.data.record.Record;

public class RecordStoreMultipleQueryIterator extends
AbstractMultipleIterator<Record> {

  private RecordStoreQueryReader reader;

  private int queryIndex = 0;

  public RecordStoreMultipleQueryIterator(final RecordStoreQueryReader reader) {
    this.reader = reader;
  }

  @Override
  public void doClose() {
    super.doClose();
    this.reader = null;
  }

  @Override
  public AbstractIterator<Record> getNextIterator()
      throws NoSuchElementException {
    if (this.reader == null) {
      throw new NoSuchElementException();
    } else {
      final AbstractIterator<Record> iterator = this.reader.createQueryIterator(this.queryIndex);
      this.queryIndex++;
      return iterator;
    }
  }

}
