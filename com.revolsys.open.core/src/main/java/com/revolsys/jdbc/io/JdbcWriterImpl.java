package com.revolsys.jdbc.io;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathName;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.property.GlobalIdProperty;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.count.CategoryLabelCountMap;

public class JdbcWriterImpl extends AbstractRecordWriter {
  private static final Logger LOG = Logger.getLogger(JdbcWriterImpl.class);

  private int batchSize = 1;

  private JdbcConnection connection;

  private boolean flushBetweenTypes = false;

  private String label;

  private JdbcRecordDefinition lastRecordDefinition;

  private boolean quoteColumnNames = true;

  private JdbcRecordStore recordStore;

  private String sqlPrefix;

  private String sqlSuffix;

  private CategoryLabelCountMap statistics;

  private boolean throwExceptions = false;

  private final Map<JdbcRecordDefinition, Integer> typeCountMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, Integer> typeDeleteBatchCountMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, String> typeDeleteSqlMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, PreparedStatement> typeDeleteStatementMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, Integer> typeInsertBatchCountMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, Integer> typeInsertSequenceBatchCountMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, String> typeInsertSequenceSqlMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, PreparedStatement> typeInsertSequenceStatementMap = new LinkedHashMap<>();

  private final Map<JdbcRecordDefinition, Integer> typeInsertRowIdBatchCountMap = new LinkedHashMap<>();

  private final Map<JdbcRecordDefinition, String> typeInsertRowIdSqlMap = new LinkedHashMap<>();

  private final Map<JdbcRecordDefinition, PreparedStatement> typeInsertRowIdStatementMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, String> typeInsertSqlMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, PreparedStatement> typeInsertStatementMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, Integer> typeUpdateBatchCountMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, String> typeUpdateSqlMap = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, PreparedStatement> typeUpdateStatementMap = new LinkedHashMap<>();

  private final Map<JdbcRecordDefinition, List<Record>> typeInsertRecords = new HashMap<>();

  private final Map<JdbcRecordDefinition, List<Record>> typeInsertSequenceRecords = new HashMap<>();

  private final Map<JdbcRecordDefinition, List<Record>> typeInsertRowIdRecords = new HashMap<>();

  private final Map<JdbcRecordDefinition, List<Record>> typeUpdateRecords = new HashMap<>();

  private final Map<JdbcRecordDefinition, List<Record>> typeDeleteRecords = new HashMap<>();

  public JdbcWriterImpl(final JdbcRecordStore recordStore) {
    this(recordStore, recordStore.getStatistics());
  }

  public JdbcWriterImpl(final JdbcRecordStore recordStore, final CategoryLabelCountMap statistics) {
    this.recordStore = recordStore;
    this.statistics = statistics;
    this.connection = recordStore.getJdbcConnection();
    final DataSource dataSource = this.connection.getDataSource();
    if (dataSource != null) {
      try {
        this.connection.setAutoCommit(false);
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to create connection", e);
      }
    }
    statistics.connect();
  }

  public void appendIdEquals(final StringBuilder sqlBuffer, final List<FieldDefinition> idFields) {
    boolean first = true;
    for (final FieldDefinition idField : idFields) {
      if (first) {
        first = false;
      } else {
        sqlBuffer.append(" AND ");
      }
      idField.appendColumnName(sqlBuffer, this.quoteColumnNames);
      sqlBuffer.append(" = ");
      ((JdbcFieldDefinition)idField).addStatementPlaceHolder(sqlBuffer);
    }
  }

  @Override
  @PreDestroy
  public void close() {
    flush();
    closeDo();
  }

