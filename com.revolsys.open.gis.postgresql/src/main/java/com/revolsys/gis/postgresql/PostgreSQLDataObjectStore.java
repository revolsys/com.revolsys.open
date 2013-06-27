package com.revolsys.gis.postgresql;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;

public class PostgreSQLDataObjectStore extends AbstractJdbcDataObjectStore {

  public static final List<String> POSTGRESQL_INTERNAL_SCHEMAS = Arrays.asList(
    "information_schema", "pg_catalog", "pg_toast_temp_1");

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

  public PostgreSQLDataObjectStore(
    final PostgreSQLDatabaseFactory databaseFactory,
    final Map<String, ? extends Object> connectionProperties) {
    super(databaseFactory);
    final DataSource dataSource = databaseFactory.createDataSource(connectionProperties);
    setDataSource(dataSource);

  }

  @Override
  public AbstractIterator<DataObject> createIterator(final Query query,
    final Map<String, Object> properties) {
    return new PostgreSQLJdbcQueryIterator(this, query, properties);
  }

  @Override
  protected Set<String> getDatabaseSchemaNames() {
    final Set<String> databaseSchemaNames = super.getDatabaseSchemaNames();
    databaseSchemaNames.removeAll(POSTGRESQL_INTERNAL_SCHEMAS);
    return databaseSchemaNames;
  }

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    return "nextval('" + sequenceName + "')";
  }

  @Override
  public Object getNextPrimaryKey(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    return getNextPrimaryKey(sequenceName);
  }

  @Override
  public Object getNextPrimaryKey(final String sequenceName) {
    final String sql = "SELECT nextval(?)";
    try {
      return JdbcUtils.selectLong(getDataSource(), getConnection(), sql,
        sequenceName);
    } catch (final SQLException e) {
      throw new IllegalArgumentException("Cannot create ID for " + sequenceName);
    }
  }

  @Override
  public int getRowCount(Query query) {
    BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      final String typePath = query.getTypeName();
      final DataObjectMetaData metaData = getMetaData(typePath);
      if (metaData == null) {
        throw new IllegalArgumentException("Unable to  find table " + typePath);
      } else {
        query = query.clone();
        query.setAttributeNames("count(*))");
        final String geometryAttributeName = metaData.getGeometryAttributeName();
        final GeometryFactory geometryFactory = metaData.getGeometryFactory();
        boundingBox = boundingBox.convert(geometryFactory);
        final double x1 = boundingBox.getMinX();
        final double y1 = boundingBox.getMinY();
        final double x2 = boundingBox.getMaxX();
        final double y2 = boundingBox.getMaxY();

        final PGbox box = new PGbox(x1, y1, x2, y2);
        query.and(new BinaryCondition(geometryAttributeName, "&&", box));
      }
    }

    return super.getRowCount(query);
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    final String typePath = metaData.getPath();
    final String schema = getDatabaseSchemaName(PathUtil.getPath(typePath));
    final String shortName = ShortNameProperty.getShortName(metaData);
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
    setPermissionsSql("select table_schema \"SCHEMA\", table_name, privilege_type \"PRIVILEGE\" from information_schema.role_table_grants "
      + "where grantee  in (current_user, 'PUBLIC') or grantee in (select role_name from information_schema.applicable_roles where grantee = current_user) AND "
      + "privilege_type IN ('SELECT', 'INSERT','UPDATE','DELETE') "
      + "order by table_schema, table_name, privilege_type");
  }

  @Override
  public boolean isSchemaExcluded(final String schemaName) {
    return POSTGRESQL_INTERNAL_SCHEMAS.contains(schemaName);
  }

  public boolean isUseSchemaSequencePrefix() {
    return useSchemaSequencePrefix;
  }

  @Override
  public ResultPager<DataObject> page(final Query query) {
    return new PostgreSQLJdbcQueryResultPager(this, getProperties(), query);
  }

  public void setUseSchemaSequencePrefix(final boolean useSchemaSequencePrefix) {
    this.useSchemaSequencePrefix = useSchemaSequencePrefix;
  }
}
