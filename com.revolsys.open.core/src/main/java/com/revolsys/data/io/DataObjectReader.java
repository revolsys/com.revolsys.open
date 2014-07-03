package com.revolsys.data.io;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Reader;

public interface DataObjectReader extends Reader<Record> {
  RecordDefinition getMetaData();
}
