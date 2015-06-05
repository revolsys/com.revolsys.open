package com.revolsys.data.record.io;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Reader;

public interface RecordReader extends Reader<Record> {
  RecordDefinition getRecordDefinition();
}