  private void close(final Map<JdbcRecordDefinition, String> sqlMap,
    final Map<JdbcRecordDefinition, PreparedStatement> statementMap,
    final Map<JdbcRecordDefinition, Integer> batchCountMap,
    final Map<JdbcRecordDefinition, List<Record>> recordsByType, final boolean hasGeneratedKeys) {
    for (final Entry<JdbcRecordDefinition, PreparedStatement> entry : statementMap.entrySet()) {
      final JdbcRecordDefinition recordDefinition = entry.getKey();
      final PreparedStatement statement = entry.getValue();
      try {
        processCurrentBatch(recordDefinition, sqlMap, statement, batchCountMap, recordsByType,
          hasGeneratedKeys);
      } catch (final DataAccessException e) {
        if (this.throwExceptions) {
          throw e;
        } else {
          LOG.error("Error commiting records", e);
        }
      }
      JdbcUtils.close(statement);
    }
  }

  protected synchronized void closeDo() {
    if (this.recordStore != null) {
      try {
        close(this.typeInsertSqlMap, this.typeInsertStatementMap, this.typeInsertBatchCountMap,
          this.typeInsertRecords, false);

        close(this.typeInsertSequenceSqlMap, this.typeInsertSequenceStatementMap,
          this.typeInsertSequenceBatchCountMap, this.typeInsertSequenceRecords, true);

        close(this.typeInsertRowIdSqlMap, this.typeInsertRowIdStatementMap,
          this.typeInsertRowIdBatchCountMap, this.typeInsertRowIdRecords, true);

        close(this.typeUpdateSqlMap, this.typeUpdateStatementMap, this.typeUpdateBatchCountMap,
          this.typeUpdateRecords, false);

        close(this.typeDeleteSqlMap, this.typeDeleteStatementMap, this.typeDeleteBatchCountMap,
          this.typeDeleteRecords, false);

        if (this.statistics != null) {
          this.statistics.disconnect();
          this.statistics = null;
        }
      } finally {
        this.typeInsertSqlMap = null;
        this.typeInsertStatementMap = null;
        this.typeInsertBatchCountMap = null;
        this.typeInsertSequenceSqlMap = null;
        this.typeInsertSequenceStatementMap = null;
        this.typeInsertSequenceBatchCountMap = null;
        this.typeUpdateBatchCountMap = null;
        this.typeUpdateSqlMap = null;
        this.typeUpdateStatementMap = null;
        this.typeDeleteBatchCountMap = null;
        this.typeDeleteSqlMap = null;
        this.typeDeleteStatementMap = null;
        this.recordStore = null;
        if (this.connection != null) {
          final DataSource dataSource = this.connection.getDataSource();
          try {
            if (dataSource != null && !Transaction.isHasCurrentTransaction()) {
              this.connection.commit();
            }
          } catch (final SQLException e) {
            throw new RuntimeException("Failed to commit data:", e);
          } finally {
            FileUtil.closeSilent(this.connection);
            this.connection = null;
          }
        }
      }
    }
  }

  public synchronized void commit() {
    flush();
    JdbcUtils.commit(this.connection);
  }

  private void delete(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);
    PreparedStatement statement = this.typeDeleteStatementMap.get(recordDefinition);
    if (statement == null) {
      final String sql = getDeleteSql(recordDefinition);
      try {
        statement = this.connection.prepareStatement(sql);
        this.typeDeleteStatementMap.put(recordDefinition, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
        return;
      }
    }
    setIdEqualsValues(statement, 1, recordDefinition, record);
    statement.addBatch();
    Maps.addCount(this.typeDeleteBatchCountMap, recordDefinition);
    this.recordStore.addStatistic("Delete", record);
  }

  @Override
  public synchronized void flush() {
    flush(this.typeInsertSqlMap, this.typeInsertStatementMap, this.typeInsertBatchCountMap,
      this.typeInsertRecords, false);

    flush(this.typeInsertSequenceSqlMap, this.typeInsertSequenceStatementMap,
      this.typeInsertSequenceBatchCountMap, this.typeInsertSequenceRecords, true);

    flush(this.typeInsertRowIdSqlMap, this.typeInsertRowIdStatementMap,
      this.typeInsertRowIdBatchCountMap, this.typeInsertRowIdRecords, true);

    flush(this.typeUpdateSqlMap, this.typeUpdateStatementMap, this.typeUpdateBatchCountMap,
      this.typeUpdateRecords, false);

    flush(this.typeDeleteSqlMap, this.typeDeleteStatementMap, this.typeDeleteBatchCountMap,
      this.typeDeleteRecords, false);
  }

