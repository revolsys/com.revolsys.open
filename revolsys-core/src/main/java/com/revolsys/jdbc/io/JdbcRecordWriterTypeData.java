package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.record.Record;

public class JdbcRecordWriterTypeData {

  private int batchCount = 0;

  private final String sql;

  private final PreparedStatement statement;

  private List<Record> records;

  private final JdbcRecordDefinition recordDefinition;

  private final boolean hasGeneratedKeys;

  public JdbcRecordWriterTypeData(final JdbcRecordDefinition recordDefinition, final String sql,
    final PreparedStatement statement, final boolean hasGeneratedKeys) {
    this.recordDefinition = recordDefinition;
    this.sql = sql;
    this.statement = statement;
    this.hasGeneratedKeys = hasGeneratedKeys;
  }

  public synchronized int addCount() {
    return ++this.batchCount;
  }

  public int addRecord(final Record record) {
    if (this.records == null) {
      this.records = new ArrayList<>();
    }
    this.records.add(record);
    return addCount();
  }

  public void clear() {
    this.batchCount = 0;
    if (this.records != null) {
      this.records.clear();
    }
  }

  public int getBatchCount() {
    return this.batchCount;
  }

  public JdbcRecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public List<Record> getRecords() {
    return this.records;
  }

  public String getSql() {
    return this.sql;
  }

  public PreparedStatement getStatement() {
    return this.statement;
  }

  public boolean isHasGeneratedKeys() {
    return this.hasGeneratedKeys;
  }

}
