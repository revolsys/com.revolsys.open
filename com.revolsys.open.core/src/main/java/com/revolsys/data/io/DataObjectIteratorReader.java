package com.revolsys.data.io;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class DataObjectIteratorReader extends IteratorReader<Record>
  implements DataObjectReader {
  public DataObjectIteratorReader(final DataObjectIterator iterator) {
    super(iterator);
  }

  @Override
  public RecordDefinition getMetaData() {
    final DataObjectIterator iterator = (DataObjectIterator)iterator();
    iterator.hasNext();
    return iterator.getMetaData();
  }

  @Override
  public String toString() {
    return "Reader=" + iterator().toString();
  }
}