  private void flush(final Map<JdbcRecordDefinition, String> sqlMap,
    final Map<JdbcRecordDefinition, PreparedStatement> statementMap,
    final Map<JdbcRecordDefinition, Integer> batchCountMap,
    final Map<JdbcRecordDefinition, List<Record>> recordsByType, final boolean hasGeneratedKeys) {
    if (statementMap != null) {
      for (final Entry<JdbcRecordDefinition, PreparedStatement> entry : statementMap.entrySet()) {
        final JdbcRecordDefinition recordDefinition = entry.getKey();
        final PreparedStatement statement = entry.getValue();
        try {
          processCurrentBatch(recordDefinition, sqlMap, statement, batchCountMap, recordsByType,
            hasGeneratedKeys);
        } catch (final DataAccessException e) {
          if (this.throwExceptions) {
            throw e;
          } else {
            LOG.error("Error writing to database", e);
          }
        }
      }
    }
  }

  private void flushIfRequired(final JdbcRecordDefinition recordDefinition) {
    if (this.flushBetweenTypes && recordDefinition != this.lastRecordDefinition) {
      flush();
      this.lastRecordDefinition = recordDefinition;
    }
  }

  public int getBatchSize() {
    return this.batchSize;
  }

  private String getDeleteSql(final JdbcRecordDefinition recordDefinition) {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    if (idFields.isEmpty()) {
      throw new RuntimeException("No primary key found for: " + recordDefinition);
    } else {
      final String tableName = recordDefinition.getDbTableQualifiedName();
      String sql = this.typeDeleteSqlMap.get(recordDefinition);
      if (sql == null) {
        final StringBuilder sqlBuffer = new StringBuilder();
        if (this.sqlPrefix != null) {
          sqlBuffer.append(this.sqlPrefix);
        }
        sqlBuffer.append("delete ");
        sqlBuffer.append(" from ");
        sqlBuffer.append(tableName);
        sqlBuffer.append(" where ");
        appendIdEquals(sqlBuffer, idFields);
        sqlBuffer.append(" ");
        if (this.sqlSuffix != null) {
          sqlBuffer.append(this.sqlSuffix);
        }
        sql = sqlBuffer.toString();

        this.typeDeleteSqlMap.put(recordDefinition, sql);
      }
      return sql;
    }
  }

  private String getInsertSql(final JdbcRecordDefinition recordDefinition,
    final boolean generatePrimaryKey, final Map<JdbcRecordDefinition, String> sqlMap) {
    String sql = sqlMap.get(recordDefinition);
    if (sql == null) {
      final JdbcRecordStore recordStore = this.recordStore;
      final String tableName = recordDefinition.getDbTableQualifiedName();
      final boolean hasRowIdField = recordStore.isIdFieldRowid(recordDefinition);
      final StringBuilder sqlBuffer = new StringBuilder();
      if (this.sqlPrefix != null) {
        sqlBuffer.append(this.sqlPrefix);
      }
      sqlBuffer.append("insert ");

      sqlBuffer.append(" into ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" (");
      boolean first = true;
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        if (!(hasRowIdField && fieldDefinition.isIdField())) {
          if (first) {
            first = false;
          } else {
            sqlBuffer.append(',');
          }
          fieldDefinition.appendColumnName(sqlBuffer, this.quoteColumnNames);
        }
      }

      sqlBuffer.append(") VALUES (");
      first = true;
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        final boolean idField = fieldDefinition.isIdField();
        if (!(hasRowIdField && idField)) {
          if (first) {
            first = false;
          } else {
            sqlBuffer.append(',');
          }
          if (idField && generatePrimaryKey) {
            final String primaryKeySql = recordStore.getGeneratePrimaryKeySql(recordDefinition);
            sqlBuffer.append(primaryKeySql);
          } else {
            ((JdbcFieldDefinition)fieldDefinition).addInsertStatementPlaceHolder(sqlBuffer,
              generatePrimaryKey);
          }
        }
      }
      sqlBuffer.append(")");
      if (this.sqlSuffix != null) {
        sqlBuffer.append(this.sqlSuffix);
      }
      sql = sqlBuffer.toString();
      sqlMap.put(recordDefinition, sql);
    }
    return sql;
  }

