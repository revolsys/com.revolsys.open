package com.revolsys.jdbc.io;

import com.revolsys.record.io.RecordWriter;

public interface JdbcRecordWriter extends RecordWriter {

  void commit();

  String getLabel();

  String getSqlPrefix();

  String getSqlSuffix();

  boolean isFlushBetweenTypes();

  boolean isQuoteColumnNames();

  boolean isThrowExceptions();

  void setThrowExceptions(boolean throwExceptions);

}
