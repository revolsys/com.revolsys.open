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

import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.Path;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jts.geom.GeometryFactory;

public class OracleSdoGeometryAttributeAdder extends JdbcAttributeAdder {

  private static final Map<DataType, Integer> DATA_TYPE_TO_2D_ID = new HashMap<DataType, Integer>();

  private static final Map<String, Integer> GEOMETRY_TYPE_TO_ID = new HashMap<String, Integer>();

  private static final Map<Integer, String> ID_TO_GEOMETRY_TYPE = new HashMap<Integer, String>();

  private static final Map<Integer, DataType> ID_TO_DATA_TYPE = new HashMap<Integer, DataType>();

  public static final String ORACLE_SRID = "ORACLE_SRID";

  static {
    addGeometryType(DataTypes.GEOMETRY, "GEOMETRY", 0);
    addGeometryType(DataTypes.POINT, "POINT", 1);
    addGeometryType(DataTypes.LINE_STRING, "LINESTRING", 2);
    addGeometryType(DataTypes.POLYGON, "POLYGON", 3);
    addGeometryType(DataTypes.MULTI_POINT, "MULTIPOINT", 4);
    addGeometryType(DataTypes.MULTI_LINE_STRING, "MULTILINESTRING", 5);
    addGeometryType(DataTypes.MULTI_POLYGON, "MULTIPOLYGON", 6);
    addGeometryType(DataTypes.GEOMETRY_COLLECTION, "GEOMCOLLECTION", 7);
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
    addGeometryType(DataTypes.GEOMETRY_COLLECTION, "GEOMCOLLECTIONZ", 1007);
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
    addGeometryType(DataTypes.GEOMETRY_COLLECTION, "GEOMCOLLECTIONM", 2007);
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
    addGeometryType(DataTypes.GEOMETRY_COLLECTION, "GEOMCOLLECTIONZM", 3007);
    addGeometryType(null, "CURVEZM", 3013);
    addGeometryType(null, "SURFACEZM", 3014);
    addGeometryType(null, "POLYHEDRALSURFACEZM", 3015);
  }

  private static final Logger LOG = LoggerFactory.getLogger(OracleSdoGeometryAttributeAdder.class);

  private static void addGeometryType(final DataType dataType,
    final String name, final Integer id) {
    ID_TO_GEOMETRY_TYPE.put(id, name);
    GEOMETRY_TYPE_TO_ID.put(name, id);
    ID_TO_DATA_TYPE.put(id, dataType);
    if (!DATA_TYPE_TO_2D_ID.containsKey(dataType)) {
      DATA_TYPE_TO_2D_ID.put(dataType, id);
    }
  }

  public static int getGeometryTypeId(final DataType dataType,
    final int axisCount) {
    final int id = DATA_TYPE_TO_2D_ID.get(dataType);
    if (axisCount > 3) {
      return 3000 + id;
    } else if (axisCount > 2) {
      return 1000 + id;
    } else {
      return id;
    }
  }

  private final DataSource dataSource;

  private final AbstractJdbcRecordStore recordStore;

  public OracleSdoGeometryAttributeAdder(
    final AbstractJdbcRecordStore recordStore, final DataSource dataSource) {
    this.recordStore = recordStore;
    this.dataSource = dataSource;
  }

  @Override
  public Attribute addAttribute(final RecordDefinitionImpl recordDefinition,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final String typePath = recordDefinition.getPath();
    final String columnName = name.toUpperCase();
    final RecordStoreSchema schema = recordDefinition.getSchema();

    GeometryFactory geometryFactory = getColumnProperty(schema, typePath,
      columnName, GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = schema.getGeometryFactory();
    }

    DataType dataType = getColumnProperty(schema, typePath, columnName,
      GEOMETRY_TYPE);
    if (dataType == null) {
      dataType = DataTypes.GEOMETRY;
    }

    int axisCount = getIntegerColumnProperty(schema, typePath, columnName,
      NUM_AXIS);
    if (axisCount == -1) {
      axisCount = geometryFactory.getAxisCount();
    }
    int oracleSrid = getIntegerColumnProperty(schema, typePath, columnName,
      ORACLE_SRID);
    if (oracleSrid == -1) {
      oracleSrid = 0;
    }
    final Attribute attribute = new OracleSdoGeometryJdbcAttribute(dbName,
      name, dataType, sqlType, required, description, null, geometryFactory,
      axisCount, oracleSrid);
    recordDefinition.addAttribute(attribute);
    attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
    return attribute;

  }

