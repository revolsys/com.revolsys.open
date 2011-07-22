package com.revolsys.gis.mysql.io;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcQuery;
import com.revolsys.gis.jdbc.io.JdbcQueryReader;
import com.revolsys.io.Reader;
import com.revolsys.jdbc.JdbcUtils;
import com.vividsolutions.jts.geom.Envelope;

public class MysqlDataObjectStore extends AbstractJdbcDataObjectStore {
  private boolean initialized;

  public MysqlDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public MysqlDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  public MysqlDataObjectStore(final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this(dataObjectFactory);
    setDataSource(dataSource);
  }

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    final String shortName = ShortNameProperty.getShortName(metaData);
    if (shortName == null) {
      ShortNameProperty.getShortName(metaData);
      throw new IllegalArgumentException("No sequence for "
        + metaData.getName());
    }
    return shortName + "_SEQ.NEXTVAL";
  }

  public long getNextPrimaryKey(final DataObjectMetaData metaData) {
    final String shortName = ShortNameProperty.getShortName(metaData);
    final String sequenceName = shortName + "_SEQ";
    return getNextPrimaryKey(sequenceName);
  }

  public long getNextPrimaryKey(final String sequenceName) {
    final String sql = "SELECT " + sequenceName + ".NEXTVAL FROM SYS.DUAL";
    try {
      return JdbcUtils.selectLong(getDataSource(), getConnection(), sql);
    } catch (final SQLException e) {
      throw new IllegalArgumentException(
        "Cannot create ID for " + sequenceName, e);
    }
  }

  @Override
  @PostConstruct
  public void initialize() {
    super.initialize();
    if (!initialized) {
      initialized = true;
      final JdbcAttributeAdder attributeAdder = new JdbcAttributeAdder();
      addAttributeAdder("INTEGER", attributeAdder);
      addAttributeAdder("SMALLINT", attributeAdder);
      addAttributeAdder("DECIMAL", attributeAdder);
      addAttributeAdder("NUMERIC", attributeAdder);
      addAttributeAdder("FLOAT", attributeAdder);
      addAttributeAdder("REAL", attributeAdder);
      addAttributeAdder("INT", attributeAdder);
      addAttributeAdder("DEC", attributeAdder);
      addAttributeAdder("FIXED", attributeAdder);
      addAttributeAdder("DOUBLE", attributeAdder);
      addAttributeAdder("DOUBLE PRECISION", attributeAdder);

      addAttributeAdder("CHAR", attributeAdder);
      addAttributeAdder("VARCHAR", attributeAdder);
      addAttributeAdder("BINARY", attributeAdder);
      addAttributeAdder("VARBINARY", attributeAdder);
      addAttributeAdder("BLOB", new JdbcAttributeAdder(DataTypes.STRING));
      addAttributeAdder("TEXT", attributeAdder);
      addAttributeAdder("ENUM", attributeAdder);
      addAttributeAdder("SET", attributeAdder);

      addAttributeAdder("DATETIME", attributeAdder);
      addAttributeAdder("DATE", attributeAdder);
      addAttributeAdder("TIMESTAMP", attributeAdder);
      addAttributeAdder("TIME", attributeAdder);
      addAttributeAdder("YEAR", attributeAdder);

      final MysqlSdoGeometryAttributeAdder geometryAttributeAdder = new MysqlSdoGeometryAttributeAdder(
        getDataSource());
      addAttributeAdder("GEOMETRY", geometryAttributeAdder);
      addAttributeAdder("POINT", geometryAttributeAdder);
      addAttributeAdder("CURVE", geometryAttributeAdder);
      addAttributeAdder("LINESTRING", geometryAttributeAdder);
      addAttributeAdder("SURFACE", geometryAttributeAdder);
      addAttributeAdder("POLYGON", geometryAttributeAdder);
      addAttributeAdder("GEOMETRYCOLLECTION", geometryAttributeAdder);
      addAttributeAdder("MULTIPOINT", geometryAttributeAdder);
      addAttributeAdder("MULTICURVE", geometryAttributeAdder);
      addAttributeAdder("MULTILINESTRING", geometryAttributeAdder);
      addAttributeAdder("MULTISURFACE", geometryAttributeAdder);
      addAttributeAdder("MULTIPOLYGON", geometryAttributeAdder);
    }
  }

  @Override
  public Reader query(final QName typeName, final Envelope envelope) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    final String geometryColumnName = geometryAttribute.getName();
    GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);

    final double x1 = envelope.getMinX();
    final double y1 = envelope.getMinY();
    final double x2 = envelope.getMaxX();
    final double y2 = envelope.getMaxY();

    final StringBuffer sql = new StringBuffer();
    JdbcQuery.addColumnsAndTableName(sql, metaData, "T", null);
    sql.append(" WHERE ");
    sql.append(" SDO_RELATE("
      + geometryColumnName
      + ","
      +

      "MDSYS.SDO_GEOMETRY(2003,?,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?))"
      + ",'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'");
    final JdbcQueryReader reader = createReader();
    JdbcQuery query = new JdbcQuery(metaData, sql.toString(),
      geometryFactory.getSRID(), x1, y1, x2, y2);
    reader.addQuery(query);
    return reader;
  }

}
