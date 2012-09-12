package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ResultPager;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectReader;
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
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractJdbcDataObjectStore extends
  AbstractDataObjectStore implements JdbcDataObjectStore {
  private Map<String, JdbcAttributeAdder> attributeAdders = new HashMap<String, JdbcAttributeAdder>();

  private int batchSize;

  private Connection connection;

  private DataSource dataSource;

  private String[] excludeTablePatterns = new String[0];

  private boolean flushBetweenTypes;

  private String hints;

  private Map<String, String> sequenceTypeSqlMap = new HashMap<String, String>();

  private String sqlPrefix;

  private String sqlSuffix;

  private List<String> tableTypes = Arrays.asList("VIEW", "TABLE");

  private Map<String, String> schemaNameMap = new HashMap<String, String>();

  private Map<String, String> tableNameMap = new HashMap<String, String>();

  private JdbcDatabaseFactory databaseFactory;

  private final Object writerKey = new Object();

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
      tableTypes = null;
    }
  }

  @Override
  protected AbstractIterator<DataObject> createIterator(Query query,
    final Map<String, Object> properties) {
    final BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      query = createBoundingBoxQuery(query, boundingBox);
    }
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

  protected DataObjectStoreQueryReader createReader(
    final DataObjectMetaData metaData, final String sql,
    final List<Object> parameters) {
    final Query query = new Query(metaData);
    query.setSql(sql);
    query.setParameters(parameters);
    return createReader(query);
  }

  protected DataObjectStoreQueryReader createReader(
    final DataObjectMetaData metaData, final String query,
    final Object... parameters) {
    return createReader(metaData, query, Arrays.asList(parameters));
  }

  protected DataObjectStoreQueryReader createReader(final Query query) {
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(query);
    return reader;
  }

  @Override
  public DataObjectReader createReader(final String typePath,
    final String query, final List<Object> parameters) {
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(typePath, query, parameters);
    return reader;
  }

  protected DataObjectReader createReader(final String typePath,
    final String whereClause, final Object... parameters) {
    return createReader(typePath, whereClause, Arrays.asList(parameters));
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
          if (getProperties().get("autoCommit") == Boolean.TRUE) {
            autoCommit = true;
          }
          connection.setAutoCommit(autoCommit);
        } catch (final SQLException e) {
          throw new IllegalArgumentException("Unable to create connection", e);
        }
      }

      final PreparedStatement statement = connection.prepareStatement(sql);
      try {
        JdbcUtils.setPreparedStatementFilterParameters(statement, query);
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

  public String getDatabaseSchemaName(final String schemaPath) {
    return schemaNameMap.get(schemaPath);
  }

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

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    throw new UnsupportedOperationException(
      "Cannot create SQL to generate Primary Key for " + metaData);
  }

  public String getHints() {
    return hints;
  }

  @Override
  public DataObjectMetaData getMetaData(final String typePath,
    final ResultSetMetaData resultSetMetaData) {
    try {
      final String schemaName = PathUtil.getPath(typePath);
      final DataObjectStoreSchema schema = getSchema(schemaName);
      final String dbSchema = getDatabaseSchemaName(schema);
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(this,
        schema, typePath);

      final String tableName = getDatabaseTableName(typePath);
      final String idColumnName = loadIdColumnName(dbSchema, tableName);
      for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
        final String name = resultSetMetaData.getColumnName(i).toUpperCase();
        if (name.equals(idColumnName)) {
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

  public boolean isFlushBetweenTypes() {
    return flushBetweenTypes;
  }

  private synchronized String loadIdColumnName(final String schemaName,
    final String tableName) {
    final Connection connection = getDbConnection();
    try {
      final DatabaseMetaData databaseMetaData = connection.getMetaData();
      final ResultSet rs = databaseMetaData.getPrimaryKeys(null, schemaName,
        tableName);
      try {
        if (rs.next()) {
          final String idColumnName = rs.getString("COLUMN_NAME");
          return idColumnName;
        } else {
          return null;
        }
      } finally {
        JdbcUtils.close(rs);
      }
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Unable to primary keys for schema "
        + schemaName, e);
    } finally {
      releaseConnection(connection);
    }
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
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

      final ResultSet tablesRs = databaseMetaData.getTables(null, dbSchemaName,
        "%", null);
      final Map<String, String> idColumnNames = new HashMap<String, String>();
      try {
        while (tablesRs.next()) {
          final String dbTableName = tablesRs.getString("TABLE_NAME");
          final String tableName = dbTableName.toUpperCase();
          final String tableType = tablesRs.getString("TABLE_TYPE");
          boolean excluded = !tableTypes.contains(tableType);
          for (final String pattern : excludeTablePatterns) {
            if (dbTableName.matches(pattern) || tableName.matches(pattern)) {
              excluded = true;
            }
          }
          if (!excluded) {
            final String typePath = PathUtil.toPath(schemaName, tableName);
            tableNameMap.put(typePath, dbTableName);
            final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
              this, schema, typePath);
            metaDataMap.put(typePath, metaData);
            final String idColumnName = loadIdColumnName(dbSchemaName,
              dbTableName);
            idColumnNames.put(typePath, idColumnName);

          }
        }
      } finally {
        JdbcUtils.close(tablesRs);
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
            if (name.equalsIgnoreCase(idColumnNames.get(typePath))) {
              metaData.setIdAttributeIndex(metaData.getAttributeCount());
            }
            addAttribute(metaData, name, dataType, sqlType, length, scale,
              required);
          }
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
    try {
      final Connection connection = getDbConnection();
      try {
        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        final ResultSet schemaRs = databaseMetaData.getSchemas();

        try {
          while (schemaRs.next()) {
            final String dbSchemaName = schemaRs.getString("TABLE_SCHEM");
            final String schemaName = "/" + dbSchemaName.toUpperCase();
            schemaNameMap.put(schemaName, dbSchemaName);
            final DataObjectStoreSchema schema = new DataObjectStoreSchema(
              this, schemaName);
            schemaMap.put(schemaName, schema);
          }
        } finally {
          JdbcUtils.close(schemaRs);
        }
      } finally {
        releaseConnection(connection);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to get list of namespaces", e);
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
  public DataObjectStoreQueryReader query(final String typePath,
    final BoundingBox boundingBox) {

    final Query query = createBoundingBoxQuery(new Query(typePath), boundingBox);
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(query);
    return reader;
  }

  @Override
  public Reader<DataObject> query(final String typePath, final Geometry geometry) {
    return query(typePath, geometry, null);
  }

  @Override
  public Reader<DataObject> query(final String typePath, Geometry geometry,
    final String whereClause) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    final JdbcAttribute geometryAttribute = (JdbcAttribute)metaData.getGeometryAttribute();
    final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
    geometry = ProjectionFactory.convert(geometry, geometryFactory);

    final StringBuffer where = new StringBuffer();
    if (StringUtils.hasText(whereClause)) {
      where.append(whereClause);
      where.append(" AND ");
    }
    final SqlFunction intersectsFunction = geometryAttribute.getProperty(JdbcConstants.FUNCTION_INTERSECTS);
    final StringBuffer qArg = new StringBuffer();
    geometryAttribute.addSelectStatementPlaceHolder(qArg);
    where.append(intersectsFunction.toSql(geometryAttribute.getName(), qArg));

    final Query query = new Query(metaData);
    query.setWhereClause(where.toString());
    query.addParameter(geometry, geometryAttribute);
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
    this.excludeTablePatterns = excludeTablePatterns;
  }

  public void setFlushBetweenTypes(final boolean flushBetweenTypes) {
    this.flushBetweenTypes = flushBetweenTypes;
  }

  public void setHints(final String hints) {
    this.hints = hints;
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
