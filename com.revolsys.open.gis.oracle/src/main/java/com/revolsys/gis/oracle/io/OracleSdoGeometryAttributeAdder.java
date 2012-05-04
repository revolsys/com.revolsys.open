package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.JdbcConstants;
import com.revolsys.jdbc.io.SqlFunction;

public class OracleSdoGeometryAttributeAdder extends JdbcAttributeAdder {
  private final DataSource dataSource;

  private static final Map<Integer, String> ID_TO_GEOMETRY_TYPE = new HashMap<Integer, String>();

  private static final Map<Integer, DataType> ID_TO_DATA_TYPE = new HashMap<Integer, DataType>();

  private static final Map<DataType, Integer> DATA_TYPE_TO_2D_ID = new HashMap<DataType, Integer>();

  private static final Map<String, Integer> GEOMETRY_TYPE_TO_ID = new HashMap<String, Integer>();

  static {
    addGeometryType(DataTypes.GEOMETRY, "GEOMETRY", 0);
    addGeometryType(DataTypes.POINT, "POINT", 1);
    addGeometryType(DataTypes.LINE_STRING, "LINESTRING", 2);
    addGeometryType(DataTypes.POLYGON, "POLYGON", 3);
    addGeometryType(DataTypes.MULTI_POINT, "MULTIPOINT", 4);
    addGeometryType(DataTypes.MULTI_LINE_STRING, "MULTILINESTRING", 5);
    addGeometryType(DataTypes.MULTI_POLYGON, "MULTIPOLYGON", 6);
    addGeometryType(null, "GEOMCOLLECTION", 7);
    addGeometryType(null, "CURVE", 13);
    addGeometryType(null, "SURFACE", 14);
    addGeometryType(null, "POLYHEDRALSURFACE", 15);
    addGeometryType(DataTypes.GEOMETRY, "GEOMETRYZ", 1000);
    addGeometryType(DataTypes.POINT, "POINTZ", 1001);
    addGeometryType(DataTypes.LINE_STRING, "LINESTRINGZ", 1002);
    addGeometryType(DataTypes.POLYGON, "POLYGONZ", 1003);
    addGeometryType(DataTypes.MULTI_POINT, "MULTIPOINTZ", 1004);
    addGeometryType(DataTypes.MULTI_LINE_STRING, "MULTILINESTRINGZ", 1005);
    addGeometryType(DataTypes.MULTI_POLYGON, "MULTIPOLYGONZ", 1006);
    addGeometryType(null, "GEOMCOLLECTIONZ", 1007);
    addGeometryType(null, "CURVEZ", 1013);
    addGeometryType(null, "SURFACEZ", 1014);
    addGeometryType(null, "POLYHEDRALSURFACEZ", 1015);
    addGeometryType(DataTypes.GEOMETRY, "GEOMETRYM", 2000);
    addGeometryType(DataTypes.POINT, "POINTM", 2001);
    addGeometryType(DataTypes.LINE_STRING, "LINESTRINGM", 2002);
    addGeometryType(DataTypes.POLYGON, "POLYGONM", 2003);
    addGeometryType(DataTypes.MULTI_POINT, "MULTIPOINTM", 2004);
    addGeometryType(DataTypes.MULTI_LINE_STRING, "MULTILINESTRINGM", 2005);
    addGeometryType(DataTypes.MULTI_POLYGON, "MULTIPOLYGONM", 2006);
    addGeometryType(null, "GEOMCOLLECTIONM", 2007);
    addGeometryType(null, "CURVEM", 2013);
    addGeometryType(null, "SURFACEM", 2014);
    addGeometryType(null, "POLYHEDRALSURFACEM", 2015);
    addGeometryType(DataTypes.GEOMETRY, "GEOMETRYZM", 3000);
    addGeometryType(DataTypes.POINT, "POINTZM", 3001);
    addGeometryType(DataTypes.LINE_STRING, "LINESTRINGZM", 3002);
    addGeometryType(DataTypes.POLYGON, "POLYGONZM", 3003);
    addGeometryType(DataTypes.MULTI_POINT, "MULTIPOINTZM", 3004);
    addGeometryType(DataTypes.MULTI_LINE_STRING, "MULTILINESTRINGZM", 3005);
    addGeometryType(DataTypes.MULTI_POLYGON, "MULTIPOLYGONZM", 3006);
    addGeometryType(null, "GEOMCOLLECTIONZM", 3007);
    addGeometryType(null, "CURVEZM", 3013);
    addGeometryType(null, "SURFACEZM", 3014);
    addGeometryType(null, "POLYHEDRALSURFACEZM", 3015);
  }

