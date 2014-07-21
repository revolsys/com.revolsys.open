package com.revolsys.gis.postgresql;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.Property;

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

  private final PostgreSQLRecordStore recordStore;

  public PostgreSQLGeometryAttributeAdder(
    final PostgreSQLRecordStore recordStore, final DataSource dataSource) {
    this.recordStore = recordStore;
    this.dataSource = dataSource;
  }

  @Override
  public Attribute addAttribute(final RecordDefinitionImpl recordDefinition,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final String typePath = recordDefinition.getPath();
    String owner = this.recordStore.getDatabaseSchemaName(PathUtil.getPath(typePath));
    if (!Property.hasValue(owner)) {
      owner = "public";
    }
    final String tableName = this.recordStore.getDatabaseTableName(typePath);
    final String columnName = name.toLowerCase();
    try {
      int srid = 0;
      String type = "geometry";
      int axisCount = 3;
      try {
        final String sql = "select SRID, TYPE, COORD_DIMENSION from GEOMETRY_COLUMNS where UPPER(F_TABLE_SCHEMA) = UPPER(?) AND UPPER(F_TABLE_NAME) = UPPER(?) AND UPPER(F_GEOMETRY_COLUMN) = UPPER(?)";
        final Map<String, Object> values = JdbcUtils.selectMap(this.dataSource,
          sql, owner, tableName, columnName);
        srid = (Integer)values.get("srid");
        type = (String)values.get("type");
        axisCount = (Integer)values.get("coord_dimension");
      } catch (final IllegalArgumentException e) {
        LOG.warn("Cannot get geometry column metadata for " + typePath + "."
            + columnName);
      }

      final DataType dataType = DATA_TYPE_MAP.get(type);
      final GeometryFactory storeGeometryFactory = this.recordStore.getGeometryFactory();
      final GeometryFactory geometryFactory;
      if (storeGeometryFactory == null) {
        geometryFactory = GeometryFactory.floating(srid, axisCount);
      } else {
        geometryFactory = GeometryFactory.fixed(srid, axisCount,
          storeGeometryFactory.getScaleXY(), storeGeometryFactory.getScaleZ());
      }
      final Attribute attribute = new PostgreSQLGeometryJdbcAttribute(dbName,
        name, dataType, required, description, null, srid, axisCount,
        geometryFactory);
      recordDefinition.addAttribute(attribute);
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
