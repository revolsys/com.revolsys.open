package com.revolsys.gis.postgis;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.jdbc.JdbcUtils;

public class PostGisGeometryAttributeAdder extends JdbcAttributeAdder {

  private static final Logger LOG = LoggerFactory.getLogger(PostGisGeometryAttributeAdder.class);

  private static final Map<String, DataType> DATA_TYPE_MAP = new HashMap<String, DataType>();

  static {
    DATA_TYPE_MAP.put("GEOMETRY", DataTypes.GEOMETRY);
    DATA_TYPE_MAP.put("POINT", DataTypes.POINT);
    DATA_TYPE_MAP.put("LINESTRING", DataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("POLYGON", DataTypes.POLYGON);
    DATA_TYPE_MAP.put("MULTIPOINT", DataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put("MULTILINESTRING", DataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("MULTIPOLYGON", DataTypes.MULTI_POLYGON);
  }

  private final DataSource dataSource;

  private PostGisDataObjectStore dataStore;

  public PostGisGeometryAttributeAdder(PostGisDataObjectStore dataStore,
    final DataSource dataSource) {
    this.dataStore = dataStore;
    this.dataSource = dataSource;
  }

  @Override
  public Attribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String name, final int sqlType, final int length, final int scale,
    final boolean required) {
    final QName typeName = metaData.getName();
    String owner = dataStore.getDatabaseSchemaName(typeName.getNamespaceURI());
    if (owner.equals("")) {
      owner = "public";
    }
    final String tableName = dataStore.getDatabaseTableName(typeName);
    final String columnName = name.toLowerCase();
    try {
      int srid = 0;
      String type = "GEOMETRY";
      int numAxis = 3;
      try {
        final String sql = "select SRID, TYPE, COORD_DIMENSION from GEOMETRY_COLUMNS where F_TABLE_SCHEMA = ? AND F_TABLE_NAME = ? AND F_GEOMETRY_COLUMN = ?";
        final Map<String, Object> values = JdbcUtils.selectMap(dataSource, sql,
          owner, tableName, columnName);
        srid = (Integer)values.get("srid");
        type = (String)values.get("type");
        numAxis = (Integer)values.get("coord_dimension");
      } catch (IllegalArgumentException e) {
        LOG.warn("Cannot get geometry column metadata for " + typeName + "."
          + columnName);
      }

      final DataType dataType = DATA_TYPE_MAP.get(type);
      CoordinatesPrecisionModel precisionModel = dataStore.getPrecisionModel();
      final GeometryFactory geometryFactory;
      if (precisionModel == null) {
        geometryFactory = new GeometryFactory(srid, numAxis);
      } else {
        geometryFactory = new GeometryFactory(
          EpsgCoordinateSystems.getCoordinateSystem(srid), precisionModel,
          numAxis);
      }
      final Attribute attribute = new PostGisGeometryJdbcAttribute(name,
        dataType, length, scale, required, null, srid, numAxis, geometryFactory);
      metaData.addAttribute(attribute);
      attribute.setProperty(JdbcConstants.FUNCTION_INTERSECTS, new SqlFunction(
        "intersects(", ")"));
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
