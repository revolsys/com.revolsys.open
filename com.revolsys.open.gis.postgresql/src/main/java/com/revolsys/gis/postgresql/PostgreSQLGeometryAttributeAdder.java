package com.revolsys.gis.postgresql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

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
import com.revolsys.jts.geom.GeometryFactory;

public class PostgreSQLGeometryAttributeAdder extends JdbcAttributeAdder {

  private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLGeometryAttributeAdder.class);

  private static final Map<String, DataType> DATA_TYPE_MAP = new HashMap<String, DataType>();

  static {
    DATA_TYPE_MAP.put("GEOMETRY", DataTypes.GEOMETRY);
    DATA_TYPE_MAP.put("POINT", DataTypes.POINT);
    DATA_TYPE_MAP.put("LINESTRING", DataTypes.LINE_STRING);
    DATA_TYPE_MAP.put("POLYGON", DataTypes.POLYGON);
    DATA_TYPE_MAP.put("MULTIPOINT", DataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put("MULTILINESTRING", DataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("MULTIPOLYGON", DataTypes.MULTI_POLYGON);
  }

  private final DataSource dataSource;

  private final PostgreSQLDataObjectStore dataStore;

  public PostgreSQLGeometryAttributeAdder(
    final PostgreSQLDataObjectStore dataStore, final DataSource dataSource) {
    this.dataStore = dataStore;
    this.dataSource = dataSource;
  }

  @Override
  public Attribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final String typePath = metaData.getPath();
    String owner = dataStore.getDatabaseSchemaName(PathUtil.getPath(typePath));
    if (!StringUtils.hasText(owner)) {
      owner = "public";
    }
    final String tableName = dataStore.getDatabaseTableName(typePath);
    final String columnName = name.toLowerCase();
    try {
      int srid = 0;
      String type = "geometry";
      int axisCount = 3;
      try {
        final String sql = "select SRID, TYPE, COORD_DIMENSION from GEOMETRY_COLUMNS where UPPER(F_TABLE_SCHEMA) = UPPER(?) AND UPPER(F_TABLE_NAME) = UPPER(?) AND UPPER(F_GEOMETRY_COLUMN) = UPPER(?)";
        final Map<String, Object> values = JdbcUtils.selectMap(dataSource, sql,
          owner, tableName, columnName);
        srid = (Integer)values.get("srid");
        type = (String)values.get("type");
        axisCount = (Integer)values.get("coord_dimension");
      } catch (final IllegalArgumentException e) {
        LOG.warn("Cannot get geometry column metadata for " + typePath + "."
          + columnName);
      }

      final DataType dataType = DATA_TYPE_MAP.get(type);
      final com.revolsys.jts.geom.GeometryFactory storeGeometryFactory = dataStore.getGeometryFactory();
      final com.revolsys.jts.geom.GeometryFactory geometryFactory;
      if (storeGeometryFactory == null) {
        geometryFactory = GeometryFactory.floating(srid, axisCount);
      } else {
        geometryFactory = GeometryFactory.fixed(srid, axisCount,
          storeGeometryFactory.getScaleXY(), storeGeometryFactory.getScaleZ());
      }
      final Attribute attribute = new PostgreSQLGeometryJdbcAttribute(dbName,
        name, dataType, required, description, null, srid, axisCount,
        geometryFactory);
      metaData.addAttribute(attribute);
      attribute.setProperty(JdbcConstants.FUNCTION_INTERSECTS, new SqlFunction(
        "intersects(", ")"));
      attribute.setProperty(JdbcConstants.FUNCTION_BUFFER, new SqlFunction(
        "st_buffer(", ")"));
      attribute.setProperty(JdbcConstants.FUNCTION_EQUAL, new SqlFunction(
        "st_equals(", ")"));
      attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
        geometryFactory);
      return attribute;
    } catch (final SQLException e) {
      LOG.error("Attribute not registered in GEOMETRY_COLUMN table " + owner
        + "." + tableName + "." + name, e);
      return null;
    } catch (final Throwable e) {
      LOG.error("Error registering attribute " + name, e);
      return null;
    }
  }
}
