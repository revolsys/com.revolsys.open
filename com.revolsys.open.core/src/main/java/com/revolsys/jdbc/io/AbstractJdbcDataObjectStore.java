package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.DataObjectStoreQueryReader;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.io.Query;
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
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.io.Reader;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractJdbcDataObjectStore extends
  AbstractDataObjectStore implements JdbcDataObjectStore {
  private final Map<String, JdbcAttributeAdder> attributeAdders = new HashMap<String, JdbcAttributeAdder>();

  private int batchSize;

  private Connection connection;

  private DataSource dataSource;

  private String[] excludeTablePatterns = new String[0];

  private boolean flushBetweenTypes;

  private Statistics deleteStatistics;

  private Statistics insertStatistics;

  private Statistics updateStatistics;

  private String hints;

  private final Map<QName, String> sequenceTypeSqlMap = new HashMap<QName, String>();

  private String sqlPrefix;

  private String sqlSuffix;

  private final List<String> tableTypes = Arrays.asList("VIEW", "TABLE");

  private final Map<String, String> schemaNameMap = new HashMap<String, String>();

  private final Map<QName, String> tableNameMap = new HashMap<QName, String>();

  private CoordinatesPrecisionModel precisionModel;

  public AbstractJdbcDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public AbstractJdbcDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  protected void addAttribute(final DataObjectMetaDataImpl metaData,
    final String name, final String dataType, final int sqlType,
    final int length, final int scale, final boolean required) {
    JdbcAttributeAdder attributeAdder = attributeAdders.get(dataType);
    if (attributeAdder == null) {
      attributeAdder = new JdbcAttributeAdder(DataTypes.OBJECT);
    }
    attributeAdder.addAttribute(metaData, name, sqlType, length, scale,
      required);
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
    super.close();
    final JdbcWriter writer = getSharedAttribute("writer");
    if (writer != null) {
      setSharedAttribute("writer", null);
      writer.close();
    }
    if (insertStatistics != null) {
      insertStatistics.disconnect();
      insertStatistics = null;
    }
    if (updateStatistics != null) {
      updateStatistics.disconnect();
      updateStatistics = null;
    }
    if (deleteStatistics != null) {
      deleteStatistics.disconnect();
      deleteStatistics = null;
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

  @Override
  public Object createPrimaryIdValue(final QName typeName) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    final GlobalIdProperty globalIdProperty = GlobalIdProperty.getProperty(metaData);
    if (globalIdProperty != null) {
      return UUID.randomUUID().toString();
    } else {
      return getNextPrimaryKey(metaData);
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

  @Override
  public DataObjectReader createReader(final QName typeName,
    final String query, final List<Object> parameters) {
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(typeName, query, parameters);
    return reader;
  }

  protected DataObjectReader createReader(final QName typeName,
    final String whereClause, final Object... parameters) {
    return createReader(typeName, whereClause, Arrays.asList(parameters));
  }

  protected DataObjectStoreQueryReader createReader(final Query query) {
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(query);
    return reader;
  }

  public JdbcWriter createWriter() {
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

  @Override
  public void delete(final DataObject object) {
    if (object.getState() == DataObjectState.Persisted
      || object.getState() == DataObjectState.Modified) {
      object.setState(DataObjectState.Deleted);
      getWriter().write(object);
    }
  }

  @Override
  public void deleteAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      delete(object);
    }
  }

  public JdbcAttribute getAttribute(final String schemaName,
    final String tableName, final String columnName) {
    final QName typeName = new QName(schemaName, tableName);
    final DataObjectMetaData metaData = getMetaData(typeName);
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

  public List<String> getColumnNames(final QName typeName) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    return metaData.getAttributeNames();
  }

  public Connection getConnection() {
    return connection;
  }

  public String getDatabaseSchemaName(final String schemaName) {
    return schemaNameMap.get(schemaName);
  }

  public String getDatabaseTableName(final QName typeName) {
    return tableNameMap.get(typeName);
  }

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

  public Statistics getDeleteStatistics() {
    if (deleteStatistics == null) {
      if (getLabel() == null) {
        deleteStatistics = new Statistics("Delete");
      } else {
        deleteStatistics = new Statistics(getLabel() + " Delete");
      }
      deleteStatistics.connect();
    }
    return deleteStatistics;
  }

  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    throw new UnsupportedOperationException(
      "Cannot create SQL to generate Primary Key for " + metaData);
  }

  public String getHints() {
    return hints;
  }

  public Statistics getInsertStatistics() {
    if (insertStatistics == null) {
      if (getLabel() == null) {
        insertStatistics = new Statistics("Insert");
      } else {
        insertStatistics = new Statistics(getLabel() + " Insert");
      }
      insertStatistics.connect();
    }
    return insertStatistics;
  }

  public DataObjectMetaData getMetaData(final QName typeName,
    final ResultSetMetaData resultSetMetaData) {
    try {
      final String schemaName = typeName.getNamespaceURI();
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(this,
        getSchema(schemaName), typeName);

      final String tableName = typeName.getLocalPart();
      final String idColumnName = loadIdColumnName(schemaName, tableName);
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
        + typeName);
    }
  }

  public CoordinatesPrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  protected String getSequenceInsertSql(final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    final String tableName = JdbcUtils.getTableName(typeName);
    String sql = sequenceTypeSqlMap.get(typeName);
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
      sequenceTypeSqlMap.put(typeName, sql);
    }
    return sql;
  }

  public String getSqlPrefix() {
    return sqlPrefix;
  }

  public String getSqlSuffix() {
    return sqlSuffix;
  }

  public Statistics getUpdateStatistics() {
    if (updateStatistics == null) {
      if (getLabel() == null) {
        updateStatistics = new Statistics("Update");
      } else {
        updateStatistics = new Statistics(getLabel() + " Update");
      }
      updateStatistics.connect();
    }
    return updateStatistics;
  }

  public synchronized JdbcWriter getWriter() {
    JdbcWriter writer = getSharedAttribute("writer");
    if (writer == null) {
      writer = createWriter();
      setSharedAttribute("writer", writer);
    }
    return writer;
  }

  @PostConstruct
  public void initialize() {
  }

  @Override
  public void insert(final DataObject object) {
    try {
      final JdbcWriter writer = createWriter();
      writer.write(object);
      writer.close();
    } catch (final RuntimeException e) {
      LoggerFactory.getLogger(getClass()).error("Unable to insert " + object);
      throw e;
    }
  }

  @Override
  public void insertAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      insert(object);
    }
  }

  @Override
  public boolean isEditable(final QName typeName) {
    final DataObjectMetaData metaData = getMetaData(typeName);
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
    final Map<QName, DataObjectMetaData> metaDataMap) {
    final String schemaName = schema.getName();
    final String dbSchemaName = getDatabaseSchemaName(schemaName);
    for (final JdbcAttributeAdder attributeAdder : attributeAdders.values()) {
      attributeAdder.initialize(schema);
    }
    final Connection connection = getDbConnection();
    try {
      final DatabaseMetaData databaseMetaData = connection.getMetaData();

      final ResultSet tablesRs = databaseMetaData.getTables(null, dbSchemaName,
        "%", null);
      final Map<QName, String> idColumnNames = new HashMap<QName, String>();
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
            final QName typeName = new QName(schemaName, tableName);
            tableNameMap.put(typeName, dbTableName);
            final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
              this, schema, typeName);
            metaDataMap.put(typeName, metaData);
            final String idColumnName = loadIdColumnName(dbSchemaName,
              dbTableName);
            idColumnNames.put(typeName, idColumnName);

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
          final QName typeName = new QName(schemaName, tableName);
          final DataObjectMetaDataImpl metaData = (DataObjectMetaDataImpl)metaDataMap.get(typeName);
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
            if (name.equalsIgnoreCase(idColumnNames.get(typeName))) {
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
            final String schemaName = dbSchemaName.toUpperCase();
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

  protected void postCreateDataObjectMetaData(
    final DataObjectMetaDataImpl metaData) {
  }

  @Override
  public Reader<DataObject> query(final QName typeName) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Unknown type " + typeName);
    } else {
      final Query query = new Query(metaData);
      final DataObjectStoreQueryReader reader = createReader(query);
      return reader;
    }
  }

  public DataObjectStoreQueryReader query(final QName typeName,
    final BoundingBox boundingBox) {

    final Query query = createBoundingBoxQuery(new Query(typeName), boundingBox);
    final DataObjectStoreQueryReader reader = createReader();
    reader.addQuery(query);
    return reader;
  }

  public Reader<DataObject> query(final QName typeName, final Geometry geometry) {
    return query(typeName, geometry, null);
  }

  public Reader<DataObject> query(final QName typeName, Geometry geometry,
    final String whereClause) {
    final DataObjectMetaData metaData = getMetaData(typeName);
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
    if (dataSource != null) {
      JdbcUtils.close(connection);
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

  public void setPrecisionModel(final CoordinatesPrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  public void setSqlPrefix(final String sqlPrefix) {
    this.sqlPrefix = sqlPrefix;
  }

  public void setSqlSuffix(final String sqlSuffix) {
    this.sqlSuffix = sqlSuffix;
  }

  @Override
  public void update(final DataObject object) {
    getWriter().write(object);
  }
}
