package com.revolsys.gis.postgresql;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.Path;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.Property;

public class PostgreSQLGeometryFieldAdder extends JdbcFieldAdder {

  private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLGeometryFieldAdder.class);

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

  private final PostgreSQLRecordStore recordStore;

  public PostgreSQLGeometryFieldAdder(final PostgreSQLRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  public FieldDefinition addField(final RecordDefinitionImpl recordDefinition,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final String typePath = recordDefinition.getPath();
    String owner = this.recordStore.getDatabaseSchemaName(Path.getPath(typePath));
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
        final Map<String, Object> values = JdbcUtils.selectMap(
          this.recordStore, sql, owner, tableName, columnName);
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
      final FieldDefinition field = new PostgreSQLGeometryJdbcFieldDefinition(
        dbName, name, dataType, required, description, null, srid, axisCount,
        geometryFactory);
      recordDefinition.addField(field);
      field.setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
      return field;
    } catch (final Throwable e) {
      LOG.error("Attribute not registered in GEOMETRY_COLUMN table " + owner
        + "." + tableName + "." + name, e);
      return null;
    }
  }
}
