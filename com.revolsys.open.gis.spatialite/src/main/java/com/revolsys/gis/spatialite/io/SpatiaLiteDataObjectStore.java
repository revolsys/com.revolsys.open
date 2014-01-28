package com.revolsys.gis.spatialite.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.DataStoreIteratorFactory;
import com.revolsys.util.CollectionUtil;

public class SpatiaLiteDataObjectStore extends AbstractJdbcDataObjectStore {
  private static final List<String> ALL_PERMISSIONS = Arrays.asList("SELECT",
    "INSERT", "UPDATE", "DELETE");

  private boolean initialized;

  private boolean useSchemaSequencePrefix = true;

  private final Map<String, Set<String>> schemaTableNames = new TreeMap<String, Set<String>>();

  public SpatiaLiteDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public SpatiaLiteDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
    initSettings();
  }

  public SpatiaLiteDataObjectStore(final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this(dataObjectFactory);
    setDataSource(dataSource);
  }

  public SpatiaLiteDataObjectStore(final DataSource dataSource) {
    super(dataSource);
    initSettings();
  }

  public SpatiaLiteDataObjectStore(
    final SpatiaLiteDatabaseFactory databaseFactory,
    final Map<String, ? extends Object> connectionProperties) {
    super(databaseFactory);
    setConnectionProperties(connectionProperties);
    final DataSource dataSource = databaseFactory.createDataSource(connectionProperties);
    setDataSource(dataSource);
    initSettings();

  }

  protected Query addBoundingBoxFilter(Query query) {
    final BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      final String typePath = query.getTypeName();
      final DataObjectMetaData metaData = getMetaData(typePath);
      if (metaData == null) {
        throw new IllegalArgumentException("Unable to  find table " + typePath);
      } else {
        query = query.clone();
        final Attribute geometryAttribute = metaData.getGeometryAttribute();
        final String geometryColumnName = geometryAttribute.getName();
        final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);

        final BoundingBox projectedBoundingBox = boundingBox.convert(geometryFactory);

        final double k = projectedBoundingBox.getMinX();
        final double y1 = projectedBoundingBox.getMinY();
        final double x2 = projectedBoundingBox.getMaxX();
        final double y2 = projectedBoundingBox.getMaxY();

        // TODO BBOX
        // if (geometryAttribute instanceof OracleSdoGeometryJdbcAttribute) {
        // final String where = " SDO_RELATE("
        // + geometryColumnName
        // + ","
        // +
        // "MDSYS.SDO_GEOMETRY(2003,?,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)),'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'";
        // query.and(new SqlCondition(where, geometryFactory.getSRID(), x1, y1,
        // x2, y2));
        // } else if (geometryAttribute instanceof ArcSdeStGeometryAttribute) {
        // final String where = " SDE.ST_ENVINTERSECTS(" + geometryColumnName
        // + ", ?, ?, ?, ?) = 1";
        // query.and(new SqlCondition(where, x1, y1, x2, y2));
        // } else {
        // throw new IllegalArgumentException("Unknown geometry attribute "
        // + geometryAttribute);
        // }
      }
    }
    return query;

  }

  @Override
  protected Set<String> getDatabaseSchemaNames() {
    schemaTableNames.put("", new TreeSet<String>());
    try {
      final Connection connection = getDbConnection();
      try {
        final String sql = "select name from sqlite_master where type='table'";
        final PreparedStatement statement = connection.prepareStatement(sql);
        final ResultSet resultSet = statement.executeQuery();

        try {
          while (resultSet.next()) {
            final String name = resultSet.getString("NAME");
            if (!name.startsWith("idx_")) {
              final String schemaName;
              final String tableName;

              final int dotIndex = name.indexOf('.');
              if (dotIndex == -1) {
                schemaName = "";
                tableName = name;
              } else {
                schemaName = name.substring(0, dotIndex);
                tableName = name.substring(dotIndex + 1);
              }
              addAllSchemaNames(schemaName);
              if (!isSchemaExcluded(schemaName)) {
                CollectionUtil.addToSet(schemaTableNames, schemaName, tableName);
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
        "Unable to get schema and table names", e);
    }
    return schemaTableNames.keySet();
  }

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    return sequenceName + ".NEXTVAL";
  }

  @Override
  public Object getNextPrimaryKey(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    return getNextPrimaryKey(sequenceName);
  }

  @Override
  public Object getNextPrimaryKey(final String sequenceName) {
    final String sql = "SELECT " + sequenceName + ".NEXTVAL FROM SYS.DUAL";
    try {
      return JdbcUtils.selectLong(getDataSource(), getConnection(), sql);
    } catch (final SQLException e) {
      throw new IllegalArgumentException(
        "Cannot create ID for " + sequenceName, e);
    }
  }

  @Override
  public int getRowCount(final Query query) {
    final Query bboxQuery = addBoundingBoxFilter(query);
    if (bboxQuery != query) {
      query.setAttributeNames("count(*))");
    }
    return super.getRowCount(query);
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    if (metaData == null) {
      return null;
    } else {
      final String typePath = metaData.getPath();
      final String schema = getDatabaseSchemaName(PathUtil.getPath(typePath));
      final String shortName = ShortNameProperty.getShortName(metaData);
      final String sequenceName;
      if (StringUtils.hasText(shortName)) {
        if (this.useSchemaSequencePrefix) {
          sequenceName = schema + "." + shortName.toLowerCase() + "_SEQ";
        } else {
          sequenceName = shortName.toLowerCase() + "_SEQ";
        }
      } else {
        final String tableName = getDatabaseTableName(typePath);
        if (this.useSchemaSequencePrefix) {
          sequenceName = schema + "." + tableName + "_SEQ";
        } else {
          sequenceName = tableName + "_SEQ";
        }
      }
      return sequenceName;
    }
  }

  @Override
  @PostConstruct
  public void initialize() {
    super.initialize();
    if (!this.initialized) {
      this.initialized = true;
      final JdbcAttributeAdder attributeAdder = new JdbcAttributeAdder();
      addAttributeAdder("NUMBER", attributeAdder);

      addAttributeAdder("CHAR", attributeAdder);
      addAttributeAdder("NCHAR", attributeAdder);
      addAttributeAdder("VARCHAR", attributeAdder);
      addAttributeAdder("VARCHAR2", attributeAdder);
      addAttributeAdder("NVARCHAR2", new JdbcAttributeAdder(DataTypes.STRING));
      addAttributeAdder("LONG", attributeAdder);
      addAttributeAdder("CLOB", attributeAdder);
      addAttributeAdder("NCLOB", attributeAdder);

      addAttributeAdder("DATE", attributeAdder);
      addAttributeAdder("TIMESTAMP", attributeAdder);

      setPrimaryKeySql("SELECT distinct cols.table_name, cols.column_name FROM all_constraints cons, all_cons_columns cols WHERE cons.constraint_type = 'P' AND cons.constraint_name = cols.constraint_name AND cons.owner = cols.owner AND cons.owner =?");
    }
  }

  private void initSettings() {
    setExcludeTablePaths("/GEOMETRY_COLUMNS", "/GEOMETRY_COLUMNS_AUTH",
      "/GEOMETRY_COLUMNS_FIELD_INFOS", "/GEOMETRY_COLUMNS_STATISTICS",
      "/GEOMETRY_COLUMNS_TIME", "/SPATIALITE_HISTORY", "/SPATIALINDEX",
      "/SPATIAL_REF_SYS", "/SQL_STATEMENTS_LOG", "/SQLITE_SEQUENCE",
      "/VIEWS_GEOMETRY_COLUMNS", "/VIEWS_GEOMETRY_COLUMNS_AUTH",
      "/VIEWS_GEOMETRY_COLUMNS_FIELD_INFOS",
      "/VIEWS_GEOMETRY_COLUMNS_STATISTICS", "/VIRTS_GEOMETRY_COLUMNS",
      "/VIRTS_GEOMETRY_COLUMNS_AUTH", "/VIRTS_GEOMETRY_COLUMNS_FIELD_INFOS",
      "/VIRTS_GEOMETRY_COLUMNS_STATISTICS");
    setExcludeTablePatterns("^/SPATIALITE_.*");
    setExcludeTablePatterns("^/SQLITE_.*");
    setExcludeTablePatterns("^/IDX_.*");
    setIteratorFactory(new DataStoreIteratorFactory(
      SpatiaLiteDataObjectStore.class, "createOracleIterator"));
  }

  @Override
  public boolean isSchemaExcluded(final String schemaName) {
    return false;
  }

  public boolean isUseSchemaSequencePrefix() {
    return this.useSchemaSequencePrefix;
  }

  @Override
  protected synchronized Map<String, List<String>> loadIdColumnNames(
    final String dbSchemaName) {
    final String schemaName = "/" + dbSchemaName.toUpperCase();

    final Map<String, List<String>> idColumnNames = new HashMap<>();
    final Connection connection = getDbConnection();
    try {
      for (final String dbTableName : schemaTableNames.get(dbSchemaName)) {
        if (!isExcluded(dbSchemaName, dbTableName)) {
          final String tableName = schemaName + "/"
            + dbTableName.toUpperCase().replaceAll("/+", "/");
          System.out.println(tableName);
          final PreparedStatement statement = connection.prepareStatement("PRAGMA table_info('"
            + dbTableName + "')");
          try {
            final ResultSet rs = statement.executeQuery();
            try {
              while (rs.next()) {
                final String idAttributeName = rs.getString("name");
                final int pk = rs.getInt("PK");
                if (pk == 1) {
                  CollectionUtil.addToList(idColumnNames, tableName,
                    idAttributeName);
                }
              }
            } finally {
              JdbcUtils.close(rs);
            }
          } finally {
            JdbcUtils.close(statement);
          }
        }
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
  protected void loadSchemaTablePermissions(final String schemaName,
    final Map<String, List<String>> tablePermissionsMap,
    final Map<String, String> tableDescriptionMap) {
    final Set<String> tableNames = schemaTableNames.get(schemaName);
    if (tableNames != null) {
      for (final String dbTableName : tableNames) {
        if (!isExcluded(schemaName, dbTableName)) {
          tablePermissionsMap.put(dbTableName, ALL_PERMISSIONS);
        }
      }
    }
  }

  @Override
  public ResultPager<DataObject> page(final Query query) {
    return new SpatiaLiteJdbcQueryResultPager(this, getProperties(), query);
  }

  public void setUseSchemaSequencePrefix(final boolean useSchemaSequencePrefix) {
    this.useSchemaSequencePrefix = useSchemaSequencePrefix;
  }

}
