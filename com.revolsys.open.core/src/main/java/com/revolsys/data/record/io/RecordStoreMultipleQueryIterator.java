package com.revolsys.data.record.io;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.iterator.AbstractMultipleIterator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class RecordStoreMultipleQueryIterator extends AbstractMultipleIterator<Record> {

  private int queryIndex = 0;

  private RecordStoreQueryReader reader;

  public RecordStoreMultipleQueryIterator(final RecordStoreQueryReader reader) {
    this.reader = reader;
  }

  @Override
  public void doClose() {
    super.doClose();
    this.reader = null;
  }

  @Override
  public AbstractIterator<Record> getNextIterator() throws NoSuchElementException {
    if (this.reader == null) {
      throw new NoSuchElementException();
    } else {
      final AbstractIterator<Record> iterator = this.reader.createQueryIterator(this.queryIndex);
      this.queryIndex++;
      return iterator;
    }
  }

  public RecordDefinition getRecordDefinition() {
    final AbstractIterator<Record> iterator = getIterator();
    if (iterator instanceof RecordReader) {
      final RecordReader reader = (RecordReader)iterator;
      return reader.getRecordDefinition();
    }
    return null;
  }

}
