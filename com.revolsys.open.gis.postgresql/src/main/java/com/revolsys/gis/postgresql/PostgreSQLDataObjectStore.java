package com.revolsys.gis.postgresql;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.postgresql.geometric.PGbox;
import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ResultPager;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;

public class PostgreSQLDataObjectStore extends AbstractJdbcDataObjectStore {

  private boolean useSchemaSequencePrefix = true;

  public PostgreSQLDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public PostgreSQLDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  public PostgreSQLDataObjectStore(final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this(dataObjectFactory);
    setDataSource(dataSource);
  }

  public PostgreSQLDataObjectStore(final DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public AbstractIterator<DataObject> createIterator(Query query,
    Map<String, Object> properties) {
    return new PostgreSQLJdbcQueryIterator(this, query, properties);
  }

  public PostgreSQLDataObjectStore(
    final PostgreSQLDatabaseFactory databaseFactory,
    final Map<String, ? extends Object> connectionProperties) {
    super(databaseFactory);
    final DataSource dataSource = databaseFactory.createDataSource(connectionProperties);
    setDataSource(dataSource);

  }

  @Override
  public ResultPager<DataObject> page(Query query) {
    return new PostgreSQLJdbcQueryResultPager(this, getProperties(), query);
  }

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    return "nextval('" + sequenceName + "')";
  }

  public Object getNextPrimaryKey(final DataObjectMetaData metaData) {
    String sequenceName = getSequenceName(metaData);
    return getNextPrimaryKey(sequenceName);
  }

  public Object getNextPrimaryKey(final String sequenceName) {
    final String sql = "SELECT nextval(?)";
    try {
      return JdbcUtils.selectLong(getDataSource(), getConnection(), sql,
        sequenceName);
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Cannot create ID for " + sequenceName);
    }
  }

  public void setUseSchemaSequencePrefix(boolean useSchemaSequencePrefix) {
    this.useSchemaSequencePrefix = useSchemaSequencePrefix;
  }

  public boolean isUseSchemaSequencePrefix() {
    return useSchemaSequencePrefix;
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    final String typePath = metaData.getPath();
    final String schema = getDatabaseSchemaName(PathUtil.getPath(typePath));
    String shortName = ShortNameProperty.getShortName(metaData);
    final String sequenceName;
    if (StringUtils.hasText(shortName)) {
      if (useSchemaSequencePrefix) {
        sequenceName = schema + "." + shortName.toLowerCase() + "_seq";
      } else {
        sequenceName = shortName.toLowerCase() + "_seq";
      }
    } else {
      final String tableName = getDatabaseTableName(typePath);
      final String idAttributeName = metaData.getIdAttributeName()
        .toLowerCase();
      if (useSchemaSequencePrefix) {
        sequenceName = schema + "." + tableName + "_" + idAttributeName
          + "_seq";
      } else {
        sequenceName = tableName + "_" + idAttributeName + "_seq";
      }
    }
    return sequenceName;

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
    addAttributeAdder("name", stringAttributeAdder);
    addAttributeAdder("bpchar", stringAttributeAdder);

    final JdbcAttributeAdder longAttributeAdder = new JdbcAttributeAdder(
      DataTypes.LONG);
    addAttributeAdder("int8", longAttributeAdder);
    addAttributeAdder("bigint", longAttributeAdder);
    addAttributeAdder("bigserial", longAttributeAdder);
    addAttributeAdder("serial8", longAttributeAdder);

    final JdbcAttributeAdder intAttributeAdder = new JdbcAttributeAdder(
      DataTypes.INT);
    addAttributeAdder("int4", intAttributeAdder);
    addAttributeAdder("integer", intAttributeAdder);
    addAttributeAdder("serial", intAttributeAdder);
    addAttributeAdder("serial4", intAttributeAdder);

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

    final JdbcAttributeAdder geometryAttributeAdder = new PostgreSQLGeometryAttributeAdder(
      this, getDataSource());
    addAttributeAdder("geometry", geometryAttributeAdder);
  }
}
