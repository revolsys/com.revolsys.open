package com.revolsys.format.directory;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordReader;

public class RecordReaderQueryIterator extends AbstractIterator<Record> {

  private Iterator<Record> iterator;

  private final RecordReader reader;

  private final Condition whereCondition;

  public RecordReaderQueryIterator(final RecordReader reader, final Query query) {
    this.reader = reader;
    this.whereCondition = query.getWhereCondition();
  }

  @Override
  protected void doClose() {
    this.reader.close();
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    while (true) {
      final Record record = this.iterator.next();
      if (this.whereCondition == null || this.whereCondition.test(record)) {
        return record;
      }
    }
  }

  @Override
  public synchronized void init() {
    this.reader.open();
    this.iterator = this.reader.iterator();
  }
}
