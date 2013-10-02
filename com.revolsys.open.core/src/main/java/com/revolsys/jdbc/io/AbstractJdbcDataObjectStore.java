package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ResultPager;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreQueryReader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.GlobalIdProperty;
import com.revolsys.gis.data.model.codes.AbstractCodeTable;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.SqlCondition;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.transaction.DataSourceTransactionManagerFactory;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractJdbcDataObjectStore extends
  AbstractDataObjectStore implements JdbcDataObjectStore {
  private Map<String, JdbcAttributeAdder> attributeAdders = new HashMap<String, JdbcAttributeAdder>();

  private int batchSize;

  private Connection connection;

  private DataSource dataSource;

  private List<String> excludeTablePatterns = new ArrayList<String>();

  private boolean flushBetweenTypes;

  private String hints;

  private Map<String, String> sequenceTypeSqlMap = new HashMap<String, String>();

  private String sqlPrefix;

  private String sqlSuffix;

  private String permissionsSql;

  private Map<String, String> schemaNameMap = new HashMap<String, String>();

  private Map<String, String> tableNameMap = new HashMap<String, String>();

  private JdbcDatabaseFactory databaseFactory;

  private final Object writerKey = new Object();

  private Map<String, Map<String, List<String>>> schemaTablePermissions;

  private String primaryKeySql;

  public AbstractJdbcDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public AbstractJdbcDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  public AbstractJdbcDataObjectStore(final DataSource dataSource) {
    setDataSource(dataSource);
  }

  public AbstractJdbcDataObjectStore(final JdbcDatabaseFactory databaseFactory) {
    this(databaseFactory, new ArrayDataObjectFactory());
  }

  public AbstractJdbcDataObjectStore(final JdbcDatabaseFactory databaseFactory,
    final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
    this.databaseFactory = databaseFactory;
  }

  protected void addAttribute(final DataObjectMetaDataImpl metaData,
    final String name, final String dataType, final int sqlType,
    final int length, final int scale, final boolean required) {
    JdbcAttributeAdder attributeAdder = attributeAdders.get(dataType);
    if (attributeAdder == null) {
      attributeAdder = new JdbcAttributeAdder(DataTypes.OBJECT);
    }
    attributeAdder.addAttribute(metaData, name, dataType, sqlType, length,
      scale, required);
  }

  protected void addAttribute(final ResultSetMetaData resultSetMetaData,
    final DataObjectMetaDataImpl metaData, final String name, final int i)
    throws SQLException {
    final String dataType = resultSetMetaData.getColumnTypeName(i);
    final int sqlType = resultSetMetaData.getColumnType(i);
    final int length = resultSetMetaData.getPrecision(i);
    final int scale = resultSetMetaData.getScale(i);
    final boolean required = false;
    addAttribute(metaData, name, dataType, sqlType, length, scale, required);
  }

  public void addAttributeAdder(final String sqlTypeName,
    final JdbcAttributeAdder adder) {
    attributeAdders.put(sqlTypeName, adder);
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    try {
      super.close();
      if (connection != null) {
        if (dataSource != null) {
          JdbcUtils.release(connection, dataSource);
        }
      }
      if (databaseFactory != null && dataSource != null) {
        databaseFactory.closeDataSource(dataSource);
      }
    } finally {
      attributeAdders = null;
      connection = null;
      dataSource = null;
      excludeTablePatterns = null;
      hints = null;
      schemaNameMap = null;
      sequenceTypeSqlMap = null;
      sqlPrefix = null;
      sqlSuffix = null;
      tableNameMap = null;
    }
  }

  @Override
  public AbstractIterator<DataObject> createIterator(final Query query,
    final Map<String, Object> properties) {
    return new JdbcQueryIterator(this, query, properties);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T createPrimaryIdValue(final String typePath) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    final GlobalIdProperty globalIdProperty = GlobalIdProperty.getProperty(metaData);
    if (globalIdProperty == null) {
      return (T)getNextPrimaryKey(metaData);
    } else {
      return (T)UUID.randomUUID().toString();
    }
  }

  protected DataObjectStoreQueryReader createReader(final Query query) {
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(query);
    return reader;
  }

  @Override
  public JdbcWriter createWriter() {
    final int size = batchSize;
    return createWriter(size);
  }

  public JdbcWriter createWriter(final int batchSize) {
    final JdbcWriter writer = new JdbcWriter(this);
    writer.setSqlPrefix(sqlPrefix);
    writer.setSqlSuffix(sqlSuffix);
    writer.setBatchSize(batchSize);
    writer.setHints(hints);
    writer.setLabel(getLabel());
    writer.setFlushBetweenTypes(flushBetweenTypes);
    writer.setQuoteColumnNames(false);
    return writer;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void delete(final DataObject object) {
    if (object.getState() == DataObjectState.Persisted
      || object.getState() == DataObjectState.Modified) {
      object.setState(DataObjectState.Deleted);
      final JdbcWriter writer = getWriter();
      try {
        writer.write(object);
      } finally {
        writer.close();
      }
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public int delete(final Query query) {
    final String typeName = query.getTypeName();
    DataObjectMetaData metaData = query.getMetaData();
    if (metaData == null) {
      if (typeName != null) {
        metaData = getMetaData(typeName);
        query.setMetaData(metaData);
      }
    }
    final String sql = JdbcUtils.getDeleteSql(query);

    Connection connection = getConnection();
    final DataSource dataSource = getDataSource();
    try {
      if (dataSource != null) {
        try {
          connection = JdbcUtils.getConnection(dataSource);
          boolean autoCommit = false;
          if (BooleanStringConverter.getBoolean(getProperties().get(
            "autoCommit"))) {
            autoCommit = true;
          }
          connection.setAutoCommit(autoCommit);
        } catch (final SQLException e) {
          throw new IllegalArgumentException("Unable to create connection", e);
        }
      }

      final PreparedStatement statement = connection.prepareStatement(sql);
      try {

        JdbcUtils.setPreparedStatementParameters(statement, query);
        return statement.executeUpdate();
      } finally {
        JdbcUtils.close(statement);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to delete : " + sql, e);
    } finally {
      if (dataSource != null) {
        JdbcUtils.release(connection, dataSource);
      }
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void deleteAll(final Collection<DataObject> objects) {
    final JdbcWriter writer = getWriter();
    try {
      for (final DataObject object : objects) {
        if (object.getState() == DataObjectState.Persisted
          || object.getState() == DataObjectState.Modified) {
          object.setState(DataObjectState.Deleted);
          writer.write(object);
        }
      }
    } finally {
      writer.close();
    }
  }

  public JdbcAttribute getAttribute(final String schemaName,
    final String tableName, final String columnName) {
    final String typePath = PathUtil.toPath(schemaName, tableName);
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      return null;
    } else {
      final Attribute attribute = metaData.getAttribute(columnName);
      return (JdbcAttribute)attribute;
    }
  }

  public int getBatchSize() {
    return batchSize;
  }

  public List<String> getColumnNames(final String typePath) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    return metaData.getAttributeNames();
  }

  @Override
  public Connection getConnection() {
    return connection;
  }

  public String getDatabaseSchemaName(final DataObjectStoreSchema schema) {
    if (schema == null) {
      return null;
    } else {
      final String schemaPath = schema.getPath();
      return getDatabaseSchemaName(schemaPath);
    }
  }

  // protected Set<String> getDatabaseSchemaNames() {
  // final Set<String> databaseSchemaNames = new TreeSet<String>();
  // try {
  // final Connection connection = getDbConnection();
  // try {
  // final DatabaseMetaData databaseMetaData = connection.getMetaData();
  // final ResultSet schemaRs = databaseMetaData.getSchemas();
  //
  // try {
  // while (schemaRs.next()) {
  // final String dbSchemaName = schemaRs.getString("TABLE_SCHEM");
  // databaseSchemaNames.add(dbSchemaName);
  // }
  // } finally {
  // JdbcUtils.close(schemaRs);
  // }
  // } finally {
  // releaseConnection(connection);
  // }
  // } catch (final SQLException e) {
  // throw new RuntimeException("Unable to get list of namespaces", e);
  // }
  // return databaseSchemaNames;
  // }

  public String getDatabaseSchemaName(final String schemaPath) {
    return schemaNameMap.get(schemaPath);
  }

  // protected Set<String> getDatabaseTableNames(final String dbSchemaName)
  // throws SQLException {
  // final Connection connection = getDbConnection();
  // try {
  // final Set<String> tableNames = new LinkedHashSet<String>();
  //
  // final DatabaseMetaData databaseMetaData = connection.getMetaData();
  // final ResultSet tablesRs = databaseMetaData.getTables(null, dbSchemaName,
  // "%", null);
  // try {
  // while (tablesRs.next()) {
  // final String dbTableName = tablesRs.getString("TABLE_NAME");
  // final String tableName = dbTableName.toUpperCase();
  // final String tableType = tablesRs.getString("TABLE_TYPE");
  // final boolean excluded = !tableTypes.contains(tableType);
  // if (!excluded && !isExcluded(dbSchemaName, dbTableName)) {
  // tableNames.add(tableName);
  // }
  // }
  // } finally {
  // JdbcUtils.close(tablesRs);
  // }
  // return tableNames;
  // } finally {
  // releaseConnection(connection);
  // }
  // }

  protected Set<String> getDatabaseSchemaNames() {
    final Map<String, Map<String, List<String>>> tablePermissions = getSchemaTablePermissions();
    return tablePermissions.keySet();
  }

  @Override
  public String getDatabaseTableName(final String typePath) {
    return tableNameMap.get(typePath);
  }

  protected Set<String> getDatabaseTableNames(final String dbSchemaName)
    throws SQLException {
    final Map<String, Map<String, List<String>>> schemaTablePermissions = getSchemaTablePermissions();
    final Map<String, List<String>> schemaPermissions = schemaTablePermissions.get(dbSchemaName);
    return schemaPermissions.keySet();
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  protected Connection getDbConnection() {
    if (dataSource != null) {
      return JdbcUtils.getConnection(dataSource);
    } else {
      return connection;
    }
  }

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    throw new UnsupportedOperationException(
      "Cannot create SQL to generate Primary Key for " + metaData);
  }

  public String getHints() {
    return hints;
  }

  public String getIdAttributeName(final String typePath) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      return null;
    } else {
      return metaData.getIdAttributeName();
    }
  }

  @Override
  public DataObjectMetaData getMetaData(final String typePath,
    final ResultSetMetaData resultSetMetaData) {
    try {
      final String schemaName = PathUtil.getPath(typePath);
      final DataObjectStoreSchema schema = getSchema(schemaName);
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(this,
        schema, typePath);

      final String idAttributeName = getIdAttributeName(typePath);
      for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
        final String name = resultSetMetaData.getColumnName(i).toUpperCase();
        if (name.equals(idAttributeName)) {
          metaData.setIdAttributeIndex(i - 1);
        }
        addAttribute(resultSetMetaData, metaData, name, i);
      }

      addMetaDataProperties(metaData);

      return metaData;
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to load metadata for "
        + typePath);
    }
  }

  @Override
  public int getRowCount(Query query) {
    query = query.clone();
    query.setSql(null);
    query.setAttributeNames("count(*)");
    query.setOrderBy(Collections.<String, Boolean> emptyMap());
    final String sql = JdbcUtils.getSelectSql(query);
    final DataSource dataSource = getDataSource();
    Connection connection = getConnection();
    if (dataSource != null) {
      connection = JdbcUtils.getConnection(dataSource);
    }
    try {
      final PreparedStatement statement = connection.prepareStatement(sql);
      try {
        JdbcUtils.setPreparedStatementParameters(statement, query);
        final ResultSet resultSet = statement.executeQuery();
        try {
          if (resultSet.next()) {
            return resultSet.getInt(1);
          } else {
            return 0;
          }
        } finally {
          JdbcUtils.close(resultSet);
        }

      } finally {
        JdbcUtils.close(statement);
      }
    } catch (final SQLException e) {
      throw JdbcUtils.getException(dataSource, connection, "selectInt", sql, e);
    } finally {
      if (dataSource != null) {
        JdbcUtils.release(connection, dataSource);
      }
    }
  }

  protected Map<String, Map<String, List<String>>> getSchemaTablePermissions() {
    if (schemaTablePermissions == null) {
      synchronized (this) {
        if (schemaTablePermissions == null) {

          final Map<String, Map<String, List<String>>> schemaTablePermissions1 = new HashMap<String, Map<String, List<String>>>();
          try {
            final Connection connection = getDbConnection();
            try {
              final PreparedStatement statement = connection.prepareStatement(permissionsSql);
              final ResultSet resultSet = statement.executeQuery();

              try {
                while (resultSet.next()) {
                  final String owner = resultSet.getString("SCHEMA_NAME");
                  if (!isSchemaExcluded(owner)) {
                    final String dbTableName = resultSet.getString("TABLE_NAME");
                    if (!isExcluded(owner, dbTableName)) {

                      final String privilege = resultSet.getString("PRIVILEGE");
                      Map<String, List<String>> schemaPermissions = schemaTablePermissions1.get(owner);
                      if (schemaPermissions == null) {
                        schemaPermissions = new TreeMap<String, List<String>>();
                        schemaTablePermissions1.put(owner, schemaPermissions);
                      }
                      List<String> tablePermissions = schemaPermissions.get(dbTableName);
                      if (tablePermissions == null) {
                        tablePermissions = new ArrayList<String>();
                        schemaPermissions.put(dbTableName, tablePermissions);
                      }
                      tablePermissions.add(privilege);
                    }
                  }
                }
              } finally {
                JdbcUtils.close(resultSet);
              }
            } finally {
              releaseConnection(connection);
            }
          } catch (final Throwable e) {
            LoggerFactory.getLogger(getClass()).error(
              "Unable to get schema and table permissions", e);
          }
          final Map<String, Map<String, List<String>>> schemaTablePermissions = schemaTablePermissions1;
          this.schemaTablePermissions = schemaTablePermissions;

        }
      }
    }
    return schemaTablePermissions;
  }

  protected String getSequenceInsertSql(final DataObjectMetaData metaData) {
    final String typePath = metaData.getPath();
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    String sql = sequenceTypeSqlMap.get(typePath);
    if (sql == null) {
      final StringBuffer sqlBuffer = new StringBuffer();
      sqlBuffer.append("insert ");

      sqlBuffer.append(" into ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" (");
      sqlBuffer.append('"').append(metaData.getIdAttributeName()).append('"');
      sqlBuffer.append(",");
      for (int i = 0; i < metaData.getAttributeCount(); i++) {
        if (i != metaData.getIdAttributeIndex()) {
          final String attributeName = metaData.getAttributeName(i);
          sqlBuffer.append('"').append(attributeName).append('"');
          if (i < metaData.getAttributeCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(") VALUES (");
      sqlBuffer.append(getGeneratePrimaryKeySql(metaData));
      sqlBuffer.append(",");
      for (int i = 0; i < metaData.getAttributeCount(); i++) {
        if (i != metaData.getIdAttributeIndex()) {
          sqlBuffer.append("?");
          if (i < metaData.getAttributeCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(")");
      sql = sqlBuffer.toString();
      sequenceTypeSqlMap.put(typePath, sql);
    }
    return sql;
  }

  public String getSqlPrefix() {
    return sqlPrefix;
  }

  public String getSqlSuffix() {
    return sqlSuffix;
  }

  public List<String> getTablePermissions(final String schema,
    final String table) {
    final Map<String, Map<String, List<String>>> schemaTablePermissions = getSchemaTablePermissions();
    final Map<String, List<String>> schemaPermissions = schemaTablePermissions.get(schema);
    if (schemaPermissions != null) {
      final List<String> tablePermissions = schemaPermissions.get(table);
      if (tablePermissions != null) {
        return tablePermissions;
      }
    }
    return Arrays.asList("SELECT");
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    final DataSource dataSource = getDataSource();
    return DataSourceTransactionManagerFactory.getTransactionManager(dataSource);
  }

  @Override
  public JdbcWriter getWriter() {
    final JdbcWriterResourceHolder resourceHolder = (JdbcWriterResourceHolder)TransactionSynchronizationManager.getResource(writerKey);
    if (resourceHolder != null
      && (resourceHolder.hasWriter() || resourceHolder.isSynchronizedWithTransaction())) {
      resourceHolder.requested();
      if (!resourceHolder.hasWriter()) {
        final JdbcWriter writer = createWriter(1);
        resourceHolder.setWriter(writer);
      }
      return resourceHolder.getWriter();
    }
    final JdbcWriter writer = createWriter(1);
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      JdbcWriterResourceHolder holderToUse = resourceHolder;
      if (holderToUse == null) {
        holderToUse = new JdbcWriterResourceHolder(writer);
      } else {
        holderToUse.setWriter(writer);
      }
      holderToUse.requested();
      final JdbcWriterSynchronization synchronization = new JdbcWriterSynchronization(
        this, holderToUse, writerKey);
      TransactionSynchronizationManager.registerSynchronization(synchronization);
      holderToUse.setSynchronizedWithTransaction(true);
      if (holderToUse != resourceHolder) {
        TransactionSynchronizationManager.bindResource(writerKey, holderToUse);
      }
    }
    return writer;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void insert(final DataObject object) {
    final JdbcWriter writer = getWriter();
    try {
      writer.write(object);
    } finally {
      writer.close();
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void insertAll(final Collection<DataObject> objects) {
    final JdbcWriter writer = getWriter();
    try {
      for (final DataObject object : objects) {
        writer.write(object);
      }
    } finally {
      writer.close();
    }
  }

  @Override
  public boolean isEditable(final String typePath) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    return metaData.getIdAttributeIndex() != -1;
  }

  protected boolean isExcluded(final String dbSchemaName, final String tableName) {
    final String path = ("/" + dbSchemaName + "/" + tableName).toUpperCase();
    for (final String pattern : excludeTablePatterns) {
      if (path.matches(pattern) || tableName.matches(pattern)) {
        return true;
      }
    }
    return false;
  }

  public boolean isFlushBetweenTypes() {
    return flushBetweenTypes;
  }

  public abstract boolean isSchemaExcluded(String schemaName);

  protected synchronized Map<String, String> loadIdColumnNames(
    final String dbSchemaName) {
    final String schemaName = "/" + dbSchemaName.toUpperCase();
    final Map<String, String> idColumnNames = new HashMap<String, String>();
    final Connection connection = getDbConnection();
    try {
      final PreparedStatement statement = connection.prepareStatement(primaryKeySql);
      statement.setString(1, dbSchemaName);
      final ResultSet rs = statement.executeQuery();
      try {
        while (rs.next()) {
          final String tableName = rs.getString("TABLE_NAME").toUpperCase();
          final String idAttributeName = rs.getString("COLUMN_NAME");
          idColumnNames.put(schemaName + "/" + tableName, idAttributeName);
        }
      } finally {
        JdbcUtils.close(rs);
      }
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to primary keys for schema "
        + dbSchemaName, e);
    } finally {
      releaseConnection(connection);
    }
    return idColumnNames;
  }

  @Override
  protected synchronized void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<String, DataObjectMetaData> metaDataMap) {
    final String schemaName = schema.getPath();
    final String dbSchemaName = getDatabaseSchemaName(schemaName);
    for (final JdbcAttributeAdder attributeAdder : attributeAdders.values()) {
      attributeAdder.initialize(schema);
    }
    final Connection connection = getDbConnection();
    try {
      final DatabaseMetaData databaseMetaData = connection.getMetaData();

      final Map<String, String> idAttributeNames = loadIdColumnNames(dbSchemaName);
      final Set<String> tableNames = getDatabaseTableNames(dbSchemaName);
      for (final String dbTableName : tableNames) {
        final String tableName = dbTableName.toUpperCase();
        final String typePath = PathUtil.toPath(schemaName, tableName);
        tableNameMap.put(typePath, dbTableName);
        final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
          this, schema, typePath);
        final List<String> permissions = getTablePermissions(dbSchemaName,
          dbTableName);
        metaData.setProperty("permissions", permissions);
        metaDataMap.put(typePath, metaData);
      }

      final ResultSet columnsRs = databaseMetaData.getColumns(null,
        dbSchemaName, "%", "%");
      try {
        while (columnsRs.next()) {
          final String tableName = columnsRs.getString("TABLE_NAME")
            .toUpperCase();
          final String typePath = PathUtil.toPath(schemaName, tableName);
          final DataObjectMetaDataImpl metaData = (DataObjectMetaDataImpl)metaDataMap.get(typePath);
          if (metaData != null) {
            final String name = columnsRs.getString("COLUMN_NAME")
              .toUpperCase();
            final int sqlType = columnsRs.getInt("DATA_TYPE");
            final String dataType = columnsRs.getString("TYPE_NAME");
            final int length = columnsRs.getInt("COLUMN_SIZE");
            int scale = columnsRs.getInt("DECIMAL_DIGITS");
            if (columnsRs.wasNull()) {
              scale = -1;
            }
            final boolean required = !columnsRs.getString("IS_NULLABLE")
              .equals("YES");
            addAttribute(metaData, name, dataType, sqlType, length, scale,
              required);
          }
        }

        for (final DataObjectMetaData metaData : schema.getTypes()) {
          final String typePath = metaData.getPath();
          final String idAttributeName = idAttributeNames.get(typePath);
          ((DataObjectMetaDataImpl)metaData).setIdAttributeName(idAttributeName);
        }

      } finally {
        JdbcUtils.close(columnsRs);
      }

    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to load metadata for schema "
        + schemaName, e);
    } finally {
      releaseConnection(connection);
    }

    for (final DataObjectMetaData metaData : metaDataMap.values()) {
      addMetaDataProperties((DataObjectMetaDataImpl)metaData);

      postCreateDataObjectMetaData((DataObjectMetaDataImpl)metaData);
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    final Set<String> databaseSchemaNames = getDatabaseSchemaNames();
    for (final String dbSchemaName : databaseSchemaNames) {
      final String schemaName = "/" + dbSchemaName.toUpperCase();
      schemaNameMap.put(schemaName, dbSchemaName);
      final DataObjectStoreSchema schema = new DataObjectStoreSchema(this,
        schemaName);
      schemaMap.put(schemaName, schema);
    }
  }

  @Override
  public ResultPager<DataObject> page(final Query query) {
    return new JdbcQueryResultPager(this, getProperties(), query);
  }

  protected void postCreateDataObjectMetaData(
    final DataObjectMetaDataImpl metaData) {
  }

  @Override
  public DataObjectStoreQueryReader query(
    final DataObjectFactory dataObjectFactory, final String typePath,
    final BoundingBox boundingBox) {

    final Query query = new Query(typePath);
    query.setProperty("dataObjectFactory", dataObjectFactory);
    query.setBoundingBox(boundingBox);
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(query);
    return reader;
  }

  @Override
  public Reader<DataObject> query(final DataObjectFactory dataObjectFactory,
    final String typePath, Geometry geometry) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    final JdbcAttribute geometryAttribute = (JdbcAttribute)metaData.getGeometryAttribute();
    final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
    geometry = ProjectionFactory.convert(geometry, geometryFactory);

    final SqlFunction intersectsFunction = geometryAttribute.getProperty(JdbcConstants.FUNCTION_INTERSECTS);
    final StringBuffer qArg = new StringBuffer();
    geometryAttribute.addSelectStatementPlaceHolder(qArg);

    final Query query = new Query(metaData);
    query.setProperty("dataObjectFactory", dataObjectFactory);
    query.setWhereCondition(new SqlCondition(intersectsFunction.toSql(
      geometryAttribute.getName(), qArg), geometryAttribute, geometry));
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(query);
    return reader;
  }

  protected void releaseConnection(final Connection connection) {
    JdbcUtils.release(connection, dataSource);
  }

  @Override
  public void releaseWriter(final JdbcWriter writer) {
    if (writer != null) {
      final JdbcWriterResourceHolder resourceHolder = (JdbcWriterResourceHolder)TransactionSynchronizationManager.getResource(writerKey);
      if (resourceHolder != null && resourceHolder.writerEquals(writer)) {
        resourceHolder.released();
      } else {
        writer.doClose();
      }
    }
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  public void setCodeTables(final List<AbstractCodeTable> codeTables) {
    for (final AbstractCodeTable codeTable : codeTables) {
      addCodeTable(codeTable);
    }
  }

  public void setConnection(final Connection connection) {
    this.connection = connection;
  }

  @Override
  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setExcludeTablePatterns(final String... excludeTablePatterns) {
    this.excludeTablePatterns = new ArrayList<String>(
      Arrays.asList(excludeTablePatterns));
  }

  public void setFlushBetweenTypes(final boolean flushBetweenTypes) {
    this.flushBetweenTypes = flushBetweenTypes;
  }

  public void setHints(final String hints) {
    this.hints = hints;
  }

  protected void setPermissionsSql(final String permissionsSql) {
    this.permissionsSql = permissionsSql;
  }

  public void setPrimaryKeySql(final String primaryKeySql) {
    this.primaryKeySql = primaryKeySql;
  }

  public void setSqlPrefix(final String sqlPrefix) {
    this.sqlPrefix = sqlPrefix;
  }

  public void setSqlSuffix(final String sqlSuffix) {
    this.sqlSuffix = sqlSuffix;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void update(final DataObject object) {
    final JdbcWriter writer = getWriter();
    try {
      writer.write(object);
    } finally {
      writer.close();
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Override
  public void updateAll(final Collection<DataObject> objects) {
    final JdbcWriter writer = getWriter();
    try {
      for (final DataObject object : objects) {
        writer.write(object);
      }
    } finally {
      writer.close();
    }
  }
}
