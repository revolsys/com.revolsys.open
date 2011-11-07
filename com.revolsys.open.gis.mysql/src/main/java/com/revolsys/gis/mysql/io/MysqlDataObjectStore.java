package com.revolsys.gis.mysql.io;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.JdbcUtils;

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

  public Object getNextPrimaryKey(final DataObjectMetaData metaData) {
    final String shortName = ShortNameProperty.getShortName(metaData);
    final String sequenceName = shortName + "_SEQ";
    return getNextPrimaryKey(sequenceName);
  }

  public Object getNextPrimaryKey(final String sequenceName) {
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


}
