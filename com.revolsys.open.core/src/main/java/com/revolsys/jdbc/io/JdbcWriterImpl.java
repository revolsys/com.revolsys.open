package com.revolsys.jdbc.io;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

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
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.count.CategoryLabelCountMap;

public class JdbcWriterImpl extends AbstractRecordWriter {
  private static final Logger LOG = Logger.getLogger(JdbcWriterImpl.class);

  private int batchSize = 1;

  private JdbcConnection connection;

  private boolean flushBetweenTypes = false;

  private String hints = null;

  private String label;

  private RecordDefinition lastRecordDefinition;

  private boolean quoteColumnNames = true;

  private JdbcRecordStore recordStore;

  private String sqlPrefix;

  private String sqlSuffix;

  private CategoryLabelCountMap statistics;

  private boolean throwExceptions = false;

  private final Map<PathName, Integer> typeCountMap = new LinkedHashMap<>();

  private Map<PathName, Integer> typeDeleteBatchCountMap = new LinkedHashMap<>();

  private Map<PathName, String> typeDeleteSqlMap = new LinkedHashMap<>();

  private Map<PathName, PreparedStatement> typeDeleteStatementMap = new LinkedHashMap<>();

  private Map<PathName, Integer> typeInsertBatchCountMap = new LinkedHashMap<>();

  private Map<PathName, Integer> typeInsertSequenceBatchCountMap = new LinkedHashMap<>();

  private Map<PathName, String> typeInsertSequenceSqlMap = new LinkedHashMap<>();

  private Map<PathName, PreparedStatement> typeInsertSequenceStatementMap = new LinkedHashMap<>();

  private Map<PathName, String> typeInsertSqlMap = new LinkedHashMap<>();

  private Map<PathName, PreparedStatement> typeInsertStatementMap = new LinkedHashMap<>();

  private Map<PathName, Integer> typeUpdateBatchCountMap = new LinkedHashMap<>();

  private Map<PathName, String> typeUpdateSqlMap = new LinkedHashMap<>();

  private Map<PathName, PreparedStatement> typeUpdateStatementMap = new LinkedHashMap<>();

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

  private void addSqlColumEqualsPlaceholder(final StringBuilder sqlBuffer,
    final JdbcFieldDefinition attribute) {
    final String fieldName = attribute.getName();
    if (this.quoteColumnNames) {
      sqlBuffer.append('"').append(fieldName).append('"');
    } else {
      sqlBuffer.append(fieldName);
    }
    sqlBuffer.append(" = ");
    attribute.addInsertStatementPlaceHolder(sqlBuffer, false);
  }

  @Override
  @PreDestroy
  public void close() {
    flush();
    closeDo();
  }

