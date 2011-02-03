package com.revolsys.gis.postgis;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.JdbcConstants;
import com.revolsys.gis.jdbc.io.SqlFunction;
import com.revolsys.jdbc.JdbcUtils;

public class PostGisGeometryAttributeAdder extends JdbcAttributeAdder {

  private static final Map<String, DataType> DATA_TYPE_MAP = new HashMap<String, DataType>();

  static {
    DATA_TYPE_MAP.put("GEOMETRY", DataTypes.GEOMETRY);
    DATA_TYPE_MAP.put("POINT", DataTypes.POINT);
    DATA_TYPE_MAP.put("LINESTRING", DataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("POLYGON", DataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("MULTIPOINT", DataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put("MULTILINESTRING", DataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("MULTIPOLYGON", DataTypes.MULTI_POLYGON);
  }

  private final DataSource dataSource;

  public PostGisGeometryAttributeAdder(
    final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Attribute addAttribute(
    final DataObjectMetaDataImpl metaData,
    final String name,
    final int sqlType,
    final int length,
    final int scale,
    final boolean required) {
    final QName typeName = metaData.getName();
    String owner = typeName.getNamespaceURI().toLowerCase();
    if (owner.equals("")) {
      owner = "public";
    }
    final String tableName = typeName.getLocalPart().toLowerCase();
    final String columnName = name.toLowerCase();
    try {
      final String sql = "select SRID, TYPE from GEOMETRY_COLUMNS where F_TABLE_SCHEMA = ? AND F_TABLE_NAME = ? AND F_GEOMETRY_COLUMN = ?";
      final Map<String, Object> values = JdbcUtils.selectMap(dataSource, sql,
        owner, tableName, columnName);
      int srid = (Integer)values.get("srid");
      String type = (String)values.get("type");
      final DataType dataType = DATA_TYPE_MAP.get(type);
      final Attribute attribute = new PostGisGeometryJdbcAttribute(name,
        dataType, length, scale, required, null, srid);
      metaData.addAttribute(attribute);
      attribute.setProperty(JdbcConstants.FUNCTION_INTERSECTS, new SqlFunction(
        "intersects(", ")"));
      attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
        GeometryFactory.getFactory(srid));
      return attribute;
    } catch (final SQLException e) {
      throw new IllegalArgumentException(
        "Attribute not registered in GEOMETRY_COLUMN table " + owner + "."
          + tableName + "." + name, e);
    }
  }
}
