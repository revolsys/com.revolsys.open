package com.revolsys.gis.postgis;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.postgresql.geometric.PGbox;

import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.JdbcUtils;
import com.vividsolutions.jts.geom.Envelope;

public class PostGisDataObjectStore extends JdbcDataObjectStore {
  public PostGisDataObjectStore(
    final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  public PostGisDataObjectStore(
    final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this(dataObjectFactory);
    setDataSource(dataSource);
  }

  @Override
  public String getGeneratePrimaryKeySql(
    final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    final String schema = typeName.getNamespaceURI();
    final String tableName = typeName.getLocalPart();
    final String idAttributeName = metaData.getIdAttributeName();
    return "nextval('" + schema + "." + tableName + "_" + idAttributeName
      + "_seq')";
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
    final String sql = "SELECT nextval(?)";
    try {
      return JdbcUtils.selectLong(getDataSource(), getConnection(), sql,
        sequenceName);
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Cannot create ID for " + sequenceName);
    }
  }

  @Override
  @PostConstruct
  public void initialize() {
    super.initialize();
    final JdbcAttributeAdder numberAttributeAdder = new JdbcAttributeAdder(
      DataTypes.DECIMAL);
    addAttributeAdder("numeric", numberAttributeAdder);

    final JdbcAttributeAdder stringAttributeAdder = new JdbcAttributeAdder(
      DataTypes.STRING);
    addAttributeAdder("varchar", stringAttributeAdder);
    addAttributeAdder("text", stringAttributeAdder);

    final JdbcAttributeAdder longAttributeAdder = new JdbcAttributeAdder(
      DataTypes.LONG);
    addAttributeAdder("int8", longAttributeAdder);
    addAttributeAdder("bigint", longAttributeAdder);

    final JdbcAttributeAdder intAttributeAdder = new JdbcAttributeAdder(
      DataTypes.INT);
    addAttributeAdder("int4", intAttributeAdder);
    addAttributeAdder("integer", intAttributeAdder);

    final JdbcAttributeAdder shortAttributeAdder = new JdbcAttributeAdder(
      DataTypes.SHORT);
    addAttributeAdder("int2", shortAttributeAdder);
    addAttributeAdder("smallint", shortAttributeAdder);

    final JdbcAttributeAdder floatAttributeAdder = new JdbcAttributeAdder(
      DataTypes.FLOAT);
    addAttributeAdder("float4", floatAttributeAdder);

    final JdbcAttributeAdder doubleAttributeAdder = new JdbcAttributeAdder(
      DataTypes.DOUBLE);
    addAttributeAdder("float8", doubleAttributeAdder);
    addAttributeAdder("double precision", doubleAttributeAdder);

    addAttributeAdder("date", new JdbcAttributeAdder(DataTypes.DATE_TIME));

    addAttributeAdder("bool", new JdbcAttributeAdder(DataTypes.BOOLEAN));

    final JdbcAttributeAdder geometryAttributeAdder = new PostGisGeometryAttributeAdder(
      getDataSource());
    addAttributeAdder("geometry", geometryAttributeAdder);
  }

  @Override
  public Reader query(
    final QName typeName,
    final Envelope envelope) {
    final double x1 = envelope.getMinX();
    final double y1 = envelope.getMinY();
    final double x2 = envelope.getMaxX();
    final double y2 = envelope.getMaxY();
    final String sql = "SELECT * FROM " + typeName.getNamespaceURI() + "."
      + typeName.getLocalPart() + " WHERE GEOMETRY && ?";
    final Reader reader = createReader(typeName, sql, new PGbox(x1, y1, x2, y2));
    return reader;
  }

}
