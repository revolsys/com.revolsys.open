package com.revolsys.jdbc.io;

import java.sql.BatchUpdateException;
import java.sql.Connection;
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

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.property.GlobalIdProperty;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.transaction.Transaction;

public class JdbcWriterImpl extends AbstractRecordWriter implements JdbcWriter {
  private static final Logger LOG = Logger.getLogger(JdbcWriterImpl.class);

  private int batchSize = 1;

  private Connection connection;

  private DataSource dataSource;

  private JdbcRecordStore recordStore;

  private boolean flushBetweenTypes = false;

  private String hints = null;

  private String label;

  private RecordDefinition lastMetaData;

  private boolean quoteColumnNames = true;

  private String sqlPrefix;

  private String sqlSuffix;

  private final Map<String, Integer> typeCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, Integer> typeDeleteBatchCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, String> typeDeleteSqlMap = new LinkedHashMap<String, String>();

  private Map<String, PreparedStatement> typeDeleteStatementMap = new LinkedHashMap<String, PreparedStatement>();

  private Map<String, Integer> typeInsertBatchCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, Integer> typeInsertSequenceBatchCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, String> typeInsertSequenceSqlMap = new LinkedHashMap<String, String>();

  private Map<String, PreparedStatement> typeInsertSequenceStatementMap = new LinkedHashMap<String, PreparedStatement>();

  private Map<String, String> typeInsertSqlMap = new LinkedHashMap<String, String>();

  private Map<String, PreparedStatement> typeInsertStatementMap = new LinkedHashMap<String, PreparedStatement>();

  private Map<String, Integer> typeUpdateBatchCountMap = new LinkedHashMap<String, Integer>();

  private Map<String, String> typeUpdateSqlMap = new LinkedHashMap<String, String>();

  private Map<String, PreparedStatement> typeUpdateStatementMap = new LinkedHashMap<String, PreparedStatement>();

  private StatisticsMap statistics;

  private boolean throwExceptions = false;

  public JdbcWriterImpl(final JdbcRecordStore recordStore) {
    this(recordStore, recordStore.getStatistics());
  }

  public JdbcWriterImpl(final JdbcRecordStore recordStore,
    final StatisticsMap statistics) {
    this.recordStore = recordStore;
    this.statistics = statistics;
    setConnection(recordStore.getConnection());
    setDataSource(recordStore.getDataSource());
    statistics.connect();
  }

  private void addSqlColumEqualsPlaceholder(final StringBuffer sqlBuffer,
    final JdbcAttribute attribute) {
    final String attributeName = attribute.getName();
    if (this.quoteColumnNames) {
      sqlBuffer.append('"').append(attributeName).append('"');
    } else {
      sqlBuffer.append(attributeName);
    }
    sqlBuffer.append(" = ");
    attribute.addInsertStatementPlaceHolder(sqlBuffer, false);
  }

  @Override
  @PreDestroy
  public void close() {
    flush();
    doClose();
  }