  private void close(final Map<PathName, String> sqlMap,
    final Map<PathName, PreparedStatement> statementMap,
    final Map<PathName, Integer> batchCountMap) {
    for (final Entry<PathName, PreparedStatement> entry : statementMap.entrySet()) {
      final PathName typePath = entry.getKey();
      final PreparedStatement statement = entry.getValue();
      final String sql = sqlMap.get(typePath);
      try {
        processCurrentBatch(typePath, sql, statement, batchCountMap);
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

        close(this.typeInsertSqlMap, this.typeInsertStatementMap, this.typeInsertBatchCountMap);
        close(this.typeInsertSequenceSqlMap, this.typeInsertSequenceStatementMap,
          this.typeInsertSequenceBatchCountMap);
        close(this.typeUpdateSqlMap, this.typeUpdateStatementMap, this.typeUpdateBatchCountMap);
        close(this.typeDeleteSqlMap, this.typeDeleteStatementMap, this.typeDeleteBatchCountMap);
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

  private void delete(final Record object) throws SQLException {
    final RecordDefinition objectType = object.getRecordDefinition();
    final PathName typePath = objectType.getPathName();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    flushIfRequired(recordDefinition);
    PreparedStatement statement = this.typeDeleteStatementMap.get(typePath);
    if (statement == null) {
      final String sql = getDeleteSql(recordDefinition);
      try {
        statement = this.connection.prepareStatement(sql);
        this.typeDeleteStatementMap.put(typePath, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final JdbcFieldDefinition idField = (JdbcFieldDefinition)recordDefinition.getIdField();
    parameterIndex = idField.setInsertPreparedStatementValue(statement, parameterIndex, object);
    statement.addBatch();
    Integer batchCount = this.typeDeleteBatchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 1;
      this.typeDeleteBatchCountMap.put(typePath, 1);
    } else {
      batchCount += 1;
      this.typeDeleteBatchCountMap.put(typePath, batchCount);
    }
    this.recordStore.addStatistic("Delete", object);

    // TODO this locks code tables which prevents insert
    // if (batchCount >= batchSize) {
    // final String sql = getDeleteSql(recordDefinition);
    // processCurrentBatch(typePath, sql, statement, typeDeleteBatchCountMap,
    // getDeleteStatistics());
    // }
  }

  @Override
  public synchronized void flush() {
    flush(this.typeInsertSqlMap, this.typeInsertStatementMap, this.typeInsertBatchCountMap);
    flush(this.typeInsertSequenceSqlMap, this.typeInsertSequenceStatementMap,
      this.typeInsertSequenceBatchCountMap);
    flush(this.typeUpdateSqlMap, this.typeUpdateStatementMap, this.typeUpdateBatchCountMap);
    flush(this.typeDeleteSqlMap, this.typeDeleteStatementMap, this.typeDeleteBatchCountMap);
  }

  private void flush(final Map<PathName, String> sqlMap,
    final Map<PathName, PreparedStatement> statementMap,
    final Map<PathName, Integer> batchCountMap) {
    if (statementMap != null) {
      for (final Entry<PathName, PreparedStatement> entry : statementMap.entrySet()) {
        final PathName typePath = entry.getKey();
        final PreparedStatement statement = entry.getValue();
        final String sql = sqlMap.get(typePath);
        try {
          processCurrentBatch(typePath, sql, statement, batchCountMap);
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

  private void flushIfRequired(final RecordDefinition recordDefinition) {
    if (this.flushBetweenTypes && recordDefinition != this.lastRecordDefinition) {
      flush();
      this.lastRecordDefinition = recordDefinition;
    }
  }

  public int getBatchSize() {
    return this.batchSize;
  }

  private String getDeleteSql(final RecordDefinition type) {
    final PathName typePath = type.getPathName();
    final String tableName = this.recordStore.getDatabaseQualifiedTableName(typePath);
    String sql = this.typeDeleteSqlMap.get(typePath);
    if (sql == null) {
      final StringBuilder sqlBuffer = new StringBuilder();
      if (this.sqlPrefix != null) {
        sqlBuffer.append(this.sqlPrefix);
      }
      sqlBuffer.append("delete ");
      if (this.hints != null) {
        sqlBuffer.append(this.hints);
      }
      sqlBuffer.append(" from ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" where ");
      final JdbcFieldDefinition idField = (JdbcFieldDefinition)type.getIdField();
      if (idField == null) {
        throw new RuntimeException("No primary key found for " + type);
      }
      addSqlColumEqualsPlaceholder(sqlBuffer, idField);

      sqlBuffer.append(" ");
      if (this.sqlSuffix != null) {
        sqlBuffer.append(this.sqlSuffix);
      }
      sql = sqlBuffer.toString();

      this.typeDeleteSqlMap.put(typePath, sql);
    }
    return sql;
  }

  private String getGeneratePrimaryKeySql(final RecordDefinition recordDefinition) {
    return this.recordStore.getGeneratePrimaryKeySql(recordDefinition);
  }

  /**
   * @return the hints
   */
  public String getHints() {
    return this.hints;
  }

  private String getInsertSql(final RecordDefinition type, final boolean generatePrimaryKey) {
    final PathName typePath = type.getPathName();
    final String tableName = this.recordStore.getDatabaseQualifiedTableName(typePath);
    String sql;
    if (generatePrimaryKey) {
      sql = this.typeInsertSequenceSqlMap.get(typePath);
    } else {
      sql = this.typeInsertSqlMap.get(typePath);
    }
    if (sql == null) {
      final StringBuilder sqlBuffer = new StringBuilder();
      if (this.sqlPrefix != null) {
        sqlBuffer.append(this.sqlPrefix);
      }
      sqlBuffer.append("insert ");
      if (this.hints != null) {
        sqlBuffer.append(this.hints);
      }
      sqlBuffer.append(" into ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" (");
      if (generatePrimaryKey) {
        final String idFieldName = type.getIdFieldName();
        if (this.quoteColumnNames) {
          sqlBuffer.append('"').append(idFieldName).append('"');
        } else {
          sqlBuffer.append(idFieldName);
        }
        sqlBuffer.append(",");
      }
      for (int i = 0; i < type.getFieldCount(); i++) {
        if (!generatePrimaryKey || i != type.getIdFieldIndex()) {
          final String fieldName = type.getFieldName(i);
          if (this.quoteColumnNames) {
            sqlBuffer.append('"').append(fieldName).append('"');
          } else {
            sqlBuffer.append(fieldName);
          }
          if (i < type.getFieldCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(") VALUES (");
      if (generatePrimaryKey) {
        sqlBuffer.append(getGeneratePrimaryKeySql(type));
        sqlBuffer.append(",");
      }
      for (int i = 0; i < type.getFieldCount(); i++) {
        if (!generatePrimaryKey || i != type.getIdFieldIndex()) {
          final JdbcFieldDefinition attribute = (JdbcFieldDefinition)type.getField(i);
          attribute.addInsertStatementPlaceHolder(sqlBuffer, generatePrimaryKey);
          if (i < type.getFieldCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(")");
      if (this.sqlSuffix != null) {
        sqlBuffer.append(this.sqlSuffix);
      }
      sql = sqlBuffer.toString();
      if (generatePrimaryKey) {
        this.typeInsertSequenceSqlMap.put(typePath, sql);
      } else {
        this.typeInsertSqlMap.put(typePath, sql);
      }
    }
    return sql;
  }

  public String getLabel() {
    return this.label;
  }

  private RecordDefinition getRecordDefinition(final PathName typePath) {
    if (this.recordStore == null) {
      return null;
    } else {
      final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(typePath);
      return recordDefinition;
    }
  }

  public String getSqlPrefix() {
    return this.sqlPrefix;
  }

  public String getSqlSuffix() {
    return this.sqlSuffix;
  }

  private String getUpdateSql(final RecordDefinition type) {
    final PathName typePath = type.getPathName();
    final String tableName = this.recordStore.getDatabaseQualifiedTableName(typePath);
    String sql = this.typeUpdateSqlMap.get(typePath);
    if (sql == null) {
      final StringBuilder sqlBuffer = new StringBuilder();
      if (this.sqlPrefix != null) {
        sqlBuffer.append(this.sqlPrefix);
      }
      sqlBuffer.append("update ");
      if (this.hints != null) {
        sqlBuffer.append(this.hints);
      }
      sqlBuffer.append(tableName);
      sqlBuffer.append(" set ");
      final List<FieldDefinition> idFields = type.getIdFields();
      boolean first = true;
      for (final FieldDefinition attribute : type.getFields()) {
        if (!idFields.contains(attribute)) {
          final JdbcFieldDefinition jdbcAttribute = (JdbcFieldDefinition)attribute;
          if (first) {
            first = false;
          } else {
            sqlBuffer.append(", ");
          }
          addSqlColumEqualsPlaceholder(sqlBuffer, jdbcAttribute);
        }
      }
      sqlBuffer.append(" where ");
      first = true;
      for (final FieldDefinition idField : idFields) {
        if (first) {
          first = false;
        } else {
          sqlBuffer.append(" AND ");
        }
        final JdbcFieldDefinition idJdbcAttribute = (JdbcFieldDefinition)idField;
        addSqlColumEqualsPlaceholder(sqlBuffer, idJdbcAttribute);
      }

      sqlBuffer.append(" ");
      if (this.sqlSuffix != null) {
        sqlBuffer.append(this.sqlSuffix);
      }
      sql = sqlBuffer.toString();

      this.typeUpdateSqlMap.put(typePath, sql);
    }
    return sql;
  }

  private void insert(final Record object) throws SQLException {
    final RecordDefinition objectType = object.getRecordDefinition();
    final PathName typePath = objectType.getPathName();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    flushIfRequired(recordDefinition);
    final String idFieldName = recordDefinition.getIdFieldName();
    final boolean hasId = idFieldName != null;

    final GlobalIdProperty globalIdProperty = GlobalIdProperty.getProperty(object);
    if (globalIdProperty != null) {
      if (object.getValue(globalIdProperty.getFieldName()) == null) {
        object.setValue(globalIdProperty.getFieldName(), UUID.randomUUID().toString());
      }
    }

    final boolean hasIdValue = hasId && object.getValue(idFieldName) != null;

    if (!hasId || hasIdValue) {
      insert(object, typePath, recordDefinition);
    } else {
      insertSequence(object, typePath, recordDefinition);
    }
    object.setState(RecordState.PERSISTED);
    this.recordStore.addStatistic("Insert", object);
  }

  private void insert(final Record object, final PathName typePath,
    final RecordDefinition recordDefinition) throws SQLException {
    PreparedStatement statement = this.typeInsertStatementMap.get(typePath);
    if (statement == null) {
      final String sql = getInsertSql(recordDefinition, false);
      try {
        statement = this.connection.prepareStatement(sql);
        this.typeInsertStatementMap.put(typePath, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    for (final FieldDefinition attribute : recordDefinition.getFields()) {
      final JdbcFieldDefinition jdbcAttribute = (JdbcFieldDefinition)attribute;
      parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(statement, parameterIndex,
        object);
    }
    statement.addBatch();
    Integer batchCount = this.typeInsertBatchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 1;
      this.typeInsertBatchCountMap.put(typePath, 1);
    } else {
      batchCount += 1;
      this.typeInsertBatchCountMap.put(typePath, batchCount);
    }
    if (batchCount >= this.batchSize) {
      final String sql = getInsertSql(recordDefinition, false);
      processCurrentBatch(typePath, sql, statement, this.typeInsertBatchCountMap);
    }
  }

  private void insertSequence(final Record object, final PathName typePath,
    final RecordDefinition recordDefinition) throws SQLException {
    PreparedStatement statement = this.typeInsertSequenceStatementMap.get(typePath);
    if (statement == null) {
      final String sql = getInsertSql(recordDefinition, true);
      try {
        statement = this.connection.prepareStatement(sql);
        this.typeInsertSequenceStatementMap.put(typePath, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final FieldDefinition idField = recordDefinition.getIdField();
    for (final FieldDefinition field : recordDefinition.getFields()) {
      if (field != idField) {
        final JdbcFieldDefinition jdbcAttribute = (JdbcFieldDefinition)field;
        parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(statement, parameterIndex,
          object);
      }
    }
    statement.addBatch();
    Integer batchCount = this.typeInsertSequenceBatchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 1;
      this.typeInsertSequenceBatchCountMap.put(typePath, 1);
    } else {
      batchCount += 1;
      this.typeInsertSequenceBatchCountMap.put(typePath, batchCount);
    }
    if (batchCount >= this.batchSize) {
      final String sql = getInsertSql(recordDefinition, true);
      processCurrentBatch(typePath, sql, statement, this.typeInsertSequenceBatchCountMap);
    }
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

  private void processCurrentBatch(final PathName typePath, final String sql,
    final PreparedStatement statement, final Map<PathName, Integer> batchCountMap) {
    Integer batchCount = batchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 0;
    }
    try {
      Integer typeCount = this.typeCountMap.get(typePath);
      if (typeCount == null) {
        typeCount = batchCount;
      } else {
        typeCount += batchCount;
      }
      this.typeCountMap.put(typePath, typeCount);
      statement.executeBatch();
    } catch (final SQLException e) {
      throw this.connection.getException("Process Batch", sql, e);
    } catch (final RuntimeException e) {
      LOG.error(sql, e);
      throw e;
    } finally {
      batchCountMap.put(typePath, 0);
    }
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  public void setFlushBetweenTypes(final boolean flushBetweenTypes) {
    this.flushBetweenTypes = flushBetweenTypes;
  }

  /**
   * @param hints the hints to set
   */
  public void setHints(final String hints) {
    this.hints = hints;
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

  private void update(final Record object) throws SQLException {
    final RecordDefinition objectType = object.getRecordDefinition();
    final PathName typePath = objectType.getPathName();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    flushIfRequired(recordDefinition);
    PreparedStatement statement = this.typeUpdateStatementMap.get(typePath);
    if (statement == null) {
      final String sql = getUpdateSql(recordDefinition);
      try {
        statement = this.connection.prepareStatement(sql);
        this.typeUpdateStatementMap.put(typePath, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
      if (!idFields.contains(fieldDefinition)) {
        final JdbcFieldDefinition jdbcFieldDefinition = (JdbcFieldDefinition)fieldDefinition;
        parameterIndex = jdbcFieldDefinition.setInsertPreparedStatementValue(statement,
          parameterIndex, object);
      }
    }
    for (final FieldDefinition idField : idFields) {
      final JdbcFieldDefinition jdbcFieldDefinition = (JdbcFieldDefinition)idField;
      parameterIndex = jdbcFieldDefinition.setInsertPreparedStatementValue(statement,
        parameterIndex, object);

    }
    statement.addBatch();
    Integer batchCount = this.typeUpdateBatchCountMap.get(typePath);
    if (batchCount == null) {
      batchCount = 1;
      this.typeUpdateBatchCountMap.put(typePath, 1);
    } else {
      batchCount += 1;
      this.typeUpdateBatchCountMap.put(typePath, batchCount);
    }
    if (batchCount >= this.batchSize) {
      final String sql = getUpdateSql(recordDefinition);
      processCurrentBatch(typePath, sql, statement, this.typeUpdateBatchCountMap);
    }
    this.recordStore.addStatistic("Update", object);
  }

  @Override
  public synchronized void write(final Record object) {
    try {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      final RecordStore recordStore = recordDefinition.getRecordStore();
      final RecordState state = object.getState();
      if (recordStore != this.recordStore) {
        if (state != RecordState.DELETED) {
          insert(object);
        }
      } else {
        switch (state) {
          case NEW:
            insert(object);
          break;
          case MODIFIED:
            update(object);
          break;
          case PERSISTED:
          // No action required
          break;
          case DELETED:
            delete(object);
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
