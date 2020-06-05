package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.util.count.CategoryLabelCountMap;

public class JdbcRecordWriterSingle extends AbstractJdbcRecordWriter {

  public JdbcRecordWriterSingle(final JdbcRecordStore recordStore) {
    super(recordStore);
  }

  public JdbcRecordWriterSingle(final JdbcRecordStore recordStore,
    final CategoryLabelCountMap statistics) {
    super(recordStore, statistics);
  }

  public JdbcRecordWriterSingle(final JdbcRecordStore recordStore,
    final RecordDefinitionProxy recordDefinition) {
    super(recordStore, recordDefinition);
  }

  public JdbcRecordWriterSingle(final JdbcRecordStore recordStore,
    final RecordDefinitionProxy recordDefinition, final CategoryLabelCountMap statistics) {
    super(recordStore, recordDefinition, statistics);
  }

  @Override
  protected void deleteRecordDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement) throws SQLException {
    statement.executeUpdate();
  }

  @Override
  protected void insertRecordDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final JdbcRecordDefinition recordDefinition,
    final Record record) throws SQLException {
    statement.executeUpdate();
  }

  @Override
  protected void insertRecordRowIdDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final Record record,
    final JdbcRecordDefinition recordDefinition) throws SQLException {
    statement.executeUpdate();
  }

  @Override
  protected void insertRecordSequenceDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final Record record,
    final JdbcRecordDefinition recordDefinition) throws SQLException {
    statement.executeUpdate();
  }

  @Override
  protected void updateRecordDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    statement.executeUpdate();
  }

}
