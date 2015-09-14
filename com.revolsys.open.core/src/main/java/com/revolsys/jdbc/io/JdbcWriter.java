package com.revolsys.jdbc.io;

import com.revolsys.io.Writer;
import com.revolsys.record.Record;

public interface JdbcWriter extends Writer<Record> {

  @Override
  public void flush();
}