  public String getLabel() {
    return this.label;
  }

  private JdbcRecordDefinition getRecordDefinition(final PathName typePath) {
    if (this.recordStore == null) {
      return null;
    } else {
      final JdbcRecordDefinition recordDefinition = this.recordStore.getRecordDefinition(typePath);
      return recordDefinition;
    }
  }

  private JdbcRecordDefinition getRecordDefinition(final Record record) {
    return getRecordDefinition(record.getPathName());
  }

  public String getSqlPrefix() {
    return this.sqlPrefix;
  }

  public String getSqlSuffix() {
    return this.sqlSuffix;
  }

  private String getUpdateSql(final JdbcRecordDefinition recordDefinition) {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    if (idFields.isEmpty()) {
      throw new RuntimeException("No primary key found for: " + recordDefinition);
    } else {
      final String tableName = recordDefinition.getDbTableQualifiedName();
      String sql = this.typeUpdateSqlMap.get(recordDefinition);
      if (sql == null) {
        final StringBuilder sqlBuffer = new StringBuilder();
        if (this.sqlPrefix != null) {
          sqlBuffer.append(this.sqlPrefix);
        }
        sqlBuffer.append("update ");

        sqlBuffer.append(tableName);
        sqlBuffer.append(" set ");
        boolean first = true;
        for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
          if (!idFields.contains(fieldDefinition)) {
            final JdbcFieldDefinition jdbcFieldDefinition = (JdbcFieldDefinition)fieldDefinition;
            if (first) {
              first = false;
            } else {
              sqlBuffer.append(", ");
            }
            jdbcFieldDefinition.appendColumnName(sqlBuffer, this.quoteColumnNames);
            sqlBuffer.append(" = ");
            jdbcFieldDefinition.addInsertStatementPlaceHolder(sqlBuffer, false);
          }
        }
        sqlBuffer.append(" where ");
        appendIdEquals(sqlBuffer, idFields);

        sqlBuffer.append(" ");
        if (this.sqlSuffix != null) {
          sqlBuffer.append(this.sqlSuffix);
        }
        sql = sqlBuffer.toString();

        this.typeUpdateSqlMap.put(recordDefinition, sql);
      }
      return sql;
    }
  }

  private void insert(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);

    GlobalIdProperty.setIdentifier(record);

    final String idFieldName = recordDefinition.getIdFieldName();
    final boolean hasId = recordDefinition.hasIdField();
    if (this.recordStore.isIdFieldRowid(recordDefinition)) {
      insertRowId(record, recordDefinition);
    } else if (!hasId || record.hasValue(idFieldName)) {
      insert(record, recordDefinition);
    } else {
      insertSequence(record, recordDefinition);
    }
    record.setState(RecordState.PERSISTED);
    this.recordStore.addStatistic("Insert", record);
  }

  private void insert(final Record record, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    final PreparedStatement statement = insertStatementGet(recordDefinition,
      this.typeInsertStatementMap, this.typeInsertSqlMap, false, false);
    if (statement != null) {
      int parameterIndex = 1;
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        parameterIndex = ((JdbcFieldDefinition)fieldDefinition)
          .setInsertPreparedStatementValue(statement, parameterIndex, record);
      }

      insertStatementAddBatch(recordDefinition, record, statement, this.typeInsertSqlMap,
        this.typeInsertBatchCountMap, this.typeInsertRecords, false);
    }
  }

  private void insertRowId(final Record record, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    final PreparedStatement statement = insertStatementGet(recordDefinition,
      this.typeInsertRowIdStatementMap, this.typeInsertRowIdSqlMap, false, true);
    if (statement != null) {
      insertSetValuesNonId(record, recordDefinition, statement);

      insertStatementAddBatch(recordDefinition, record, statement, this.typeInsertRowIdSqlMap,
        this.typeInsertRowIdBatchCountMap, this.typeInsertRowIdRecords, true);
    }
  }

  private void insertSequence(final Record record, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    final PreparedStatement statement = insertStatementGet(recordDefinition,
      this.typeInsertSequenceStatementMap, this.typeInsertSequenceSqlMap, true, true);
    if (statement != null) {
      insertSetValuesNonId(record, recordDefinition, statement);

      insertStatementAddBatch(recordDefinition, record, statement, this.typeInsertSequenceSqlMap,
        this.typeInsertSequenceBatchCountMap, this.typeInsertSequenceRecords, true);
    }
  }

  private void insertSetValuesNonId(final Record record,
    final JdbcRecordDefinition recordDefinition, final PreparedStatement statement)
    throws SQLException {
    int parameterIndex = 1;
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      if (!fieldDefinition.isIdField()) {
        parameterIndex = ((JdbcFieldDefinition)fieldDefinition)
          .setInsertPreparedStatementValue(statement, parameterIndex, record);
      }
    }
  }

  private void insertStatementAddBatch(final JdbcRecordDefinition recordDefinition,
    final Record record, final PreparedStatement statement,
    final Map<JdbcRecordDefinition, String> sqlMap,
    final Map<JdbcRecordDefinition, Integer> countMap,
    final Map<JdbcRecordDefinition, List<Record>> typeRecords, final boolean hasGeneratedKeys)
    throws SQLException {
    statement.addBatch();
    Maps.addToList(typeRecords, recordDefinition, record);
    final Integer batchCount = Maps.addCount(countMap, recordDefinition);
    if (batchCount >= this.batchSize) {
      processCurrentBatch(recordDefinition, sqlMap, statement, countMap, typeRecords,
        hasGeneratedKeys);
    }
  }

  private PreparedStatement insertStatementGet(final JdbcRecordDefinition recordDefinition,
    final Map<JdbcRecordDefinition, PreparedStatement> statementMap,
    final Map<JdbcRecordDefinition, String> sqlMap, final boolean generatePrimaryKey,
    final boolean returnGeneratedKeys) {
    PreparedStatement statement = statementMap.get(recordDefinition);
    if (statement == null) {
      final String sql = getInsertSql(recordDefinition, generatePrimaryKey, sqlMap);
      try {
        if (returnGeneratedKeys) {
          statement = this.recordStore.insertStatementPrepareRowId(this.connection,
            recordDefinition, sql);
        } else {
          statement = this.connection.prepareStatement(sql);
        }
        statementMap.put(recordDefinition, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
        return null;
      }
    }
    return statement;
  }

  public boolean isFlushBetweenTypes() {
    return this.flushBetweenTypes;
  }

  public boolean isQuoteColumnNames() {
    return this.quoteColumnNames;
  }

  public boolean isThrowExceptions() {
    return this.throwExceptions;
  }

  private void processCurrentBatch(final JdbcRecordDefinition recordDefinition,
    final Map<JdbcRecordDefinition, String> sqlMap, final PreparedStatement statement,
    final Map<JdbcRecordDefinition, Integer> batchCountMap,
    final Map<JdbcRecordDefinition, List<Record>> recordsByType, final boolean hasGeneratedKeys) {
    Integer batchCount = batchCountMap.get(recordDefinition);
    if (batchCount == null) {
      batchCount = 0;
    }
    try {
      Integer typeCount = this.typeCountMap.get(recordDefinition);
      if (typeCount == null) {
        typeCount = batchCount;
      } else {
        typeCount += batchCount;
      }
      this.typeCountMap.put(recordDefinition, typeCount);
      statement.executeBatch();

      if (hasGeneratedKeys) {
        final List<Record> records = recordsByType.remove(recordDefinition);
        if (records != null) {
          final ResultSet generatedKeyResultSet = statement.getGeneratedKeys();
          int recordIndex = 0;
          while (generatedKeyResultSet.next()) {
            final Record record = records.get(recordIndex++);
            int columnIndex = 1;
            for (final FieldDefinition idField : recordDefinition.getIdFields()) {
              ((JdbcFieldDefinition)idField).setFieldValueFromResultSet(generatedKeyResultSet,
                columnIndex++, record);
            }
          }
        }
      }
    } catch (final SQLException e) {
      final String sql = sqlMap.get(recordDefinition);
      throw this.connection.getException("Process Batch", sql, e);
    } catch (final RuntimeException e) {
      LOG.error(sqlMap, e);
      throw e;
    } finally {
      batchCountMap.put(recordDefinition, 0);
    }
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  public void setFlushBetweenTypes(final boolean flushBetweenTypes) {
    this.flushBetweenTypes = flushBetweenTypes;
  }

  private int setIdEqualsValues(final PreparedStatement statement, int parameterIndex,
    final JdbcRecordDefinition recordDefinition, final Record record) throws SQLException {
    for (final FieldDefinition idField : recordDefinition.getIdFields()) {
      final Object value = record.getValue(idField);
      parameterIndex = ((JdbcFieldDefinition)idField).setPreparedStatementValue(statement,
        parameterIndex++, value);
    }
    return parameterIndex;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setQuoteColumnNames(final boolean quoteColumnNames) {
    this.quoteColumnNames = quoteColumnNames;
  }

  public void setSqlPrefix(final String sqlPrefix) {
    this.sqlPrefix = sqlPrefix;
  }

  public void setSqlSuffix(final String sqlSuffix) {
    this.sqlSuffix = sqlSuffix;
  }

  public void setThrowExceptions(final boolean throwExceptions) {
    this.throwExceptions = throwExceptions;
  }

  @Override
  public String toString() {
    if (this.recordStore == null) {
      return super.toString();
    } else {
      return this.recordStore.toString() + " writer";
    }
  }

  private void update(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);
    PreparedStatement statement = this.typeUpdateStatementMap.get(recordDefinition);
    if (statement == null) {
      final String sql = getUpdateSql(recordDefinition);
      try {
        statement = this.connection.prepareStatement(sql);
        this.typeUpdateStatementMap.put(recordDefinition, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      if (!fieldDefinition.isIdField()) {
        final JdbcFieldDefinition jdbcFieldDefinition = (JdbcFieldDefinition)fieldDefinition;
        parameterIndex = jdbcFieldDefinition.setInsertPreparedStatementValue(statement,
          parameterIndex, record);
      }
    }
    parameterIndex = setIdEqualsValues(statement, parameterIndex, recordDefinition, record);
    statement.addBatch();
    final Integer batchCount = Maps.addCount(this.typeUpdateBatchCountMap, recordDefinition);
    if (batchCount >= this.batchSize) {
      processCurrentBatch(recordDefinition, this.typeUpdateSqlMap, statement,
        this.typeUpdateBatchCountMap, this.typeUpdateRecords, false);
    }
    this.recordStore.addStatistic("Update", record);
  }

  @Override
  public synchronized void write(final Record record) {
    try {
      final JdbcRecordDefinition recordDefinition = getRecordDefinition(record);
      final RecordStore recordStore = recordDefinition.getRecordStore();
      final RecordState state = record.getState();
      if (recordStore != this.recordStore) {
        if (state != RecordState.DELETED) {
          insert(recordDefinition, record);
        }
      } else {
        switch (state) {
          case NEW:
            insert(recordDefinition, record);
          break;
          case MODIFIED:
            update(recordDefinition, record);
          break;
          case PERSISTED:
          // No action required
          break;
          case DELETED:
            delete(recordDefinition, record);
          break;
          default:
            throw new IllegalStateException("State not known");
        }
      }
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final BatchUpdateException e) {
      for (SQLException e1 = e.getNextException(); e1 != null; e1 = e1.getNextException()) {
        LOG.error("Unable to write", e1);
      }
      throw new RuntimeException("Unable to write", e);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to write", e);
    }
  }
}