  private final Logger LOG = LoggerFactory.getLogger(OracleSdoGeometryAttributeAdder.class);

  private OracleDataObjectStore dataStore;

  public OracleSdoGeometryAttributeAdder(OracleDataObjectStore dataStore,
    final DataSource dataSource) {
    this.dataStore = dataStore;
    this.dataSource = dataSource;
  }

  private static void addGeometryType(DataType dataType, String name, Integer id) {
    ID_TO_GEOMETRY_TYPE.put(id, name);
    GEOMETRY_TYPE_TO_ID.put(name, id);
    ID_TO_DATA_TYPE.put(id, dataType);
    if (!DATA_TYPE_TO_2D_ID.containsKey(dataType)) {
      DATA_TYPE_TO_2D_ID.put(dataType, id);
    }
  }

  public static int getGeometryTypeId(DataType dataType, int numAxis) {
    int id = DATA_TYPE_TO_2D_ID.get(dataType);
    if (numAxis > 3) {
      return 3000 + id;
    } else if (numAxis > 2) {
      return 1000 + id;
    } else {
      return id;
    }
  }

  @Override
  public Attribute addAttribute(
    final DataObjectMetaDataImpl metaData,
    final String name,
    final int sqlType,
    final int length,
    final int scale,
    final boolean required) {
    final String typePath = metaData.getPath();
    final String columnName = name.toUpperCase();
    final DataObjectStoreSchema schema = metaData.getSchema();
    int srid = getIntegerColumnProperty(schema, typePath, columnName,
      OracleSdoGeometryJdbcAttribute.SRID_PROPERTY);
    if (srid == -1) {
      srid = 0;
    }
    int dimension = getIntegerColumnProperty(schema, typePath, columnName,
      OracleSdoGeometryJdbcAttribute.COORDINATE_DIMENSION_PROPERTY);
    if (dimension == -1) {
      dimension = 2;
    }
    final double precision = getDoubleColumnProperty(schema, typePath,
      columnName, OracleSdoGeometryJdbcAttribute.COORDINATE_PRECISION_PROPERTY);

    final double geometryScale = 1 / precision;
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(srid,
      dimension, geometryScale, 0);
    DataType dataType = DataTypes.GEOMETRY;
    final String schemaName = JdbcUtils.getSchemaName(typePath).toUpperCase();
    final String tableName = JdbcUtils.getTableName(typePath).toUpperCase();
    String sql = "SELECT GEOMETRY_TYPE FROM ALL_GEOMETRY_COLUMNS WHERE F_TABLE_SCHEMA = ? AND F_TABLE_NAME = ? AND F_GEOMETRY_COLUMN = ?";
    try {
      int geometryType = JdbcUtils.selectInt(dataSource, sql, schemaName,
        tableName, columnName);
      dataType = ID_TO_DATA_TYPE.get(geometryType);
    } catch (IllegalArgumentException e) {
      LOG.error("No ALL_GEOMETRY_COLUMNS metadata for " + typePath + "."
        + columnName);
    } catch (RuntimeException e) {
      LOG.error("Unable to get geometry type for " + typePath + "."
        + columnName, e);
    }
    final Attribute attribute = new OracleSdoGeometryJdbcAttribute(name,
      dataType, sqlType, length, scale, required, null, geometryFactory,
      dimension);
    metaData.addAttribute(attribute);
    attribute.setProperty(JdbcConstants.FUNCTION_INTERSECTS, new SqlFunction(
      "SDO_RELATE(", ",'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'"));
    attribute.setProperty(JdbcConstants.FUNCTION_BUFFER, new SqlFunction(
      "SDO_GEOM.SDO_BUFFER(", "," + precision + ")"));
    attribute.setProperty(JdbcConstants.FUNCTION_EQUAL, new SqlFunction(
      "SDO_EQUAL(", ") = 'TRUE'"));
    attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
    return attribute;

  }

