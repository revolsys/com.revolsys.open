package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcQuery;
import com.revolsys.gis.jdbc.io.JdbcQueryReader;
import com.revolsys.gis.oracle.esri.ArcSdeObjectIdJdbcAttribute;
import com.revolsys.gis.oracle.esri.ArcSdeOracleStGeometryJdbcAttribute;
import com.revolsys.gis.oracle.esri.StGeometryAttributeAdder;
import com.revolsys.io.Reader;
import com.revolsys.jdbc.JdbcUtils;

public class OracleDataObjectStore extends AbstractJdbcDataObjectStore {
  private boolean initialized;

  public OracleDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public OracleDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
    setExcludeTablePatterns(".*\\$");
  }

  public OracleDataObjectStore(final DataObjectFactory dataObjectFactory,
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
      addAttributeAdder("NUMBER", attributeAdder);

      addAttributeAdder("CHAR", attributeAdder);
      addAttributeAdder("NCHAR", attributeAdder);
      addAttributeAdder("VARCHAR", attributeAdder);
      addAttributeAdder("VARCHAR2", attributeAdder);
      addAttributeAdder("NVARCHAR2", new JdbcAttributeAdder(DataTypes.STRING));
      addAttributeAdder("LONG", attributeAdder);
      addAttributeAdder("CLOB", attributeAdder);
      addAttributeAdder("NCLOB", attributeAdder);

      addAttributeAdder("DATE", attributeAdder);
      addAttributeAdder("TIMESTAMP", attributeAdder);

      final JdbcAttributeAdder stGeometryAttributeAdder = new StGeometryAttributeAdder(
        getDataSource(), getConnection());
      addAttributeAdder("ST_GEOMETRY", stGeometryAttributeAdder);
      addAttributeAdder("SDE.ST_GEOMETRY", stGeometryAttributeAdder);

      final OracleSdoGeometryAttributeAdder sdoGeometryAttributeAdder = new OracleSdoGeometryAttributeAdder(
        getDataSource());
      addAttributeAdder("SDO_GEOMETRY", sdoGeometryAttributeAdder);
      addAttributeAdder("MDSYS.SDO_GEOMETRY", sdoGeometryAttributeAdder);
      setSqlPrefix("BEGIN ");
      setSqlSuffix(";END;");
    }
  }

  @Override
  protected void postCreateDataObjectMetaData(
    final DataObjectMetaDataImpl metaData) {
    final JdbcAttribute objectIdAttribute = (JdbcAttribute)metaData.getAttribute("OBJECTID");
    if (objectIdAttribute != null) {
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute instanceof ArcSdeOracleStGeometryJdbcAttribute) {
        final Connection connection = getDbConnection();
        try {
          final Attribute newObjectIdAttribute = ArcSdeObjectIdJdbcAttribute.getInstance(
            objectIdAttribute, connection, metaData.getName());
          if (newObjectIdAttribute != null) {
            metaData.replaceAttribute(objectIdAttribute, newObjectIdAttribute);
          }
        } finally {
          releaseConnection(connection);
        }
      }
    }
  }

  public JdbcQuery createQuery(final QName typeName,
    final BoundingBox boundingBox) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    final String geometryColumnName = geometryAttribute.getName();
    GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);

    final BoundingBox projectedBoundingBox = boundingBox.convert(geometryFactory);

    final double x1 = projectedBoundingBox.getMinX();
    final double y1 = projectedBoundingBox.getMinY();
    final double x2 = projectedBoundingBox.getMaxX();
    final double y2 = projectedBoundingBox.getMaxY();

    final StringBuffer sql = new StringBuffer();
    JdbcQuery.addColumnsAndTableName(sql, metaData, "T", null);
    sql.append(" WHERE ");
    sql.append(" SDO_RELATE("
      + geometryColumnName
      + ","
      +

      "MDSYS.SDO_GEOMETRY(2003,?,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?))"
      + ",'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'");
    JdbcQuery query = new JdbcQuery(metaData, sql.toString(),
      geometryFactory.getSRID(), x1, y1, x2, y2);
    return query;
  }

}
