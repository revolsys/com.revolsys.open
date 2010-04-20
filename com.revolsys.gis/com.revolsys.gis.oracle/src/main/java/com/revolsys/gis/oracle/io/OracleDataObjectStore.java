package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcQueryReader;
import com.revolsys.gis.oracle.esri.ArcSdeObjectIdJdbcAttribute;
import com.revolsys.gis.oracle.esri.ArcSdeOracleStGeometryJdbcAttribute;
import com.revolsys.gis.oracle.esri.StGeometryAttributeAdder;
import com.revolsys.jdbc.JdbcUtils;
import com.vividsolutions.jts.geom.Envelope;

public class OracleDataObjectStore extends JdbcDataObjectStore {
  private boolean initialized;

  public OracleDataObjectStore() {
   this(new ArrayDataObjectFactory());
  }

  public OracleDataObjectStore(
    final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
    setExcludeTablePatterns(".*\\$");
  }

  public OracleDataObjectStore(
    final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this(dataObjectFactory);
    setDataSource(dataSource);
  }

  @Override
  public String getGeneratePrimaryKeySql(
    final DataObjectMetaData metaData) {
    final String shortName = ShortNameProperty.getShortName(metaData);
    return shortName + "_SEQ.NEXTVAL";
  }

  @Override
  public long getNextPrimaryKey(
    final DataObjectMetaData metaData) {
    final String shortName = ShortNameProperty.getShortName(metaData);
    final String sequenceName = shortName + "_SEQ";
    return getNextPrimaryKey(sequenceName);
  }

  @Override
  public long getNextPrimaryKey(
    final String sequenceName) {
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
      addAttributeAdder("NVARCHAR2", attributeAdder);
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

  @Override
  public Reader query(
    final QName typeName,
    final Envelope envelope) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    final String geometryColumnName = geometryAttribute.getName();

    final double x1 = envelope.getMinX();
    final double y1 = envelope.getMinY();
    final double x2 = envelope.getMaxX();
    final double y2 = envelope.getMaxY();
    final String sql = "SELECT * FROM "
      + typeName.getNamespaceURI()
      + "."
      + typeName.getLocalPart()
      + " WHERE "
      + " SDO_RELATE("
      + geometryColumnName
      + ","
      +

      "MDSYS.SDO_GEOMETRY(2003,?,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?))"
      + ",'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'";
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(3005);
    parameters.add(x1);
    parameters.add(y1);
    parameters.add(x2);
    parameters.add(y2);
    final JdbcQueryReader reader = createReader();
    reader.addQuery(typeName, sql, parameters);
    return reader;
  }

}
