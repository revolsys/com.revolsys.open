package com.revolsys.data.io;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class RecordIteratorReader extends IteratorReader<Record>
  implements RecordReader {
  public RecordIteratorReader(final RecordIterator iterator) {
    super(iterator);
  }

  @Override
  public RecordDefinition getMetaData() {
    final RecordIterator iterator = (RecordIterator)iterator();
    iterator.hasNext();
    return iterator.getMetaData();
  }

  @Override
  public String toString() {
    return "Reader=" + iterator().toString();
  }
}