  private void close(final Map<String, String> sqlMap,
    final Map<String, PreparedStatement> statementMap,
    final Map<String, Integer> batchCountMap) {
    for (final Entry<String, PreparedStatement> entry : statementMap.entrySet()) {
      final String typePath = entry.getKey();
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

  public synchronized void commit() {
    flush();
    JdbcUtils.commit(this.connection);
  }

  private void delete(final Record object) throws SQLException {
    final RecordDefinition objectType = object.getRecordDefinition();
    final String typePath = objectType.getPath();
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
    final JdbcAttribute idAttribute = (JdbcAttribute)recordDefinition.getIdAttribute();
    parameterIndex = idAttribute.setInsertPreparedStatementValue(statement,
      parameterIndex, object);
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

  protected synchronized void doClose() {
    if (this.recordStore != null) {
      try {

        close(this.typeInsertSqlMap, this.typeInsertStatementMap,
          this.typeInsertBatchCountMap);
        close(this.typeInsertSequenceSqlMap,
          this.typeInsertSequenceStatementMap,
          this.typeInsertSequenceBatchCountMap);
        close(this.typeUpdateSqlMap, this.typeUpdateStatementMap,
          this.typeUpdateBatchCountMap);
        close(this.typeDeleteSqlMap, this.typeDeleteStatementMap,
          this.typeDeleteBatchCountMap);
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
        if (this.dataSource != null) {
          try {
            if (!Transaction.isHasCurrentTransaction()) {
              this.connection.commit();
            }
          } catch (final SQLException e) {
            throw new RuntimeException("Failed to commit data:", e);
          } finally {
            JdbcUtils.release(this.connection, this.dataSource);
            this.dataSource = null;
            this.connection = null;
          }
        }
      }
    }
  }

  @Override
  public synchronized void flush() {
    flush(this.typeInsertSqlMap, this.typeInsertStatementMap,
      this.typeInsertBatchCountMap);
    flush(this.typeInsertSequenceSqlMap, this.typeInsertSequenceStatementMap,
      this.typeInsertSequenceBatchCountMap);
    flush(this.typeUpdateSqlMap, this.typeUpdateStatementMap,
      this.typeUpdateBatchCountMap);
    flush(this.typeDeleteSqlMap, this.typeDeleteStatementMap,
      this.typeDeleteBatchCountMap);
  }

  private void flush(final Map<String, String> sqlMap,
    final Map<String, PreparedStatement> statementMap,
    final Map<String, Integer> batchCountMap) {
    if (statementMap != null) {
      for (final Entry<String, PreparedStatement> entry : statementMap.entrySet()) {
        final String typePath = entry.getKey();
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
    if (this.flushBetweenTypes && recordDefinition != this.lastMetaData) {
      flush();
      this.lastMetaData = recordDefinition;
    }
  }

  public int getBatchSize() {
    return this.batchSize;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  private String getDeleteSql(final RecordDefinition type) {
    final String typePath = type.getPath();
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    String sql = this.typeDeleteSqlMap.get(typePath);
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
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
      final JdbcAttribute idAttribute = (JdbcAttribute)type.getIdAttribute();
      if (idAttribute == null) {
        throw new RuntimeException("No primary key found for " + type);
      }
      addSqlColumEqualsPlaceholder(sqlBuffer, idAttribute);

      sqlBuffer.append(" ");
      if (this.sqlSuffix != null) {
        sqlBuffer.append(this.sqlSuffix);
      }
      sql = sqlBuffer.toString();

      this.typeDeleteSqlMap.put(typePath, sql);
    }
    return sql;
  }

  private String getGeneratePrimaryKeySql(
    final RecordDefinition recordDefinition) {
    return this.recordStore.getGeneratePrimaryKeySql(recordDefinition);
  }

  /**
   * @return the hints
   */
  public String getHints() {
    return this.hints;
  }

  private String getInsertSql(final RecordDefinition type,
    final boolean generatePrimaryKey) {
    final String typePath = type.getPath();
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    String sql;
    if (generatePrimaryKey) {
      sql = this.typeInsertSequenceSqlMap.get(typePath);
    } else {
      sql = this.typeInsertSqlMap.get(typePath);
    }
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
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
        final String idAttributeName = type.getIdAttributeName();
        if (this.quoteColumnNames) {
          sqlBuffer.append('"').append(idAttributeName).append('"');
        } else {
          sqlBuffer.append(idAttributeName);
        }
        sqlBuffer.append(",");
      }
      for (int i = 0; i < type.getAttributeCount(); i++) {
        if (!generatePrimaryKey || i != type.getIdAttributeIndex()) {
          final String attributeName = type.getAttributeName(i);
          if (this.quoteColumnNames) {
            sqlBuffer.append('"').append(attributeName).append('"');
          } else {
            sqlBuffer.append(attributeName);
          }
          if (i < type.getAttributeCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(") VALUES (");
      if (generatePrimaryKey) {
        sqlBuffer.append(getGeneratePrimaryKeySql(type));
        sqlBuffer.append(",");
      }
      for (int i = 0; i < type.getAttributeCount(); i++) {
        if (!generatePrimaryKey || i != type.getIdAttributeIndex()) {
          final JdbcAttribute attribute = (JdbcAttribute)type.getAttribute(i);
          attribute.addInsertStatementPlaceHolder(sqlBuffer, generatePrimaryKey);
          if (i < type.getAttributeCount() - 1) {
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

  private RecordDefinition getRecordDefinition(final String typePath) {
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
    final String typePath = type.getPath();
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    String sql = this.typeUpdateSqlMap.get(typePath);
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
      if (this.sqlPrefix != null) {
        sqlBuffer.append(this.sqlPrefix);
      }
      sqlBuffer.append("update ");
      if (this.hints != null) {
        sqlBuffer.append(this.hints);
      }
      sqlBuffer.append(tableName);
      sqlBuffer.append(" set ");
      final List<Attribute> idAttributes = type.getIdAttributes();
      boolean first = true;
      for (final Attribute attribute : type.getAttributes()) {
        if (!idAttributes.contains(attribute)) {
          final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
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
      for (final Attribute idAttribute : idAttributes) {
        if (first) {
          first = false;
        } else {
          sqlBuffer.append(" AND ");
        }
        final JdbcAttribute idJdbcAttribute = (JdbcAttribute)idAttribute;
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
    final String typePath = objectType.getPath();
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    flushIfRequired(recordDefinition);
    final String idAttributeName = recordDefinition.getIdAttributeName();
    final boolean hasId = idAttributeName != null;

    final GlobalIdProperty globalIdProperty = GlobalIdProperty.getProperty(object);
    if (globalIdProperty != null) {
      if (object.getValue(globalIdProperty.getAttributeName()) == null) {
        object.setValue(globalIdProperty.getAttributeName(), UUID.randomUUID()
          .toString());
      }
    }

    final boolean hasIdValue = hasId
        && object.getValue(idAttributeName) != null;

    if (!hasId || hasIdValue) {
      insert(object, typePath, recordDefinition);
    } else {
      insertSequence(object, typePath, recordDefinition);
    }
    object.setState(RecordState.Persisted);
    this.recordStore.addStatistic("Insert", object);
  }

  private void insert(final Record object, final String typePath,
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
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
      parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(statement,
        parameterIndex, object);
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
      processCurrentBatch(typePath, sql, statement,
        this.typeInsertBatchCountMap);
    }
  }

  private void insertSequence(final Record object, final String typePath,
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
    final Attribute idAttribute = recordDefinition.getIdAttribute();
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      if (attribute != idAttribute) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(
          statement, parameterIndex, object);
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
      processCurrentBatch(typePath, sql, statement,
        this.typeInsertSequenceBatchCountMap);
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

  private void processCurrentBatch(final String typePath, final String sql,
    final PreparedStatement statement, final Map<String, Integer> batchCountMap) {
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
      throw JdbcUtils.getException(getDataSource(), this.connection,
        "Process Batch", sql, e);
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

  public void setConnection(final Connection connection) {
    this.connection = connection;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
    try {
      setConnection(JdbcUtils.getConnection(dataSource));
      this.connection.setAutoCommit(false);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to create connection", e);
    }
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
    final String typePath = objectType.getPath();
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
    final List<Attribute> idAttributes = recordDefinition.getIdAttributes();
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      if (!idAttributes.contains(attribute)) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(
          statement, parameterIndex, object);
      }
    }
    for (final Attribute idAttribute : idAttributes) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)idAttribute;
      parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(statement,
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
      processCurrentBatch(typePath, sql, statement,
        this.typeUpdateBatchCountMap);
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
        if (state != RecordState.Deleted) {
          insert(object);
        }
      } else {
        switch (state) {
          case New:
            insert(object);
            break;
          case Modified:
            update(object);
            break;
          case Persisted:
            // No action required
            break;
          case Deleted:
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
