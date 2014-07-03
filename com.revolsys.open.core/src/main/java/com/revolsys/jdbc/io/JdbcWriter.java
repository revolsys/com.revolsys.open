package com.revolsys.jdbc.io;

import com.revolsys.data.record.Record;
import com.revolsys.io.Writer;

public interface JdbcWriter extends Writer<Record> {

  @Override
  public void flush();
}
