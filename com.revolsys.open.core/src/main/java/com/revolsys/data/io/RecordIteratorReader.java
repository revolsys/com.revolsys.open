package com.revolsys.data.io;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class RecordIteratorReader extends IteratorReader<Record>
  implements RecordReader {
  public RecordIteratorReader(final RecordIterator iterator) {
    super(iterator);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    final RecordIterator iterator = (RecordIterator)iterator();
    iterator.hasNext();
    return iterator.getRecordDefinition();
  }

  @Override
  public String toString() {
    return "Reader=" + iterator().toString();
  }
}
