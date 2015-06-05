package com.revolsys.data.record.io;

import java.util.Iterator;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public interface RecordIterator extends Iterator<Record> {
  public RecordDefinition getRecordDefinition();
}
