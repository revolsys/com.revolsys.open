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
import java.util.Map.Entry;
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

import com.revolsys.collection.ResultPager;
import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.Maps;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.codes.AbstractCodeTable;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.io.RecordStoreExtension;
import com.revolsys.data.record.io.RecordStoreQueryReader;
import com.revolsys.data.record.property.GlobalIdProperty;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.record.schema.RecordStoreSchemaElement;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.Path;
import com.revolsys.io.PathName;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.transaction.Transaction;

public abstract class AbstractJdbcRecordStore extends AbstractRecordStore
  implements JdbcRecordStore, RecordStoreExtension {
  public static final List<String> DEFAULT_PERMISSIONS = Arrays.asList("SELECT");

  public static final AbstractIterator<Record> createJdbcIterator(
    final AbstractJdbcRecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    return new JdbcQueryIterator(recordStore, query, properties);
  }

  private final Set<String> allSchemaNames = new TreeSet<String>();

  private final Map<String, JdbcFieldAdder> attributeAdders = new HashMap<String, JdbcFieldAdder>();

  private int batchSize;

  private JdbcDatabaseFactory databaseFactory;

  private DataSource dataSource;

  private final Object exceptionWriterKey = new Object();

  private Set<String> excludeTablePaths = new HashSet<String>();

  private List<String> excludeTablePatterns = new ArrayList<String>();

  private boolean flushBetweenTypes;

  private String hints;

  private String primaryKeySql;

  private String primaryKeyTableCondition;

  private final Map<PathName, String> schemaNameMap = new HashMap<>();

  private String schemaPermissionsSql;

  private String schemaTablePermissionsSql;

  private final Map<String, String> sequenceTypeSqlMap = new HashMap<String, String>();

  private String sqlPrefix;

  private String sqlSuffix;

  private final Map<PathName, String> tableNameMap = new HashMap<>();

  private final Map<PathName, String> qualifiedTableNameMap = new HashMap<>();

  private String tablePermissionsSql;

  private DataSourceTransactionManager transactionManager;

  private final Object writerKey = new Object();

  public AbstractJdbcRecordStore() {
    this(new ArrayRecordFactory());
  }

  public AbstractJdbcRecordStore(final DataSource dataSource) {
    this();
    setDataSource(dataSource);
  }

  public AbstractJdbcRecordStore(final JdbcDatabaseFactory databaseFactory) {
    this(databaseFactory, new ArrayRecordFactory());
  }

  public AbstractJdbcRecordStore(final JdbcDatabaseFactory databaseFactory,
    final RecordFactory recordFactory) {
    this(recordFactory);
    this.databaseFactory = databaseFactory;
  }

  public AbstractJdbcRecordStore(final RecordFactory recordFactory) {
    super(recordFactory);
    setIteratorFactory(
      new RecordStoreIteratorFactory(AbstractJdbcRecordStore.class, "createJdbcIterator"));
    addRecordStoreExtension(this);
  }

  protected void addAllSchemaNames(final String schemaName) {
    this.allSchemaNames.add(schemaName.toUpperCase());
  }

  public void addExcludeTablePaths(final String tableName) {
    addExcludeTablePaths(tableName);
  }

  protected JdbcFieldDefinition addField(final RecordDefinitionImpl recordDefinition,
    final String dbColumnName, final String name, final String dataType, final int sqlType,
    final int length, final int scale, final boolean required, final String description) {
    JdbcFieldAdder attributeAdder = this.attributeAdders.get(dataType);
    if (attributeAdder == null) {
      attributeAdder = new JdbcFieldAdder(DataTypes.OBJECT);
    }
    return (JdbcFieldDefinition)attributeAdder.addField(this, recordDefinition, dbColumnName, name,
      dataType, sqlType, length, scale, required, description);
  }

  protected void addField(final ResultSetMetaData resultSetMetaData,
    final RecordDefinitionImpl recordDefinition, final String name, final int i,
    final String description) throws SQLException {
    final String dataType = resultSetMetaData.getColumnTypeName(i);
    final int sqlType = resultSetMetaData.getColumnType(i);
    final int length = resultSetMetaData.getPrecision(i);
    final int scale = resultSetMetaData.getScale(i);
    final boolean required = false;
    addField(recordDefinition, name, name.toUpperCase(), dataType, sqlType, length, scale, required,
      description);
  }

  public void addFieldAdder(final String sqlTypeName, final JdbcFieldAdder adder) {
    this.attributeAdders.put(sqlTypeName, adder);
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    try {
      super.close();
      if (this.databaseFactory != null && this.dataSource != null) {
        this.databaseFactory.closeDataSource(this.dataSource);
      }
    } finally {
      this.allSchemaNames.clear();
      this.attributeAdders.clear();
      this.transactionManager = null;
      this.databaseFactory = null;
      this.dataSource = null;
      this.excludeTablePatterns.clear();
      this.hints = null;
      this.schemaNameMap.clear();
      this.sequenceTypeSqlMap.clear();
      this.sqlPrefix = null;
      this.sqlSuffix = null;
      this.tableNameMap.clear();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T createPrimaryIdValue(final PathName typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    final GlobalIdProperty globalIdProperty = GlobalIdProperty.getProperty(recordDefinition);
    if (globalIdProperty == null) {
      return (T)getNextPrimaryKey(recordDefinition);
    } else {
      return (T)UUID.randomUUID().toString();
    }
  }

  protected RecordStoreQueryReader createReader(final Query query) {
    final RecordStoreQueryReader reader = createReader();
    reader.addQuery(query);
    return reader;
  }

  @Override
  public JdbcWriter createWriter() {
    final int size = this.batchSize;
    return createWriter(size);
  }

  public JdbcWriter createWriter(final int batchSize) {
    final JdbcWriterImpl writer = new JdbcWriterImpl(this);
    writer.setSqlPrefix(this.sqlPrefix);
    writer.setSqlSuffix(this.sqlSuffix);
    writer.setBatchSize(batchSize);
    writer.setHints(this.hints);
    writer.setLabel(getLabel());
    writer.setFlushBetweenTypes(this.flushBetweenTypes);
    writer.setQuoteColumnNames(false);
    return writer;
  }

  @Override
  public int delete(final Query query) {
    final String typeName = query.getTypeName();
    RecordDefinition recordDefinition = query.getRecordDefinition();
    if (recordDefinition == null) {
      if (typeName != null) {
        recordDefinition = getRecordDefinition(typeName);
        query.setRecordDefinition(recordDefinition);
      }
    }
    final String sql = JdbcUtils.getDeleteSql(query);
    try (
      Transaction transaction = createTransaction(com.revolsys.transaction.Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try (
        JdbcConnection connection = getJdbcConnection(isAutoCommit());
        final PreparedStatement statement = connection.prepareStatement(sql)) {

        JdbcUtils.setPreparedStatementParameters(statement, query);
        return statement.executeUpdate();
      } catch (final SQLException e) {
        transaction.setRollbackOnly();
        throw new RuntimeException("Unable to delete : " + sql, e);
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
  public void delete(final Record record) {
    final RecordState state = RecordState.Deleted;
    write(record, state);
  }

  @Override
  public void deleteAll(final Collection<Record> records) {
    writeAll(records, RecordState.Deleted);
  }

  public Set<String> getAllSchemaNames() {
    return this.allSchemaNames;
  }

  public int getBatchSize() {
    return this.batchSize;
  }

  public List<String> getColumnNames(final String typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    return recordDefinition.getFieldNames();
  }

  @Override
  public String getDatabaseQualifiedTableName(final PathName typePath) {
    return this.qualifiedTableNameMap.get(typePath);
  }

  @Override
  public String getDatabaseSchemaName(final PathName schemaPath) {
    return this.schemaNameMap.get(schemaPath);
  }

  public String getDatabaseSchemaName(final RecordStoreSchema schema) {
    if (schema == null) {
      return null;
    } else {
      final PathName schemaPath = schema.getPathName();
      return getDatabaseSchemaName(schemaPath);
    }
  }

  protected Set<String> getDatabaseSchemaNames() {
    final Set<String> schemaNames = new TreeSet<String>();
    try {
      try (
        final Connection connection = getJdbcConnection();
        final PreparedStatement statement = connection.prepareStatement(this.schemaPermissionsSql);
        final ResultSet resultSet = statement.executeQuery();) {
        while (resultSet.next()) {
          final String schemaName = resultSet.getString("SCHEMA_NAME");
          addAllSchemaNames(schemaName);
          if (!isSchemaExcluded(schemaName)) {
            schemaNames.add(schemaName);
          }
        }
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Unable to get schema and table permissions", e);
    }
    return schemaNames;
  }

  @Override
  public String getDatabaseTableName(final PathName typePath) {
    return this.tableNameMap.get(typePath);
  }

  protected DataSource getDataSource() {
    return this.dataSource;
  }

  public Set<String> getExcludeTablePaths() {
    return this.excludeTablePaths;
  }

  public JdbcFieldDefinition getField(final String schemaName, final String tableName,
    final String columnName) {
    final String typePath = Path.toPath(schemaName, tableName);
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      final FieldDefinition attribute = recordDefinition.getField(columnName);
      return (JdbcFieldDefinition)attribute;
    }
  }

  @Override
  public String getGeneratePrimaryKeySql(final RecordDefinition recordDefinition) {
    throw new UnsupportedOperationException(
      "Cannot create SQL to generate Primary Key for " + recordDefinition);
  }

  public String getHints() {
    return this.hints;
  }

  public String getIdFieldName(final String typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getIdFieldName();
    }
  }

  @Override
  public JdbcConnection getJdbcConnection() {
    return new JdbcConnection(this.dataSource);
  }

  @Override
  public JdbcConnection getJdbcConnection(final boolean autoCommit) {
    return new JdbcConnection(this.dataSource, autoCommit);
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath,
    final ResultSetMetaData resultSetMetaData) {
    try {
      final PathName pathName = PathName.create(typePath);
      final PathName schemaName = pathName.getParent();
      final RecordStoreSchema schema = getSchema(schemaName);
      final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(schema, pathName);

      final String idFieldName = getIdFieldName(typePath);
      for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
        final String name = resultSetMetaData.getColumnName(i).toUpperCase();
        if (name.equals(idFieldName)) {
          recordDefinition.setIdFieldIndex(i - 1);
        }
        addField(resultSetMetaData, recordDefinition, name, i, null);
      }

      addRecordDefinitionProperties(recordDefinition);

      return recordDefinition;
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to load metadata for " + typePath);
    }
  }

  @Override
  public int getRowCount(Query query) {
    query = query.clone();
    query.setSql(null);
    query.setFieldNames("count(*)");
    query.setOrderBy(Collections.<String, Boolean> emptyMap());
    final String sql = JdbcUtils.getSelectSql(query);
    try (
      JdbcConnection connection = getJdbcConnection()) {
      try (
        final PreparedStatement statement = connection.prepareStatement(sql)) {
        JdbcUtils.setPreparedStatementParameters(statement, query);
        try (
          final ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            final int rowCount = resultSet.getInt(1);
            return rowCount;
          } else {
            return 0;
          }
        }
      } catch (final SQLException e) {
        throw connection.getException("getRowCount", sql, e);
      }
    }
  }

  public String getSchemaTablePermissionsSql() {
    return this.schemaTablePermissionsSql;
  }

  protected String getSequenceInsertSql(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final String tableName = JdbcUtils.getQualifiedTableName(typePath);
    String sql = this.sequenceTypeSqlMap.get(typePath);
    if (sql == null) {
      final StringBuilder sqlBuffer = new StringBuilder();
      sqlBuffer.append("insert ");

      sqlBuffer.append(" into ");
      sqlBuffer.append(tableName);
      sqlBuffer.append(" (");
      sqlBuffer.append('"').append(recordDefinition.getIdFieldName()).append('"');
      sqlBuffer.append(",");
      for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
        if (i != recordDefinition.getIdFieldIndex()) {
          final String fieldName = recordDefinition.getFieldName(i);
          sqlBuffer.append('"').append(fieldName).append('"');
          if (i < recordDefinition.getFieldCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(") VALUES (");
      sqlBuffer.append(getGeneratePrimaryKeySql(recordDefinition));
      sqlBuffer.append(",");
      for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
        if (i != recordDefinition.getIdFieldIndex()) {
          sqlBuffer.append("?");
          if (i < recordDefinition.getFieldCount() - 1) {
            sqlBuffer.append(", ");
          }
        }
      }
      sqlBuffer.append(")");
      sql = sqlBuffer.toString();
      this.sequenceTypeSqlMap.put(typePath, sql);
    }
    return sql;
  }

  public String getSqlPrefix() {
    return this.sqlPrefix;
  }

  public String getSqlSuffix() {
    return this.sqlSuffix;
  }

  @Override
  public PlatformTransactionManager getTransactionManager() {
    return this.transactionManager;
  }

  @Override
  public JdbcWriter getWriter() {
    return getWriter(false);
  }

  @Override
  public JdbcWriter getWriter(final boolean throwExceptions) {
    Object writerKey;
    if (throwExceptions) {
      writerKey = this.exceptionWriterKey;
    } else {
      writerKey = this.writerKey;
    }
    JdbcWriterImpl writer;
    final JdbcWriterResourceHolder resourceHolder = (JdbcWriterResourceHolder)TransactionSynchronizationManager
      .getResource(writerKey);
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
        final JdbcWriterSynchronization synchronization = new JdbcWriterSynchronization(this,
          holderToUse, writerKey);
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
    if (this.dataSource != null) {
      this.transactionManager = new DataSourceTransactionManager(this.dataSource);
    }
  }

  @Override
  public void initialize(final RecordStore recordStore,
    final Map<String, Object> connectionProperties) {
  }

  @Override
  public void insert(final Record record) {
    write(record, RecordState.New);
  }

  @Override
  public void insertAll(final Collection<Record> records) {
    writeAll(records, RecordState.New);
  }

  public boolean isAutoCommit() {
    boolean autoCommit = false;
    if (BooleanStringConverter.getBoolean(getProperties().get("autoCommit"))) {
      autoCommit = true;
    }
    return autoCommit;
  }

  @Override
  public boolean isEditable(final String typePath) {
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    return recordDefinition.getIdFieldIndex() != -1;
  }

  @Override
  public boolean isEnabled(final RecordStore recordStore) {
    return true;
  }

  protected boolean isExcluded(final String dbSchemaName, final String tableName) {
    final String path = ("/" + dbSchemaName + "/" + tableName).toUpperCase().replaceAll("/+", "/");
    if (this.excludeTablePaths.contains(path)) {
      return true;
    } else {
      for (final String pattern : this.excludeTablePatterns) {
        if (path.matches(pattern) || tableName.matches(pattern)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isFlushBetweenTypes() {
    return this.flushBetweenTypes;
  }

  public abstract boolean isSchemaExcluded(String schemaName);

  protected synchronized Map<String, List<String>> loadIdFieldNames(final String dbSchemaName) {
    final String schemaName = "/" + dbSchemaName.toUpperCase();
    final Map<String, List<String>> idFieldNames = new HashMap<>();
    try {
      try (
        final Connection connection = getJdbcConnection();
        final PreparedStatement statement = connection.prepareStatement(this.primaryKeySql);) {
        statement.setString(1, dbSchemaName);
        try (
          final ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            final String tableName = rs.getString("TABLE_NAME").toUpperCase();
            final String idFieldName = rs.getString("COLUMN_NAME");
            Maps.addToList(idFieldNames, schemaName + "/" + tableName, idFieldName);
          }
        }
      }
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to primary keys for schema " + dbSchemaName, e);
    }
    return idFieldNames;
  }

  protected synchronized List<String> loadIdFieldNames(final String dbSchemaName,
    final String dbTableName) {
    final List<String> idFieldNames = new ArrayList<>();
    try {
      try (
        final Connection connection = getJdbcConnection();
        final PreparedStatement statement = connection
          .prepareStatement(this.primaryKeySql + this.primaryKeyTableCondition);) {
        statement.setString(1, dbSchemaName);
        statement.setString(2, dbTableName);
        try (
          final ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            final String idFieldName = rs.getString("COLUMN_NAME");
            idFieldNames.add(idFieldName);
          }
        }
      }
    } catch (final Throwable e) {
      throw new IllegalArgumentException(
        "Unable to primary keys for table " + dbSchemaName + "." + dbTableName, e);
    }
    return idFieldNames;
  }

  protected void loadSchemaTablePermissions(final String schemaName,
    final Map<String, List<String>> tablePermissionsMap,
    final Map<String, String> tableDescriptionMap) {
    try (
      final Connection connection = getJdbcConnection();
      final PreparedStatement statement = connection
        .prepareStatement(this.schemaTablePermissionsSql)) {
      statement.setString(1, schemaName);
      try (
        final ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          final String dbTableName = resultSet.getString("TABLE_NAME");
          if (!isExcluded(schemaName, dbTableName)) {
            final String privilege = resultSet.getString("PRIVILEGE");
            if ("ALL".equals(privilege)) {
              Maps.addToList(tablePermissionsMap, dbTableName, "SELECT");
              Maps.addToList(tablePermissionsMap, dbTableName, "INSERT");
              Maps.addToList(tablePermissionsMap, dbTableName, "UPDATE");
              Maps.addToList(tablePermissionsMap, dbTableName, "DELETE");
            } else {
              Maps.addToList(tablePermissionsMap, dbTableName, privilege);
            }

            final String description = resultSet.getString("REMARKS");
            tableDescriptionMap.put(dbTableName, description);
          }
        }
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Unable to get schema and table permissions", e);
    }
  }

  protected void loadSchemaTablePermissions(final String schemaName, final String tableName,
    final List<String> tablePermissions, final Map<String, String> tableDescriptionMap) {
    try (
      final Connection connection = getJdbcConnection();
      final PreparedStatement statement = connection.prepareStatement(this.tablePermissionsSql)) {
      statement.setString(1, schemaName);
      statement.setString(2, tableName);
      try (
        final ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          final String dbTableName = resultSet.getString("TABLE_NAME");
          if (!isExcluded(schemaName, dbTableName)) {
            final String privilege = resultSet.getString("PRIVILEGE");
            if ("ALL".equals(privilege)) {
              tablePermissions.add("SELECT");
              tablePermissions.add("INSERT");
              tablePermissions.add("UPDATE");
              tablePermissions.add("DELETE");
            } else {
              tablePermissions.add(privilege);
            }

            final String description = resultSet.getString("REMARKS");
            tableDescriptionMap.put(dbTableName, description);
          }
        }
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass())
        .error("Unable to get table permissions for " + schemaName + "." + tableName, e);
    }
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    return new JdbcQueryResultPager(this, getProperties(), query);
  }

  @Override
  public void postProcess(final RecordStoreSchema schema) {
  }

  @Override
  public void preProcess(final RecordStoreSchema schema) {
    for (final JdbcFieldAdder attributeAdder : this.attributeAdders.values()) {
      attributeAdder.initialize(schema);
    }
  }

  @Override
  protected synchronized RecordDefinition refreshRecordDefinition(final RecordStoreSchema schema,
    final PathName typePath) {
    final List<String> pathElements = typePath.getElements();
    if (pathElements.size() == 2) {
      final String schemaName = pathElements.get(0).toUpperCase();
      final String tableName = pathElements.get(1).toUpperCase();

      final Map<String, String> tableDescriptionMap = new HashMap<>();
      final List<String> tablePermissions = new ArrayList<>();
      loadSchemaTablePermissions(schemaName, tableName, tablePermissions, tableDescriptionMap);
      if (tableDescriptionMap.isEmpty()) {
        return null;
      } else {
        final PathName schemaPath = schema.getPathName();
        final String dbSchemaName = getDatabaseSchemaName(schemaPath);
        final Entry<String, String> descriptionEntry = tableDescriptionMap.entrySet()
          .iterator()
          .next();
        final String dbTableName = descriptionEntry.getKey();
        final String tableDescription = descriptionEntry.getValue();
        try {
          try (
            final Connection connection = getJdbcConnection()) {
            final DatabaseMetaData databaseMetaData = connection.getMetaData();
            final List<String> idFieldNames = loadIdFieldNames(dbSchemaName, dbTableName);
            final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(schema,
              typePath);
            recordDefinition.setDescription(tableDescription);
            recordDefinition.setProperty("permissions", tablePermissions);
            try (
              final ResultSet columnsRs = databaseMetaData.getColumns(null, dbSchemaName, tableName,
                "%")) {
              while (columnsRs.next()) {
                final String dbColumnName = columnsRs.getString("COLUMN_NAME");
                final String name = dbColumnName.toUpperCase();
                final int sqlType = columnsRs.getInt("DATA_TYPE");
                final String dataType = columnsRs.getString("TYPE_NAME");
                final int length = columnsRs.getInt("COLUMN_SIZE");
                int scale = columnsRs.getInt("DECIMAL_DIGITS");
                if (columnsRs.wasNull()) {
                  scale = -1;
                }
                final boolean required = !columnsRs.getString("IS_NULLABLE").equals("YES");
                final String fieldDescription = columnsRs.getString("REMARKS");
                addField(recordDefinition, dbColumnName, name, dataType, sqlType, length, scale,
                  required, fieldDescription);
              }
            }
            recordDefinition.setIdFieldNames(idFieldNames);
            return recordDefinition;
          }
        } catch (final Throwable e) {
          throw new IllegalArgumentException("Unable to load metadata for schema " + schemaName, e);
        }
      }
    } else {
      return null;
    }
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final RecordStoreSchema rootSchema = getRootSchema();
    final PathName schemaPath = schema.getPathName();
    if (schema == rootSchema) {
      final Map<PathName, RecordStoreSchemaElement> schemas = new TreeMap<>();
      final Set<String> databaseSchemaNames = getDatabaseSchemaNames();
      for (final String dbSchemaName : databaseSchemaNames) {
        final PathName childSchemaPath = schemaPath.createChild(dbSchemaName.toUpperCase());
        this.schemaNameMap.put(childSchemaPath, dbSchemaName);
        RecordStoreSchema childSchema = schema.getSchema(childSchemaPath);
        if (childSchema == null) {
          childSchema = new RecordStoreSchema(rootSchema, childSchemaPath);
        } else {
          if (childSchema.isInitialized()) {
            childSchema.refresh();
          }
        }
        schemas.put(childSchemaPath, childSchema);
      }
      return schemas;
    } else {
      final String schemaName = schema.getPath();
      final String dbSchemaName = getDatabaseSchemaName(schemaPath);
      final Map<String, String> tableDescriptionMap = new HashMap<String, String>();
      final Map<String, List<String>> tablePermissionsMap = new TreeMap<String, List<String>>();
      loadSchemaTablePermissions(dbSchemaName, tablePermissionsMap, tableDescriptionMap);

      final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
      final Map<PathName, RecordDefinition> recordDefinitionMap = new TreeMap<>();
      try {
        try (
          final Connection connection = getJdbcConnection()) {
          final DatabaseMetaData databaseMetaData = connection.getMetaData();
          final List<PathName> removedPaths = schema.getTypePaths();
          final Map<String, List<String>> idFieldNameMap = loadIdFieldNames(dbSchemaName);
          final Set<String> tableNames = tablePermissionsMap.keySet();
          for (final String dbTableName : tableNames) {
            final String tableName = dbTableName.toUpperCase();
            final PathName typePath = schemaPath.createChild(tableName);
            removedPaths.remove(typePath);
            this.tableNameMap.put(typePath, dbTableName);
            this.qualifiedTableNameMap.put(typePath, dbSchemaName + "." + dbTableName);
            final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(schema,
              typePath);
            final String description = tableDescriptionMap.get(dbTableName);
            recordDefinition.setDescription(description);
            final List<String> permissions = Maps.get(tablePermissionsMap, dbTableName,
              DEFAULT_PERMISSIONS);
            recordDefinition.setProperty("permissions", permissions);
            recordDefinitionMap.put(typePath, recordDefinition);
            elementsByPath.put(typePath, recordDefinition);
          }
          try (
            final ResultSet columnsRs = databaseMetaData.getColumns(null, dbSchemaName, "%", "%")) {
            while (columnsRs.next()) {
              final String tableName = columnsRs.getString("TABLE_NAME").toUpperCase();
              final PathName typePath = schemaPath.createChild(tableName);
              final RecordDefinitionImpl recordDefinition = (RecordDefinitionImpl)recordDefinitionMap
                .get(typePath);
              if (recordDefinition != null) {
                final String dbColumnName = columnsRs.getString("COLUMN_NAME");
                final String name = dbColumnName.toUpperCase();
                final int sqlType = columnsRs.getInt("DATA_TYPE");
                final String dataType = columnsRs.getString("TYPE_NAME");
                final int length = columnsRs.getInt("COLUMN_SIZE");
                int scale = columnsRs.getInt("DECIMAL_DIGITS");
                if (columnsRs.wasNull()) {
                  scale = -1;
                }
                final boolean required = !columnsRs.getString("IS_NULLABLE").equals("YES");
                final String description = columnsRs.getString("REMARKS");
                addField(recordDefinition, dbColumnName, name, dataType, sqlType, length, scale,
                  required, description);
              }
            }

            for (final RecordDefinition recordDefinition : recordDefinitionMap.values()) {
              final String typePath = recordDefinition.getPath();
              final List<String> idFieldNames = idFieldNameMap.get(typePath);
              ((RecordDefinitionImpl)recordDefinition).setIdFieldNames(idFieldNames);
            }

          }
        }
      } catch (final Throwable e) {
        throw new IllegalArgumentException("Unable to load metadata for schema " + schemaName, e);
      }

      return elementsByPath;
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
    this.excludeTablePatterns = new ArrayList<String>(Arrays.asList(excludeTablePatterns));
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

  public void setPrimaryKeyTableCondition(final String primaryKeyTableCondition) {
    this.primaryKeyTableCondition = primaryKeyTableCondition;
  }

  protected void setSchemaPermissionsSql(final String scehmaPermissionsSql) {
    this.schemaPermissionsSql = scehmaPermissionsSql;
  }

  public void setSchemaTablePermissionsSql(final String tablePermissionsSql) {
    this.schemaTablePermissionsSql = tablePermissionsSql;
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
  public void update(final Record record) {
    write(record, null);
  }

  @Override
  public void updateAll(final Collection<Record> records) {
    writeAll(records, null);
  }

  protected Record write(final JdbcWriter writer, Record record, final RecordState state) {
    if (state == RecordState.New) {
      if (record.getState() != state) {
        record = copy(record);
      }
    } else if (state != null) {
      record.setState(state);
    }
    writer.write(record);
    return record;
  }

  protected void write(final Record record, final RecordState state) {
    try (
      Transaction transaction = createTransaction(com.revolsys.transaction.Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try (
        JdbcWriter writer = getWriter(true)) {
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

  protected void writeAll(final Collection<Record> records, final RecordState state) {
    try (
      Transaction transaction = createTransaction(com.revolsys.transaction.Propagation.REQUIRED)) {
      // It's important to have this in an inner try. Otherwise the exceptions
      // won't get caught on closing the writer and the transaction won't get
      // rolled back.
      try (
        final JdbcWriter writer = getWriter(true)) {
        for (final Record record : records) {
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
