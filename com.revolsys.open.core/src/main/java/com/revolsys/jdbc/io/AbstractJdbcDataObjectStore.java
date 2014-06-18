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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ResultPager;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreExtension;
import com.revolsys.gis.data.io.DataObjectStoreQueryReader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.GlobalIdProperty;
import com.revolsys.gis.data.model.codes.AbstractCodeTable;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.CollectionUtil;

public abstract class AbstractJdbcDataObjectStore extends
  AbstractDataObjectStore implements JdbcDataObjectStore,
  DataObjectStoreExtension {
  public static final List<String> DEFAULT_PERMISSIONS = Arrays.asList("SELECT");

  public static final AbstractIterator<DataObject> createJdbcIterator(
    final AbstractJdbcDataObjectStore dataStore, final Query query,
    final Map<String, Object> properties) {
    return new JdbcQueryIterator(dataStore, query, properties);
  }

  private final Map<String, JdbcAttributeAdder> attributeAdders = new HashMap<String, JdbcAttributeAdder>();

  private int batchSize;

  private Connection connection;

  private DataSource dataSource;

  private List<String> excludeTablePatterns = new ArrayList<String>();

  private boolean flushBetweenTypes;

  private String hints;

  private final Map<String, String> sequenceTypeSqlMap = new HashMap<String, String>();

  private String sqlPrefix;

  private String sqlSuffix;

  private String schemaPermissionsSql;

  private final Map<String, String> schemaNameMap = new HashMap<String, String>();

  private final Map<String, String> tableNameMap = new HashMap<String, String>();

  private JdbcDatabaseFactory databaseFactory;

  private final Object writerKey = new Object();

  private final Object exceptionWriterKey = new Object();

  private String primaryKeySql;

  private final Set<String> allSchemaNames = new TreeSet<String>();

  private String tablePermissionsSql;

  private Set<String> excludeTablePaths = new HashSet<String>();

  private DataSourceTransactionManager transactionManager;

  public AbstractJdbcDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public AbstractJdbcDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
    setIteratorFactory(new DataStoreIteratorFactory(
      AbstractJdbcDataObjectStore.class, "createJdbcIterator"));
    addDataStoreExtension(this);
  }

  public AbstractJdbcDataObjectStore(final DataSource dataSource) {
    this();
    setDataSource(dataSource);
  }

  public AbstractJdbcDataObjectStore(final JdbcDatabaseFactory databaseFactory) {
    this(databaseFactory, new ArrayDataObjectFactory());
  }

  public AbstractJdbcDataObjectStore(final JdbcDatabaseFactory databaseFactory,
    final DataObjectFactory dataObjectFactory) {
    this(dataObjectFactory);
    this.databaseFactory = databaseFactory;
  }

  protected void addAllSchemaNames(final String schemaName) {
    allSchemaNames.add(schemaName.toUpperCase());
  }

  protected JdbcAttribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String dbColumnName, final String name, final String dataType,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    JdbcAttributeAdder attributeAdder = attributeAdders.get(dataType);
    if (attributeAdder == null) {
      attributeAdder = new JdbcAttributeAdder(DataTypes.OBJECT);
    }
    return (JdbcAttribute)attributeAdder.addAttribute(metaData, dbColumnName,
      name, dataType, sqlType, length, scale, required, description);
  }

  protected void addAttribute(final ResultSetMetaData resultSetMetaData,
    final DataObjectMetaDataImpl metaData, final String name, final int i,
    final String description) throws SQLException {
    final String dataType = resultSetMetaData.getColumnTypeName(i);
    final int sqlType = resultSetMetaData.getColumnType(i);
    final int length = resultSetMetaData.getPrecision(i);
    final int scale = resultSetMetaData.getScale(i);
    final boolean required = false;
    addAttribute(metaData, name, name.toUpperCase(), dataType, sqlType, length,
      scale, required, description);
  }

  public void addAttributeAdder(final String sqlTypeName,
    final JdbcAttributeAdder adder) {
    attributeAdders.put(sqlTypeName, adder);
  }

  public void addExcludeTablePaths(final String tableName) {
    addExcludeTablePaths(tableName);
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
      allSchemaNames.clear();
      attributeAdders.clear();
      transactionManager = null;
      connection = null;
      databaseFactory = null;
      dataSource = null;
      excludeTablePatterns.clear();
      hints = null;
      schemaNameMap.clear();
      sequenceTypeSqlMap.clear();
      sqlPrefix = null;
      sqlSuffix = null;
      tableNameMap.clear();
    }
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
    final JdbcWriterImpl writer = new JdbcWriterImpl(this);
    writer.setSqlPrefix(sqlPrefix);
    writer.setSqlSuffix(sqlSuffix);
    writer.setBatchSize(batchSize);
    writer.setHints(hints);
    writer.setLabel(getLabel());
    writer.setFlushBetweenTypes(flushBetweenTypes);
    writer.setQuoteColumnNames(false);
    return writer;
  }

  @Override
  public void delete(final DataObject record) {
    final DataObjectState state = DataObjectState.Deleted;
    write(record, state);
  }

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
    try (Transaction transaction = createTransaction(com.revolsys.transaction.Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try {
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
              throw new IllegalArgumentException("Unable to create connection",
                e);
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
      } catch (final RuntimeException e) {
        transaction.setRollbackOnly();
        throw e;
      } catch (final Error e) {
        transaction.setRollbackOnly();
        throw e;
      }
    }
  }

  @Override
  public void deleteAll(final Collection<DataObject> records) {
    writeAll(records, DataObjectState.Deleted);
  }

  public Set<String> getAllSchemaNames() {
    return allSchemaNames;
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

  @Override
  public String getDatabaseQualifiedTableName(final String typePath) {
    final String schema = getDatabaseSchemaName(PathUtil.getPath(typePath));
    final String tableName = getDatabaseTableName(typePath);
    return schema + "." + tableName;
  }

  public String getDatabaseSchemaName(final DataObjectStoreSchema schema) {
    if (schema == null) {
      return null;
    } else {
      final String schemaPath = schema.getPath();
      return getDatabaseSchemaName(schemaPath);
    }
  }

  @Override
  public String getDatabaseSchemaName(final String schemaPath) {
    return schemaNameMap.get(schemaPath);
  }

  protected Set<String> getDatabaseSchemaNames() {
    final Set<String> schemaNames = new TreeSet<String>();
    try {
      final Connection connection = getDbConnection();
      try {
        final PreparedStatement statement = connection.prepareStatement(schemaPermissionsSql);
        final ResultSet resultSet = statement.executeQuery();

        try {
          while (resultSet.next()) {
            final String schemaName = resultSet.getString("SCHEMA_NAME");
            addAllSchemaNames(schemaName);
            if (!isSchemaExcluded(schemaName)) {
              schemaNames.add(schemaName);
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
    return schemaNames;
  }

  @Override
  public String getDatabaseTableName(final String typePath) {
    return tableNameMap.get(typePath);
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

  public Set<String> getExcludeTablePaths() {
    return excludeTablePaths;
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
        addAttribute(resultSetMetaData, metaData, name, i, null);
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
            final int rowCount = resultSet.getInt(1);
            return rowCount;
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

  public Connection getSqlConnection() {
    final DataSource dataSource = getDataSource();
    if (dataSource == null) {
      return getConnection();
    } else {
      return JdbcUtils.getConnection(dataSource);
    }
  }

  public String getSqlPrefix() {
    return sqlPrefix;
  }

  public String getSqlSuffix() {
    return sqlSuffix;
  }

  public String getTablePermissionsSql() {
    return tablePermissionsSql;
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }

  @Override
  public JdbcWriter getWriter() {
    return getWriter(false);
  }

  @Override
  public JdbcWriter getWriter(final boolean throwExceptions) {
    Object writerKey;
    if (throwExceptions) {
      writerKey = exceptionWriterKey;
    } else {
      writerKey = this.writerKey;
    }
    JdbcWriterImpl writer;
    final JdbcWriterResourceHolder resourceHolder = (JdbcWriterResourceHolder)TransactionSynchronizationManager.getResource(writerKey);
    if (resourceHolder != null
      && (resourceHolder.hasWriter() || resourceHolder.isSynchronizedWithTransaction())) {
      resourceHolder.requested();
      if (resourceHolder.hasWriter()) {
        writer = resourceHolder.getWriter();
      } else {
        writer = (JdbcWriterImpl)createWriter(1);
        resourceHolder.setWriter(writer);
      }
    } else {
      writer = (JdbcWriterImpl)createWriter(1);
      writer.setThrowExceptions(throwExceptions);
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
    }
    return new JdbcWriterWrapper(writer);
  }

  @Override
  @PostConstruct
  public void initialize() {
    super.initialize();
    final DataSource dataSource = getDataSource();
    if (dataSource != null) {
      transactionManager = new DataSourceTransactionManager(dataSource);
    }
  }

  @Override
  public void initialize(final DataObjectStore dataStore,
    final Map<String, Object> connectionProperties) {
  }

  @Override
  public void insert(final DataObject record) {
    write(record, DataObjectState.New);
  }

  @Override
  public void insertAll(final Collection<DataObject> records) {
    writeAll(records, DataObjectState.New);
  }

  @Override
  public boolean isEditable(final String typePath) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    return metaData.getIdAttributeIndex() != -1;
  }

  @Override
  public boolean isEnabled(final DataObjectStore dataStore) {
    return true;
  }

  protected boolean isExcluded(final String dbSchemaName, final String tableName) {
    final String path = ("/" + dbSchemaName + "/" + tableName).toUpperCase()
      .replaceAll("/+", "/");
    if (excludeTablePaths.contains(path)) {
      return true;
    } else {
      for (final String pattern : excludeTablePatterns) {
        if (path.matches(pattern) || tableName.matches(pattern)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isFlushBetweenTypes() {
    return flushBetweenTypes;
  }

  public abstract boolean isSchemaExcluded(String schemaName);

  protected synchronized Map<String, List<String>> loadIdColumnNames(
    final String dbSchemaName) {
    final String schemaName = "/" + dbSchemaName.toUpperCase();
    final Map<String, List<String>> idColumnNames = new HashMap<String, List<String>>();
    final Connection connection = getDbConnection();
    try {
      final PreparedStatement statement = connection.prepareStatement(primaryKeySql);
      try {
        statement.setString(1, dbSchemaName);
        final ResultSet rs = statement.executeQuery();
        try {
          while (rs.next()) {
            final String tableName = rs.getString("TABLE_NAME").toUpperCase();
            final String idAttributeName = rs.getString("COLUMN_NAME");
            CollectionUtil.addToList(idColumnNames, schemaName + "/"
              + tableName, idAttributeName);
          }
        } finally {
          JdbcUtils.close(rs);
        }
      } finally {
        JdbcUtils.close(statement);
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
    final Map<String, String> tableDescriptionMap = new HashMap<String, String>();
    final Map<String, List<String>> tablePermissionsMap = new TreeMap<String, List<String>>();
    loadSchemaTablePermissions(dbSchemaName, tablePermissionsMap,
      tableDescriptionMap);

    final Connection connection = getDbConnection();
    try {
      final DatabaseMetaData databaseMetaData = connection.getMetaData();

      final Map<String, List<String>> idAttributeNameMap = loadIdColumnNames(dbSchemaName);
      final Set<String> tableNames = tablePermissionsMap.keySet();
      for (final String dbTableName : tableNames) {
        final String tableName = dbTableName.toUpperCase();
        final String typePath = PathUtil.toPath(schemaName, tableName);
        tableNameMap.put(typePath, dbTableName);
        final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
          this, schema, typePath);
        final String description = tableDescriptionMap.get(dbTableName);
        metaData.setDescription(description);
        final List<String> permissions = CollectionUtil.get(
          tablePermissionsMap, dbTableName, DEFAULT_PERMISSIONS);
        metaData.setProperty("permissions", permissions);
        metaDataMap.put(typePath, metaData);
      }

      try (final ResultSet columnsRs = databaseMetaData.getColumns(null,
        dbSchemaName, "%", "%")) {
        while (columnsRs.next()) {
          final String tableName = columnsRs.getString("TABLE_NAME")
            .toUpperCase();
          final String typePath = PathUtil.toPath(schemaName, tableName);
          final DataObjectMetaDataImpl metaData = (DataObjectMetaDataImpl)metaDataMap.get(typePath);
          if (metaData != null) {
            final String dbColumnName = columnsRs.getString("COLUMN_NAME");
            final String name = dbColumnName.toUpperCase();
            final int sqlType = columnsRs.getInt("DATA_TYPE");
            final String dataType = columnsRs.getString("TYPE_NAME");
            final int length = columnsRs.getInt("COLUMN_SIZE");
            int scale = columnsRs.getInt("DECIMAL_DIGITS");
            if (columnsRs.wasNull()) {
              scale = -1;
            }
            final boolean required = !columnsRs.getString("IS_NULLABLE")
              .equals("YES");
            final String description = columnsRs.getString("REMARKS");
            addAttribute(metaData, dbColumnName, name, dataType, sqlType,
              length, scale, required, description);
          }
        }

        for (final DataObjectMetaData metaData : metaDataMap.values()) {
          final String typePath = metaData.getPath();
          final List<String> idAttributeNames = idAttributeNameMap.get(typePath);
          ((DataObjectMetaDataImpl)metaData).setIdAttributeNames(idAttributeNames);
        }

      }

    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to load metadata for schema "
        + schemaName, e);
    } finally {
      releaseConnection(connection);
    }

    for (final DataObjectMetaData metaData : metaDataMap.values()) {
      addMetaDataProperties((DataObjectMetaDataImpl)metaData);
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

  protected void loadSchemaTablePermissions(final String schemaName,
    final Map<String, List<String>> tablePermissionsMap,
    final Map<String, String> tableDescriptionMap) {
    try {
      final Connection connection = getDbConnection();
      try {
        final PreparedStatement statement = connection.prepareStatement(tablePermissionsSql);
        statement.setString(1, schemaName);
        final ResultSet resultSet = statement.executeQuery();

        try {
          while (resultSet.next()) {
            final String dbTableName = resultSet.getString("TABLE_NAME");
            if (!isExcluded(schemaName, dbTableName)) {
              final String privilege = resultSet.getString("PRIVILEGE");
              CollectionUtil.addToList(tablePermissionsMap, dbTableName,
                privilege);

              final String description = resultSet.getString("REMARKS");
              tableDescriptionMap.put(dbTableName, description);
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
  }

  @Override
  public ResultPager<DataObject> page(final Query query) {
    return new JdbcQueryResultPager(this, getProperties(), query);
  }

  @Override
  public void postProcess(final DataObjectStoreSchema schema) {
  }

  @Override
  public void preProcess(final DataObjectStoreSchema schema) {
    for (final JdbcAttributeAdder attributeAdder : attributeAdders.values()) {
      attributeAdder.initialize(schema);
    }
  }

  protected void releaseConnection(final Connection connection) {
    JdbcUtils.release(connection, dataSource);
  }

  public void releaseSqlConnection(final Connection connection) {
    final DataSource dataSource = getDataSource();
    JdbcUtils.release(connection, dataSource);
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

  public void setExcludeTablePaths(final Collection<String> excludeTablePaths) {
    this.excludeTablePaths = new HashSet<String>(excludeTablePaths);
  }

  public void setExcludeTablePaths(final String... excludeTablePaths) {
    setExcludeTablePaths(Arrays.asList(excludeTablePaths));
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

  public void setPrimaryKeySql(final String primaryKeySql) {
    this.primaryKeySql = primaryKeySql;
  }

  protected void setSchemaPermissionsSql(final String scehmaPermissionsSql) {
    schemaPermissionsSql = scehmaPermissionsSql;
  }

  public void setSqlPrefix(final String sqlPrefix) {
    this.sqlPrefix = sqlPrefix;
  }

  public void setSqlSuffix(final String sqlSuffix) {
    this.sqlSuffix = sqlSuffix;
  }

  public void setTablePermissionsSql(final String tablePermissionsSql) {
    this.tablePermissionsSql = tablePermissionsSql;
  }

  @Override
  public void update(final DataObject record) {
    write(record, null);
  }

  @Override
  public void updateAll(final Collection<DataObject> records) {
    writeAll(records, null);
  }

  protected void write(final DataObject record, final DataObjectState state) {
    try (Transaction transaction = createTransaction(com.revolsys.transaction.Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try (JdbcWriter writer = getWriter(true)) {
        write(writer, record, state);
      } catch (final RuntimeException e) {
        transaction.setRollbackOnly();
        throw e;
      } catch (final Error e) {
        transaction.setRollbackOnly();
        throw e;
      }
    }
  }

  protected DataObject write(final JdbcWriter writer, DataObject record,
    final DataObjectState state) {
    if (state == DataObjectState.New) {
      if (record.getState() != state) {
        record = copy(record);
      }
    } else if (state == DataObjectState.Deleted) {
      final DataObjectState recordState = record.getState();
      if (recordState == DataObjectState.Deleted) {
        return record;
      } else {
        record.setState(state);
      }
    } else if (state != null) {
      record.setState(state);
    }
    writer.write(record);
    return record;
  }

  protected void writeAll(final Collection<DataObject> records,
    final DataObjectState state) {
    try (Transaction transaction = createTransaction(com.revolsys.transaction.Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try (final JdbcWriter writer = getWriter(true)) {
        for (final DataObject record : records) {
          write(writer, record, state);
        }
      } catch (final RuntimeException e) {
        transaction.setRollbackOnly();
        throw e;
      } catch (final Error e) {
        transaction.setRollbackOnly();
        throw e;
      }
    }
  }
}
