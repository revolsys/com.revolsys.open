package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import oracle.sql.ARRAY;
import oracle.sql.Datum;
import oracle.sql.STRUCT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.JdbcConstants;
import com.revolsys.gis.jdbc.io.SqlFunction;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.jdbc.JdbcUtils;

public class OracleSdoGeometryAttributeAdder extends JdbcAttributeAdder {
  private final DataSource dataSource;

  private static final Map<Integer, String> ID_TO_GEOMETRY_TYPE = new HashMap<Integer, String>();

  private static final Map<String, Integer> GEOMETRY_TYPE_TO_ID = new HashMap<String, Integer>();

  static {
    addGeometryType("GEOMETRY", 0);
    addGeometryType("POINT", 1);
    addGeometryType("LINESTRING", 2);
    addGeometryType("POLYGON", 3);
    addGeometryType("MULTIPOINT", 4);
    addGeometryType("MULTILINESTRING", 5);
    addGeometryType("MULTIPOLYGON", 6);
    addGeometryType("GEOMCOLLECTION", 7);
    addGeometryType("CURVE", 13);
    addGeometryType("SURFACE", 14);
    addGeometryType("POLYHEDRALSURFACE", 15);
    addGeometryType("GEOMETRYZ", 1000);
    addGeometryType("POINTZ", 1001);
    addGeometryType("LINESTRINGZ", 1002);
    addGeometryType("POLYGONZ", 1003);
    addGeometryType("MULTIPOINTZ", 1004);
    addGeometryType("MULTILINESTRINGZ", 1005);
    addGeometryType("MULTIPOLYGONZ", 1006);
    addGeometryType("GEOMCOLLECTIONZ", 1007);
    addGeometryType("CURVEZ", 1013);
    addGeometryType("SURFACEZ", 1014);
    addGeometryType("POLYHEDRALSURFACEZ", 1015);
    addGeometryType("GEOMETRY", 2000);
    addGeometryType("POINTM", 2001);
    addGeometryType("LINESTRINGM", 2002);
    addGeometryType("POLYGONM", 2003);
    addGeometryType("MULTIPOINTM", 2004);
    addGeometryType("MULTILINESTRINGM", 2005);
    addGeometryType("MULTIPOLYGONM", 2006);
    addGeometryType("GEOMCOLLECTIONM", 2007);
    addGeometryType("CURVEM", 2013);
    addGeometryType("SURFACEM", 2014);
    addGeometryType("POLYHEDRALSURFACEM", 2015);
    addGeometryType("GEOMETRYZM", 3000);
    addGeometryType("POINTZM", 3001);
    addGeometryType("LINESTRINGZM", 3002);
    addGeometryType("POLYGONZM", 3003);
    addGeometryType("MULTIPOINTZM", 3004);
    addGeometryType("MULTILINESTRINGZM", 3005);
    addGeometryType("MULTIPOLYGONZM", 3006);
    addGeometryType("GEOMCOLLECTIONZM", 3007);
    addGeometryType("CURVEZM", 3013);
    addGeometryType("SURFACEZM", 3014);
    addGeometryType("POLYHEDRALSURFACEZM", 3015);
  }

  private final Logger LOG = LoggerFactory.getLogger(OracleSdoGeometryAttributeAdder.class);