  private Object getColumnProperty(
    final DataObjectStoreSchema schema,
    final String typePath,
    final String columnName,
    final String propertyName) {
    final Map<String, Map<String, Map<String, Object>>> columnProperties = schema.getProperty(OracleSdoGeometryJdbcAttribute.SCHEMA_PROPERTY);
    final Map<String, Map<String, Object>> columnsProperties = columnProperties.get(typePath);
    if (columnsProperties != null) {
      final Map<String, Object> properties = columnsProperties.get(columnName);
      if (properties != null) {
        final Object value = properties.get(propertyName);
        return value;
      }
    }
    return null;
  }

  private double getDoubleColumnProperty(
    final DataObjectStoreSchema schema,
    final String typePath,
    final String columnName,
    final String propertyName) {
    final Object value = getColumnProperty(schema, typePath, columnName,
      propertyName);
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      return 11;
    }
  }

  private int getIntegerColumnProperty(
    final DataObjectStoreSchema schema,
    final String typePath,
    final String columnName,
    final String propertyName) {
    final Object value = getColumnProperty(schema, typePath, columnName,
      propertyName);
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      return -1;
    }
  }

  @Override
  public void initialize(final DataObjectStoreSchema schema) {
    Map<String, Map<String, Map<String, Object>>> columnProperties = schema.getProperty(OracleSdoGeometryJdbcAttribute.SCHEMA_PROPERTY);
    if (columnProperties == null) {
      columnProperties = new HashMap<String, Map<String, Map<String, Object>>>();
      schema.setProperty(OracleSdoGeometryJdbcAttribute.SCHEMA_PROPERTY,
        columnProperties);

      try {
        final Connection connection = JdbcUtils.getConnection(dataSource);
        try {
          final String schemaName = dataStore.getDatabaseSchemaName(schema);
          final String sridSql = "select TABLE_NAME, COLUMN_NAME, SRID, DIMINFO from ALL_SDO_GEOM_METADATA where OWNER = ?";
          final PreparedStatement statement = connection.prepareStatement(sridSql);
          try {
            statement.setString(1, schemaName);
            final ResultSet resultSet = statement.executeQuery();
            try {
              while (resultSet.next()) {
                final String tableName = resultSet.getString(1);
                final String columnName = resultSet.getString(2);
                final int srid = resultSet.getInt(3);
                final ARRAY dimInfo = (ARRAY)resultSet.getObject("DIMINFO");
                final int dimension = dimInfo.length();
                final Datum[] values = dimInfo.getOracleArray();
                final STRUCT xDim = (STRUCT)values[0];
                final Object[] attributes = xDim.getAttributes();
                final double precision = ((Number)attributes[3]).doubleValue();

                setColumnProperty(columnProperties, schemaName, tableName,
                  columnName, OracleSdoGeometryJdbcAttribute.SRID_PROPERTY,
                  srid);
                setColumnProperty(columnProperties, schemaName, tableName,
                  columnName,
                  OracleSdoGeometryJdbcAttribute.COORDINATE_DIMENSION_PROPERTY,
                  dimension);
                setColumnProperty(columnProperties, schemaName, tableName,
                  columnName,
                  OracleSdoGeometryJdbcAttribute.COORDINATE_PRECISION_PROPERTY,
                  precision);
              }
            } finally {
              JdbcUtils.close(resultSet);
            }
          } finally {
            JdbcUtils.close(statement);
          }
        } finally {
          JdbcUtils.close(connection);
        }
      } catch (final SQLException e) {
        LOG.error("Unable to initialize", e);
      }
    }
  }

  private void setColumnProperty(
    final Map<String, Map<String, Map<String, Object>>> esriColumnProperties,
    final String schemaName,
    final String tableName,
    final String columnName,
    final String propertyName,
    final Object propertyValue) {
    final String typePath = PathUtil.getPath(schemaName, tableName);
    Map<String, Map<String, Object>> typeColumnMap = esriColumnProperties.get(typePath);
    if (typeColumnMap == null) {
      typeColumnMap = new HashMap<String, Map<String, Object>>();
      esriColumnProperties.put(typePath, typeColumnMap);
    }
    Map<String, Object> columnProperties = typeColumnMap.get(columnName);
    if (columnProperties == null) {
      columnProperties = new HashMap<String, Object>();
      typeColumnMap.put(columnName, columnProperties);
    }
    columnProperties.put(propertyName, propertyValue);
  }
}
