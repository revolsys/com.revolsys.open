package com.revolsys.gis.jdbc.io;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.io.AbstractWriter;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcWriter extends AbstractWriter<DataObject> {
  private static final Logger LOG = Logger.getLogger(JdbcWriter.class);

  public static String getTableName(
    final QName tableName) {
    final String namespaceURI = tableName.getNamespaceURI();
    final String localPart = tableName.getLocalPart();
    if (namespaceURI != "") {
      return namespaceURI + "." + localPart;
    } else {
      return localPart;
    }
  }

  private int batchSize = 1;

  private Connection connection;

  private String currentSql;

  private DataSource dataSource;

  private final JdbcDataObjectStore dataStore;

  private String hints = null;

  private Statistics insertStatistics;

  private String label;

  private QName lastTypeName;

  private boolean quoteColumnNames = true;

  private String sqlPrefix;

  private String sqlSuffix;

  private final Map<QName, Integer> typeCountMap = new LinkedHashMap<QName, Integer>();

  private final Map<QName, Integer> typeInsertBatchCountMap = new LinkedHashMap<QName, Integer>();

  private final Map<QName, Integer> typeInsertSequenceBatchCountMap = new LinkedHashMap<QName, Integer>();

  private final Map<QName, String> typeInsertSequenceSqlMap = new LinkedHashMap<QName, String>();

  private final Map<QName, PreparedStatement> typeInsertSequenceStatementMap = new LinkedHashMap<QName, PreparedStatement>();

  private final Map<QName, String> typeInsertSqlMap = new LinkedHashMap<QName, String>();

  private final Map<QName, PreparedStatement> typeInsertStatementMap = new LinkedHashMap<QName, PreparedStatement>();

  private final Map<QName, Integer> typeUpdateBatchCountMap = new LinkedHashMap<QName, Integer>();

  private final Map<QName, String> typeUpdateSqlMap = new LinkedHashMap<QName, String>();

  private final Map<QName, PreparedStatement> typeUpdateStatementMap = new LinkedHashMap<QName, PreparedStatement>();

  private final Map<QName, Integer> typeDeleteBatchCountMap = new LinkedHashMap<QName, Integer>();

  private final Map<QName, String> typeDeleteSqlMap = new LinkedHashMap<QName, String>();

  private final Map<QName, PreparedStatement> typeDeleteStatementMap = new LinkedHashMap<QName, PreparedStatement>();

  private Statistics updateStatistics;

  private Statistics deleteStatistics;

  public JdbcWriter(
    final Connection connection,
    final JdbcDataObjectStore dataStore) {
    this.dataStore = dataStore;
    if (connection == null) {
      setConnection(dataStore.getConnection());
      setDataSource(dataStore.getDataSource());
    } else {
      setConnection(connection);
    }
  }

  public void flush() {

  }

  public JdbcWriter(
    final JdbcDataObjectStore dataStore) {
    this.dataStore = dataStore;
    setConnection(dataStore.getConnection());
    setDataSource(dataStore.getDataSource());
  }

  private void addSqlColumEqualsPlaceholder(
    final StringBuffer sqlBuffer,
    final JdbcAttribute attribute) {
    final String attributeName = attribute.getName();
    if (quoteColumnNames) {
      sqlBuffer.append('"').append(attributeName).append('"');
    } else {
      sqlBuffer.append(attributeName);
    }
    sqlBuffer.append(" = ");
    attribute.addInsertStatementPlaceHolder(sqlBuffer, false);
  }

  public void close() {
    try {

      close(typeInsertSqlMap, typeInsertStatementMap, typeInsertBatchCountMap,
        getInsertStatistics());
      close(typeInsertSequenceSqlMap, typeInsertSequenceStatementMap,
        typeInsertSequenceBatchCountMap, getInsertStatistics());
      close(typeUpdateSqlMap, typeUpdateStatementMap, typeUpdateBatchCountMap,
        getUpdateStatistics());
      close(typeDeleteSqlMap, typeDeleteStatementMap, typeDeleteBatchCountMap,
        getDeleteStatistics());
    } finally {
      if (dataSource != null) {
        try {
          connection.commit();
        } catch (final SQLException e) {
          LOG.error("Failed to commit data:", e);
        } finally {
          JdbcUtils.close(connection);
        }
      }
    }
    insertStatistics.disconnect();
    insertStatistics = null;
    updateStatistics.disconnect();
    updateStatistics = null;
    deleteStatistics.disconnect();
    deleteStatistics = null;
  }

  private void close(
    final Map<QName, String> sqlMap,
    final Map<QName, PreparedStatement> statementMap,
    final Map<QName, Integer> batchCountMap,
    final Statistics statistics) {
    for (final Entry<QName, PreparedStatement> entry : statementMap.entrySet()) {
      final QName typeName = entry.getKey();
      final PreparedStatement statement = entry.getValue();
      final String sql = sqlMap.get(typeName);
      try {
        processCurrentBatch(typeName, sql, statement, batchCountMap, statistics);
      } catch (final SQLException e) {
        LOG.error("Unable to process batch: " + sql, e);
      }
      JdbcUtils.close(statement);
    }
  }

  public int getBatchSize() {
    return batchSize;
  }

  private Connection getConnection() {
    return connection;
  }

  private DataObjectMetaData getDataObjectMetaData(
    final QName typeName) {
    final DataObjectMetaData metaData = dataStore.getMetaData(typeName);
    return metaData;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  private String getGeneratePrimaryKeySql(
    final DataObjectMetaData metaData) {
    return dataStore.getGeneratePrimaryKeySql(metaData);
  }

  /**
   * @return the hints
   */
  public String getHints() {
    return hints;
  }

  private String getInsertSql(
    final DataObjectMetaData type,
    final boolean generatePrimaryKey) {
    final QName typeName = type.getName();
    final String tableName = getTableName(typeName);
    String sql;
    if (generatePrimaryKey) {
      sql = typeInsertSequenceSqlMap.get(typeName);
    } else {
      sql = typeInsertSqlMap.get(typeName);
    }
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
      if (sqlPrefix != null) {
        sqlBuffer.append(sqlPrefix);
      }
      sqlBuffer.append("insert ");
      if (hints != null) {
        sqlBuffer.append(hints);
      }
      sqlBuffer.append(" into ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" (");
      if (generatePrimaryKey) {
        final String idAttributeName = type.getIdAttributeName();
        if (quoteColumnNames) {
          sqlBuffer.append('"').append(idAttributeName).append('"');
        } else {
          sqlBuffer.append(idAttributeName);
        }
        sqlBuffer.append(",");
      }
      for (int i = 0; i < type.getAttributeCount(); i++) {
        if (!generatePrimaryKey || i != type.getIdAttributeIndex()) {
          final String attributeName = type.getAttributeName(i);
          if (quoteColumnNames) {
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
      if (sqlSuffix != null) {
        sqlBuffer.append(sqlSuffix);
      }
      sql = sqlBuffer.toString();
      if (generatePrimaryKey) {
        typeInsertSequenceSqlMap.put(typeName, sql);
      } else {
        typeInsertSqlMap.put(typeName, sql);
      }
    }
    return sql;
  }

  public Statistics getInsertStatistics() {
    if (insertStatistics == null) {
      if (label == null) {
        insertStatistics = new Statistics("Insert");
      } else {
        insertStatistics = new Statistics(label + " Insert");
      }
      insertStatistics.connect();
    }
    return insertStatistics;
  }

  public String getLabel() {
    return label;
  }

  public String toString() {
    return null;
  }

  public String getSqlPrefix() {
    return sqlPrefix;
  }

  public String getSqlSuffix() {
    return sqlSuffix;
  }

  private String getUpdateSql(
    final DataObjectMetaData type) {
    final QName typeName = type.getName();
    final String tableName = getTableName(typeName);
    String sql = typeUpdateSqlMap.get(typeName);
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
      if (sqlPrefix != null) {
        sqlBuffer.append(sqlPrefix);
      }
      sqlBuffer.append("update ");
      if (hints != null) {
        sqlBuffer.append(hints);
      }
      sqlBuffer.append(tableName);
      sqlBuffer.append(" set ");
      for (int i = 0; i < type.getAttributeCount(); i++) {
        if (i != type.getIdAttributeIndex()) {
          final JdbcAttribute attribute = (JdbcAttribute)type.getAttribute(i);
          addSqlColumEqualsPlaceholder(sqlBuffer, attribute);
          if (i < type.getAttributeCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(" where ");
      final JdbcAttribute idAttribute = (JdbcAttribute)type.getIdAttribute();
      addSqlColumEqualsPlaceholder(sqlBuffer, idAttribute);

      sqlBuffer.append(" ");
      if (sqlSuffix != null) {
        sqlBuffer.append(sqlSuffix);
      }
      sql = sqlBuffer.toString();

      typeUpdateSqlMap.put(typeName, sql);
    }
    return sql;
  }

  public Statistics getUpdateStatistics() {
    if (updateStatistics == null) {
      if (label == null) {
        updateStatistics = new Statistics("Update");
      } else {
        updateStatistics = new Statistics(label + " Update");
      }
      updateStatistics.connect();
    }
    return updateStatistics;
  }

  private String getDeleteSql(
    final DataObjectMetaData type) {
    final QName typeName = type.getName();
    final String tableName = getTableName(typeName);
    String sql = typeDeleteSqlMap.get(typeName);
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
      if (sqlPrefix != null) {
        sqlBuffer.append(sqlPrefix);
      }
      sqlBuffer.append("delete ");
      if (hints != null) {
        sqlBuffer.append(hints);
      }
      sqlBuffer.append(" from ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" where ");
      final JdbcAttribute idAttribute = (JdbcAttribute)type.getIdAttribute();
      addSqlColumEqualsPlaceholder(sqlBuffer, idAttribute);

      sqlBuffer.append(" ");
      if (sqlSuffix != null) {
        sqlBuffer.append(sqlSuffix);
      }
      sql = sqlBuffer.toString();

      typeDeleteSqlMap.put(typeName, sql);
    }
    return sql;
  }

  public Statistics getDeleteStatistics() {
    if (deleteStatistics == null) {
      if (label == null) {
        deleteStatistics = new Statistics("Delete");
      } else {
        deleteStatistics = new Statistics(label + " Delete");
      }
      deleteStatistics.connect();
    }
    return deleteStatistics;
  }

  private void insert(
    final DataObject object)
    throws SQLException {
    final DataObjectMetaData objectType = object.getMetaData();
    final QName typeName = objectType.getName();
    final DataObjectMetaData metaData = getDataObjectMetaData(typeName);
    final boolean hasId = metaData.getIdAttributeIndex() > -1;
    final boolean hasIdValue = hasId && object.getIdValue() != null;
    if (!hasId || hasIdValue) {
      insert(object, typeName, metaData);
    } else {
      insertSequence(object, typeName, metaData);
    }
  }

  private void insert(
    final DataObject object,
    final QName typeName,
    final DataObjectMetaData metaData)
    throws SQLException {
    PreparedStatement statement = typeInsertStatementMap.get(typeName);
    if (statement == null) {
      final String sql = getInsertSql(metaData, false);
      try {
        statement = connection.prepareStatement(sql);
        typeInsertStatementMap.put(typeName, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    for (final Attribute attribute : metaData.getAttributes()) {
      final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
      parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(statement,
        parameterIndex, object);
    }
    statement.addBatch();
    Integer batchCount = typeInsertBatchCountMap.get(typeName);
    if (batchCount == null) {
      batchCount = 1;
      typeInsertBatchCountMap.put(typeName, 1);
    } else {
      batchCount += 1;
      typeInsertBatchCountMap.put(typeName, batchCount);
    }
    if (batchCount >= batchSize) {
      final String sql = getInsertSql(metaData, false);
      processCurrentBatch(typeName, sql, statement, typeInsertBatchCountMap,
        getInsertStatistics());
    }
  }

  private void insertSequence(
    final DataObject object,
    final QName typeName,
    final DataObjectMetaData metaData)
    throws SQLException {
    PreparedStatement statement = typeInsertSequenceStatementMap.get(typeName);
    if (statement == null) {
      final String sql = getInsertSql(metaData, true);
      try {
        statement = connection.prepareStatement(sql);
        typeInsertSequenceStatementMap.put(typeName, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final Attribute idAttribute = metaData.getIdAttribute();
    for (final Attribute attribute : metaData.getAttributes()) {
      if (attribute != idAttribute) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(
          statement, parameterIndex, object);
      }
    }
    statement.addBatch();
    Integer batchCount = typeInsertSequenceBatchCountMap.get(typeName);
    if (batchCount == null) {
      batchCount = 1;
      typeInsertSequenceBatchCountMap.put(typeName, 1);
    } else {
      batchCount += 1;
      typeInsertSequenceBatchCountMap.put(typeName, batchCount);
    }
    if (batchCount >= batchSize) {
      final String sql = getInsertSql(metaData, true);
      processCurrentBatch(typeName, sql, statement,
        typeInsertSequenceBatchCountMap, getInsertStatistics());
    }
  }

  public boolean isQuoteColumnNames() {
    return quoteColumnNames;
  }

  private void processCurrentBatch(
    final QName typeName,
    final String sql,
    final PreparedStatement statement,
    final Map<QName, Integer> batchCountMap,
    final Statistics statistics)
    throws SQLException {
    final int batchCount = batchCountMap.get(typeName);
    try {
      Integer typeCount = typeCountMap.get(typeName);
      if (typeCount == null) {
        typeCount = batchCount;
      } else {
        typeCount += batchCount;
      }
      typeCountMap.put(typeName, typeCount);
      statement.executeBatch();
      statistics.add(typeName.toString(), batchCount);
    } catch (final BatchUpdateException be) {
      LOG.error(be.getNextException() + " " + sql);
      throw be;
    } catch (final SQLException e) {
      LOG.error(sql, e);
      throw e;
    } catch (final RuntimeException e) {
      LOG.error(sql, e);
      throw e;
    } finally {
      batchCountMap.put(typeName, 0);
    }
  }

  public void setBatchSize(
    final int batchSize) {
    this.batchSize = batchSize;
  }

  public void setConnection(
    final Connection connection) {
    this.connection = connection;
  }

  public void setDataSource(
    final DataSource dataSource) {
    this.dataSource = dataSource;
    try {
      setConnection(dataSource.getConnection());
      connection.setAutoCommit(false);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to create connection", e);
    }
  }

  /**
   * @param hints the hints to set
   */
  public void setHints(
    final String hints) {
    this.hints = hints;
  }

  public void setLabel(
    final String label) {
    this.label = label;
  }

  public void setQuoteColumnNames(
    final boolean quoteColumnNames) {
    this.quoteColumnNames = quoteColumnNames;
  }

  public void setSqlPrefix(
    final String sqlPrefix) {
    this.sqlPrefix = sqlPrefix;
  }

  public void setSqlSuffix(
    final String sqlSuffix) {
    this.sqlSuffix = sqlSuffix;
  }

  private void update(
    final DataObject object)
    throws SQLException {
    final DataObjectMetaData objectType = object.getMetaData();
    final QName typeName = objectType.getName();
    final DataObjectMetaData metaData = getDataObjectMetaData(typeName);
    PreparedStatement statement = typeUpdateStatementMap.get(typeName);
    if (statement == null) {
      final String sql = getUpdateSql(metaData);
      try {
        statement = connection.prepareStatement(sql);
        typeUpdateStatementMap.put(typeName, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final JdbcAttribute idAttribute = (JdbcAttribute)metaData.getIdAttribute();
    for (final Attribute attribute : metaData.getAttributes()) {
      if (attribute != idAttribute) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        parameterIndex = jdbcAttribute.setInsertPreparedStatementValue(
          statement, parameterIndex, object);
      }
    }
    parameterIndex = idAttribute.setInsertPreparedStatementValue(statement,
      parameterIndex, object);
    statement.addBatch();
    Integer batchCount = typeUpdateBatchCountMap.get(typeName);
    if (batchCount == null) {
      batchCount = 1;
      typeUpdateBatchCountMap.put(typeName, 1);
    } else {
      batchCount += 1;
      typeUpdateBatchCountMap.put(typeName, batchCount);
    }
    if (batchCount >= batchSize) {
      final String sql = getUpdateSql(metaData);
      processCurrentBatch(typeName, sql, statement, typeUpdateBatchCountMap,
        getUpdateStatistics());
    }
  }

  private void delete(
    final DataObject object)
    throws SQLException {
    final DataObjectMetaData objectType = object.getMetaData();
    final QName typeName = objectType.getName();
    final DataObjectMetaData metaData = getDataObjectMetaData(typeName);
    PreparedStatement statement = typeDeleteStatementMap.get(typeName);
    if (statement == null) {
      final String sql = getDeleteSql(metaData);
      try {
        statement = connection.prepareStatement(sql);
        typeDeleteStatementMap.put(typeName, statement);
      } catch (final SQLException e) {
        LOG.error(sql, e);
      }
    }
    int parameterIndex = 1;
    final JdbcAttribute idAttribute = (JdbcAttribute)metaData.getIdAttribute();
    parameterIndex = idAttribute.setInsertPreparedStatementValue(statement,
      parameterIndex, object);
    statement.addBatch();
    Integer batchCount = typeDeleteBatchCountMap.get(typeName);
    if (batchCount == null) {
      batchCount = 1;
      typeDeleteBatchCountMap.put(typeName, 1);
    } else {
      batchCount += 1;
      typeDeleteBatchCountMap.put(typeName, batchCount);
    }
    // TODO this locks code tables which prevents insert
    // if (batchCount >= batchSize) {
    // final String sql = getDeleteSql(metaData);
    // processCurrentBatch(typeName, sql, statement, typeDeleteBatchCountMap,
    // getDeleteStatistics());
    // }
  }

  public synchronized void write(
    final DataObject object) {
    try {
      switch (object.getState()) {
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
    } catch (final Throwable t) {
      LOG.error("Unable to write: " + object, t);
    }
  }
}
