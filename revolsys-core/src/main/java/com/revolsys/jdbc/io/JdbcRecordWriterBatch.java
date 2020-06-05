package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class JdbcRecordWriterBatch extends AbstractJdbcRecordWriter {

  private final int batchSize;

  private final Map<JdbcRecordDefinition, Integer> typeCountMap = new LinkedHashMap<>();

  public JdbcRecordWriterBatch(final JdbcRecordStore recordStore,
    final RecordDefinitionProxy recordDefinition, final int batchSize) {
    super(recordStore, recordDefinition);
    this.batchSize = batchSize;
  }

  @Override
  protected void closeTypeData(final JdbcRecordWriterTypeData data) {
    processCurrentBatch(data);
  }

  @Override
  protected void deleteRecordDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement) throws SQLException {
    statement.addBatch();
    data.addCount();
  }

  @Override
  protected void flushDo(final JdbcRecordWriterTypeData data) {
    processCurrentBatch(data);
  }

  public int getBatchSize() {
    return this.batchSize;
  }

  @Override
  protected void insertRecordDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final JdbcRecordDefinition recordDefinition,
    final Record record) throws SQLException {
    insertStatementAddBatch(data, statement, record);
  }

  @Override
  protected void insertRecordRowIdDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final Record record,
    final JdbcRecordDefinition recordDefinition) throws SQLException {
    insertStatementAddBatch(data, statement, record);
  }

  @Override
  protected void insertRecordSequenceDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final Record record,
    final JdbcRecordDefinition recordDefinition) throws SQLException {
    insertStatementAddBatch(data, statement, record);
  }

  private void insertStatementAddBatch(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final Record record) throws SQLException {
    statement.addBatch();
    final Integer batchCount = data.addRecord(record);
    if (batchCount >= this.batchSize) {
      processCurrentBatch(data);
    }
  }

  private void processCurrentBatch(final JdbcRecordWriterTypeData data) {
    final int batchCount = data.getBatchCount();
    final JdbcRecordDefinition recordDefinition = data.getRecordDefinition();
    final PreparedStatement statement = data.getStatement();

    try {
      Integer typeCount = this.typeCountMap.get(recordDefinition);
      if (typeCount == null) {
        typeCount = batchCount;
      } else {
        typeCount += batchCount;
      }
      this.typeCountMap.put(recordDefinition, typeCount);
      final JdbcRecordStore recordStore = getRecordStore();
      recordStore.execteBatch(statement);

      if (data.isHasGeneratedKeys()) {
        final List<Record> records = data.getRecords();
        if (records != null) {
          final ResultSet generatedKeyResultSet = statement.getGeneratedKeys();
          int recordIndex = 0;
          while (generatedKeyResultSet.next()) {
            final Record record = records.get(recordIndex++);
            int columnIndex = 1;
            for (final FieldDefinition idField : recordDefinition.getIdFields()) {
              final Object idValue = generatedKeyResultSet.getObject(columnIndex);
              if (!generatedKeyResultSet.wasNull()) {
                final int index = idField.getIndex();
                record.setValue(index, idValue);
                columnIndex++;
              }

            }
          }
        }
      }
    } catch (final SQLException e) {
      final String sql = data.getSql();
      throw this.connection.getException("Process Batch", sql, e);
    } catch (final RuntimeException e) {
      final String sql = data.getSql();
      Logs.error(this, sql, e);
      throw e;
    } finally {
      data.clear();
    }
  }

  @Override
  protected void updateRecordDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    statement.addBatch();
    final int batchCount = data.addCount();
    if (batchCount >= this.batchSize) {
      processCurrentBatch(data);
    }
  }
}