  public OracleSdoGeometryAttributeAdder(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  private static void addGeometryType(String name, int id) {
    ID_TO_GEOMETRY_TYPE.put(id, name);
    GEOMETRY_TYPE_TO_ID.put(name, id);
  }

  @Override
  public Attribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String name, final int sqlType, final int length, final int scale,
    final boolean required) {
    final QName typeName = metaData.getName();
    final String columnName = name.toUpperCase();
    final DataObjectStoreSchema schema = metaData.getSchema();
    int srid = getIntegerColumnProperty(schema, typeName, columnName,
      OracleSdoGeometryJdbcAttribute.SRID_PROPERTY);
    if (srid == -1) {
      srid = 0;
    }
    int dimension = getIntegerColumnProperty(schema, typeName, columnName,
      OracleSdoGeometryJdbcAttribute.COORDINATE_DIMENSION_PROPERTY);
    if (dimension == -1) {
      dimension = 2;
    }
    final double precision = getDoubleColumnProperty(schema, typeName,
      columnName, OracleSdoGeometryJdbcAttribute.COORDINATE_PRECISION_PROPERTY);

    final double geometryScale = 1 / precision;
    final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
    final CoordinatesPrecisionModel precisionModel = new SimpleCoordinatesPrecisionModel(
      geometryScale);
    final GeometryFactory geometryFactory;
    if (coordinateSystem == null) {
      geometryFactory = new GeometryFactory(precisionModel, dimension);
    } else {
      geometryFactory = new GeometryFactory(coordinateSystem, precisionModel,
        dimension);
    }
    final Attribute attribute = new OracleSdoGeometryJdbcAttribute(name,
      DataTypes.GEOMETRY, sqlType, length, scale, required, null,
      geometryFactory, dimension);
    metaData.addAttribute(attribute);
    attribute.setProperty(JdbcConstants.FUNCTION_INTERSECTS, new SqlFunction(
      "SDO_RELATE(", ",'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'"));
    attribute.setProperty(JdbcConstants.FUNCTION_BUFFER, new SqlFunction(
      "SDO_GEOM.SDO_BUFFER(", "," + precision + ")"));
    attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
    return attribute;

  }

  private Object getColumnProperty(final DataObjectStoreSchema schema,
    final QName typeName, final String columnName, final String propertyName) {
    final Map<QName, Map<String, Map<String, Object>>> columnProperties = schema.getProperty(OracleSdoGeometryJdbcAttribute.SCHEMA_PROPERTY);
    final Map<String, Map<String, Object>> columnsProperties = columnProperties.get(typeName);
    if (columnsProperties != null) {
      final Map<String, Object> properties = columnsProperties.get(columnName);
      if (properties != null) {
        final Object value = properties.get(propertyName);
        return value;
      }
    }
    return null;
  }

  private double getDoubleColumnProperty(final DataObjectStoreSchema schema,
    final QName typeName, final String columnName, final String propertyName) {
    final Object value = getColumnProperty(schema, typeName, columnName,
      propertyName);
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      return 11;
    }
  }

  private int getIntegerColumnProperty(final DataObjectStoreSchema schema,
    final QName typeName, final String columnName, final String propertyName) {
    final Object value = getColumnProperty(schema, typeName, columnName,
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
    Map<QName, Map<String, Map<String, Object>>> columnProperties = schema.getProperty(OracleSdoGeometryJdbcAttribute.SCHEMA_PROPERTY);
    if (columnProperties == null) {
      columnProperties = new HashMap<QName, Map<String, Map<String, Object>>>();
      schema.setProperty(OracleSdoGeometryJdbcAttribute.SCHEMA_PROPERTY,
        columnProperties);

      try {
        final Connection connection = JdbcUtils.getConnection(dataSource);
        try {
          final String schemaName = schema.getName().toUpperCase();
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
    final Map<QName, Map<String, Map<String, Object>>> esriColumnProperties,
    final String schemaName, final String tableName, final String columnName,
    final String propertyName, final Object propertyValue) {
    final QName typeName = new QName(schemaName, tableName);
    Map<String, Map<String, Object>> typeColumnMap = esriColumnProperties.get(typeName);
    if (typeColumnMap == null) {
      typeColumnMap = new HashMap<String, Map<String, Object>>();
      esriColumnProperties.put(typeName, typeColumnMap);
    }
    Map<String, Object> columnProperties = typeColumnMap.get(columnName);
    if (columnProperties == null) {
      columnProperties = new HashMap<String, Object>();
      typeColumnMap.put(columnName, columnProperties);
    }
    columnProperties.put(propertyName, propertyValue);
  }
}