  protected double getScale(final Datum[] values, final int axisIndex)
    throws SQLException {
    if (axisIndex >= values.length) {
      return 0;
    } else {
      final STRUCT dim = (STRUCT)values[axisIndex];
      final Object[] attributes = dim.getAttributes();
      final double precision = ((Number)attributes[3]).doubleValue();
      if (precision <= 0) {
        return 0;
      } else {
        return 1 / precision;
      }
    }
  }

  @Override
  public void initialize(final RecordStoreSchema schema) {
    try {
      final Connection connection = JdbcUtils.getConnection(dataSource);
      try {
        final String schemaName = recordStore.getDatabaseSchemaName(schema);
        final String sridSql = "select M.TABLE_NAME, M.COLUMN_NAME, M.SRID, M.DIMINFO, C.GEOMETRY_TYPE "
          + "from ALL_SDO_GEOM_METADATA M "
          + "LEFT OUTER JOIN ALL_GEOMETRY_COLUMNS C ON (M.OWNER = C.F_TABLE_SCHEMA AND M.TABLE_NAME = C.F_TABLE_NAME AND M.COLUMN_NAME = C.F_GEOMETRY_COLUMN) "
          + "where OWNER = ?";
        final PreparedStatement statement = connection.prepareStatement(sridSql);
        try {
          statement.setString(1, schemaName);
          final ResultSet resultSet = statement.executeQuery();
          try {
            while (resultSet.next()) {
              final String tableName = resultSet.getString(1);
              final String columnName = resultSet.getString(2);
              final String typePath = Path.toPath(schemaName, tableName);

              int srid = resultSet.getInt(3);
              if (resultSet.wasNull() || srid < 0) {
                srid = 0;
              }
              final ARRAY dimInfo = (ARRAY)resultSet.getObject("DIMINFO");
              int axisCount = dimInfo.length();
              setColumnProperty(schema, typePath, columnName, NUM_AXIS,
                axisCount);
              if (axisCount < 2) {
                axisCount = 2;
              } else if (axisCount > 4) {
                axisCount = 4;
              }
              final Datum[] values = dimInfo.getOracleArray();
              final double scaleXy = getScale(values, 0);
              final double scaleZ = getScale(values, 2);
              final GeometryFactory geometryFactory = ((OracleRecordStore)recordStore).getGeometryFactory(
                srid, axisCount, scaleXy, scaleZ);
              setColumnProperty(schema, typePath, columnName, GEOMETRY_FACTORY,
                geometryFactory);

              setColumnProperty(schema, typePath, columnName, ORACLE_SRID, srid);

              final int geometryType = resultSet.getInt(5);
              DataType geometryDataType;
              if (resultSet.wasNull()) {
                geometryDataType = DataTypes.GEOMETRY;
              } else {
                geometryDataType = ID_TO_DATA_TYPE.get(geometryType);
                if (geometryDataType == null) {
                  geometryDataType = DataTypes.GEOMETRY;
                }
              }
              setColumnProperty(schema, typePath, columnName, GEOMETRY_TYPE,
                geometryDataType);
            }
          } finally {
            JdbcUtils.close(resultSet);
          }
        } finally {
          JdbcUtils.close(statement);
        }
      } finally {
        JdbcUtils.release(connection, dataSource);
      }
    } catch (final SQLException e) {
      LOG.error("Unable to initialize", e);
    }
  }

}
