package com.revolsys.jdbc.io;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;
import org.springframework.dao.DataAccessException;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.property.GlobalIdProperty;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.count.CategoryLabelCountMap;

public abstract class AbstractJdbcRecordWriter extends AbstractRecordWriter
  implements JdbcRecordWriter {

  protected JdbcConnection connection;

  private boolean flushBetweenTypes = false;

  private String label;

  private JdbcRecordDefinition lastRecordDefinition;

  private boolean quoteColumnNames = true;

  private JdbcRecordStore recordStore;

  private String sqlPrefix;

  private String sqlSuffix;

  private CategoryLabelCountMap statistics;

  private boolean throwExceptions = false;

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeDeleteData = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeInsertData = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeInsertSequenceData = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeInsertRowIdData = new LinkedHashMap<>();

  private Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeUpdateData = new LinkedHashMap<>();

  public AbstractJdbcRecordWriter(final JdbcRecordStore recordStore) {
    this(recordStore, null, recordStore.getStatistics());
  }

  public AbstractJdbcRecordWriter(final JdbcRecordStore recordStore,
    final CategoryLabelCountMap statistics) {
    this(recordStore, null, statistics);
  }

  public AbstractJdbcRecordWriter(final JdbcRecordStore recordStore,
    final RecordDefinitionProxy recordDefinition) {
    this(recordStore, recordDefinition, recordStore.getStatistics());
  }

  public AbstractJdbcRecordWriter(final JdbcRecordStore recordStore,
    final RecordDefinitionProxy recordDefinition, final CategoryLabelCountMap statistics) {
    super(recordDefinition);
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
    if (statistics != null) {
      statistics.connect();
    }
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
    if (this.statistics != null) {
      this.statistics.disconnect();
    }
  }

  private void close(final Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeDataMap) {
    for (final JdbcRecordWriterTypeData typeData : typeDataMap.values()) {
      try {
        closeTypeData(typeData);
      } catch (final DataAccessException e) {
        if (this.throwExceptions) {
          throw e;
        } else {
          Logs.error(this, "Error commiting records", e);
        }
      } finally {
        JdbcUtils.close(typeData.getStatement());
      }
    }
  }

  protected synchronized void closeDo() {
    if (this.recordStore != null) {
      try {
        close(this.typeInsertData);

        close(this.typeInsertData);

        close(this.typeInsertRowIdData);

        close(this.typeUpdateData);

        close(this.typeDeleteData);

        if (this.statistics != null) {
          this.statistics.disconnect();
          this.statistics = null;
        }
      } finally {
        this.typeInsertData = null;
        this.typeInsertSequenceData = null;
        this.typeInsertRowIdData = null;
        this.typeUpdateData = null;
        this.typeDeleteData = null;
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

  protected void closeTypeData(final JdbcRecordWriterTypeData typeData) {
  }

  public synchronized void commit() {
    flush();
    JdbcUtils.commit(this.connection);
  }

  private void deleteRecord(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);
    JdbcRecordWriterTypeData data = this.typeDeleteData.get(recordDefinition);
    if (data == null) {
      final String sql = getDeleteSql(recordDefinition);
      try {
        final PreparedStatement statement = this.connection.prepareStatement(sql);
        data = new JdbcRecordWriterTypeData(recordDefinition, sql, statement, false);
        this.typeDeleteData.put(recordDefinition, data);
      } catch (final SQLException e) {
        Logs.error(this, sql, e);
        return;
      }
    }
    final PreparedStatement statement = data.getStatement();
    setIdEqualsValues(statement, 1, recordDefinition, record);
    deleteRecordDo(data, statement);
    this.recordStore.addStatistic("Delete", record);
  }

  protected abstract void deleteRecordDo(final JdbcRecordWriterTypeData data,
    final PreparedStatement statement) throws SQLException;

  @Override
  public synchronized void flush() {
    flush(this.typeInsertData);

    flush(this.typeInsertSequenceData);

    flush(this.typeInsertRowIdData);

    flush(this.typeUpdateData);

    flush(this.typeDeleteData);
  }

  private void flush(final Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeDataMap) {
    if (typeDataMap != null) {
      for (final JdbcRecordWriterTypeData data : typeDataMap.values()) {
        try {
          flushDo(data);
        } catch (final DataAccessException e) {
          if (this.throwExceptions) {
            throw e;
          } else {
            Logs.error(this, "Error writing to database", e);
          }
        }
      }
    }
  }

  protected void flushDo(final JdbcRecordWriterTypeData data) {
  }

  private void flushIfRequired(final JdbcRecordDefinition recordDefinition) {
    if (this.flushBetweenTypes && recordDefinition != this.lastRecordDefinition) {
      flush();
      this.lastRecordDefinition = recordDefinition;
    }
  }

  private String getDeleteSql(final JdbcRecordDefinition recordDefinition) {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    if (idFields.isEmpty()) {
      throw new RuntimeException("No primary key found for: " + recordDefinition);
    } else {
      final String tableName = recordDefinition.getDbTableQualifiedName();
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
      return sqlBuffer.toString();
    }
  }

  private String getInsertSql(final JdbcRecordDefinition recordDefinition,
    final boolean generatePrimaryKey) {
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
    return sqlBuffer.toString();
  }

  @Override
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

  @Override
  public String getSqlPrefix() {
    return this.sqlPrefix;
  }

  @Override
  public String getSqlSuffix() {
    return this.sqlSuffix;
  }

  private String getUpdateSql(final JdbcRecordDefinition recordDefinition) {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    if (idFields.isEmpty()) {
      throw new RuntimeException("No primary key found for: " + recordDefinition);
    } else {
      final String tableName = recordDefinition.getDbTableQualifiedName();
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
      return sqlBuffer.toString();

    }
  }

  protected void insert(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);

    GlobalIdProperty.setIdentifier(record);

    final boolean hasId = recordDefinition.hasIdField();
    if (this.recordStore.isIdFieldRowid(recordDefinition)) {
      insertRowId(record, recordDefinition);
    } else if (!hasId) {
      insert(record, recordDefinition);
    } else {
      boolean hasIdValue = true;
      for (final String idFieldName : recordDefinition.getIdFieldNames()) {
        if (!record.hasValue(idFieldName)) {
          hasIdValue = false;
        }
      }
      if (hasIdValue) {
        insert(record, recordDefinition);
      } else {
        insertRecordSequence(record, recordDefinition);
      }
    }
    record.setState(RecordState.PERSISTED);
    this.recordStore.addStatistic("Insert", record);
  }

  protected void insert(final Record record, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    final JdbcRecordWriterTypeData data = insertStatementGet(recordDefinition, this.typeInsertData,
      false, false);
    if (data != null) {
      final PreparedStatement statement = data.getStatement();
      int parameterIndex = 1;
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        parameterIndex = ((JdbcFieldDefinition)fieldDefinition)
          .setInsertPreparedStatementValue(statement, parameterIndex, record);
      }

      insertRecordDo(data, statement, recordDefinition, record);
    }
  }

  protected abstract void insertRecordDo(JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final JdbcRecordDefinition recordDefinition,
    final Record record) throws SQLException;

  protected abstract void insertRecordRowIdDo(JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final Record record,
    final JdbcRecordDefinition recordDefinition) throws SQLException;

  private void insertRecordSequence(final Record record,
    final JdbcRecordDefinition recordDefinition) throws SQLException {
    final JdbcRecordWriterTypeData data = insertStatementGet(recordDefinition,
      this.typeInsertSequenceData, true, true);
    if (data != null) {
      final PreparedStatement statement = data.getStatement();
      insertSetValuesNonId(record, recordDefinition, statement);
      insertRecordSequenceDo(data, statement, record, recordDefinition);
    }
  }

  protected abstract void insertRecordSequenceDo(JdbcRecordWriterTypeData data,
    final PreparedStatement statement, final Record record,
    final JdbcRecordDefinition recordDefinition) throws SQLException;

  private void insertRowId(final Record record, final JdbcRecordDefinition recordDefinition)
    throws SQLException {
    final JdbcRecordWriterTypeData data = insertStatementGet(recordDefinition,
      this.typeInsertRowIdData, false, true);
    if (data != null) {
      final PreparedStatement statement = data.getStatement();
      insertSetValuesNonId(record, recordDefinition, statement);

      insertRecordRowIdDo(data, statement, record, recordDefinition);
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

  private JdbcRecordWriterTypeData insertStatementGet(final JdbcRecordDefinition recordDefinition,
    final Map<JdbcRecordDefinition, JdbcRecordWriterTypeData> typeDataMap,
    final boolean generatePrimaryKey, final boolean returnGeneratedKeys) {

    JdbcRecordWriterTypeData data = typeDataMap.get(recordDefinition);
    if (data == null) {
      final String sql = getInsertSql(recordDefinition, generatePrimaryKey);
      try {
        PreparedStatement statement;
        if (returnGeneratedKeys) {
          statement = this.recordStore.insertStatementPrepareRowId(this.connection,
            recordDefinition, sql);
        } else {
          statement = this.connection.prepareStatement(sql);
        }
        data = new JdbcRecordWriterTypeData(recordDefinition, sql, statement, returnGeneratedKeys);
        typeDataMap.put(recordDefinition, data);
      } catch (final SQLException e) {
        Logs.error(this, sql, e);
        return null;
      }
    }
    return data;
  }

  @Override
  public boolean isFlushBetweenTypes() {
    return this.flushBetweenTypes;
  }

  @Override
  public boolean isQuoteColumnNames() {
    return this.quoteColumnNames;
  }

  @Override
  public boolean isThrowExceptions() {
    return this.throwExceptions;
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

  @Override
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

  private void updateRecord(final JdbcRecordDefinition recordDefinition, final Record record)
    throws SQLException {
    flushIfRequired(recordDefinition);
    JdbcRecordWriterTypeData data = this.typeUpdateData.get(recordDefinition);
    if (data == null) {
      final String sql = getUpdateSql(recordDefinition);
      try {
        final PreparedStatement statement = this.connection.prepareStatement(sql);
        data = new JdbcRecordWriterTypeData(recordDefinition, sql, statement, false);
        this.typeUpdateData.put(recordDefinition, data);
      } catch (final SQLException e) {
        Logs.error(this, sql, e);
      }
    }
    final PreparedStatement statement = data.getStatement();
    int parameterIndex = 1;
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      if (!fieldDefinition.isIdField()) {
        final JdbcFieldDefinition jdbcFieldDefinition = (JdbcFieldDefinition)fieldDefinition;
        parameterIndex = jdbcFieldDefinition.setInsertPreparedStatementValue(statement,
          parameterIndex, record);
      }
    }
    parameterIndex = setIdEqualsValues(statement, parameterIndex, recordDefinition, record);
    updateRecordDo(data, statement, recordDefinition);
    this.recordStore.addStatistic("Update", record);
  }

  protected abstract void updateRecordDo(JdbcRecordWriterTypeData data, PreparedStatement statement,
    final JdbcRecordDefinition recordDefinition) throws SQLException;

  @Override
  public synchronized void write(final Record record) {
    try {
      final JdbcRecordDefinition recordDefinition = getRecordDefinition(record);
      final RecordState state = record.getState();
      if (record.getRecordStore() != this.recordStore) {
        if (state != RecordState.DELETED) {
          insert(recordDefinition, record);
        }
      } else {
        switch (state) {
          case NEW:
            insert(recordDefinition, record);
          break;
          case MODIFIED:
            updateRecord(recordDefinition, record);
          break;
          case PERSISTED:
          // No action required
          break;
          case DELETED:
            deleteRecord(recordDefinition, record);
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
        Logs.error(this, "Unable to write", e1);
      }
      throw new RuntimeException("Unable to write", e);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to write", e);
    }
  }
}
