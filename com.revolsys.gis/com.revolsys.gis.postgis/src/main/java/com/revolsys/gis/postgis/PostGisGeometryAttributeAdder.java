package com.revolsys.gis.postgis;

import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.JdbcConstants;
import com.revolsys.gis.jdbc.io.SqlFunction;
import com.revolsys.jdbc.JdbcUtils;

public class PostGisGeometryAttributeAdder extends JdbcAttributeAdder {

  private final DataSource dataSource;

  public PostGisGeometryAttributeAdder(
    final DataSource dataSource) {
    super(DataTypes.GEOMETRY);
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
      final String sql = "select SRID from GEOMETRY_COLUMNS where F_TABLE_SCHEMA = ? AND F_TABLE_NAME = ? AND F_GEOMETRY_COLUMN = ?";
      final int srid = JdbcUtils.selectInt(dataSource, sql, owner, tableName,
        columnName);
      final Attribute attribute = new PostGisGeometryJdbcAttribute(name,
        DataTypes.GEOMETRY, length, scale, required, null, srid);
      metaData.addAttribute(attribute);
      attribute.setProperty(JdbcConstants.FUNCTION_INTERSECTS, new SqlFunction(
        "SDE.ST_INTERSECTS(", ") = 1"));
      attribute.setProperty(AttributeProperties.SRID, srid);
      attribute.setProperty(AttributeProperties.COORDINATE_SYSTEM,
        EpsgCoordinateSystems.getCoordinateSystem(srid));
      return attribute;
    } catch (final SQLException e) {
      throw new IllegalArgumentException(
        "Attribute not registered in GEOMETRY_COLUMN table " + owner + "."
          + tableName + "." + name, e);
    }
  }
}
