package com.revolsys.gis.postgresql;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ResultPager;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.query.functions.EnvelopeIntersects;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.property.ShortNameProperty;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;
import com.revolsys.io.Path;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcFieldAdder;
import com.revolsys.jdbc.attribute.JdbcFieldDefinition;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.util.Property;

public class PostgreSQLRecordStore extends AbstractJdbcRecordStore {

  public static final AbstractIterator<Record> createPostgreSQLIterator(
    final PostgreSQLRecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    return new PostgreSQLJdbcQueryIterator(recordStore, query, properties);
  }

  public static final List<String> POSTGRESQL_INTERNAL_SCHEMAS = Arrays.asList(
    "information_schema", "pg_catalog", "pg_toast_temp_1");

  private boolean useSchemaSequencePrefix = true;

  public PostgreSQLRecordStore() {
    this(new ArrayRecordFactory());
  }

  public PostgreSQLRecordStore(final DataSource dataSource) {
    super(dataSource);
    initSettings();
  }

  public PostgreSQLRecordStore(final PostgreSQLDatabaseFactory databaseFactory,
    final Map<String, ? extends Object> connectionProperties) {
    super(databaseFactory);
    final DataSource dataSource = databaseFactory.createDataSource(connectionProperties);
    setDataSource(dataSource);
    initSettings();
    setConnectionProperties(connectionProperties);
  }

  public PostgreSQLRecordStore(final RecordFactory recordFactory) {
    super(recordFactory);
    initSettings();
  }

  public PostgreSQLRecordStore(final RecordFactory recordFactory,
    final DataSource dataSource) {
    this(recordFactory);
    setDataSource(dataSource);
  }

  @Override
  protected JdbcFieldDefinition addField(
    final RecordDefinitionImpl recordDefinition, final String dbColumnName,
    final String name, final String dataType, final int sqlType,
    final int length, final int scale, final boolean required,
    final String description) {
    final JdbcFieldDefinition attribute = super.addField(recordDefinition,
      dbColumnName, name, dataType, sqlType, length, scale, required,
      description);
    if (!dbColumnName.matches("[a-z_]")) {
      attribute.setQuoteName(true);
    }
    return attribute;
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    if (queryValue instanceof EnvelopeIntersects) {
      final EnvelopeIntersects envelopeIntersects = (EnvelopeIntersects)queryValue;
      final QueryValue boundingBox1Value = envelopeIntersects.getBoundingBox1Value();
      if (boundingBox1Value == null) {
        sql.append("NULL");
      } else {
        boundingBox1Value.appendSql(query, this, sql);
      }
      sql.append(" && ");
      final QueryValue boundingBox2Value = envelopeIntersects.getBoundingBox2Value();
      if (boundingBox2Value == null) {
        sql.append("NULL");
      } else {
        boundingBox2Value.appendSql(query, this, sql);
      }
    } else {
      super.appendQueryValue(query, sql, queryValue);
    }
  }

  @Override
  public String getGeneratePrimaryKeySql(final RecordDefinition recordDefinition) {
    final String sequenceName = getSequenceName(recordDefinition);
    return "nextval('" + sequenceName + "')";
  }

  @Override
  public Object getNextPrimaryKey(final RecordDefinition recordDefinition) {
    final String sequenceName = getSequenceName(recordDefinition);
    return getNextPrimaryKey(sequenceName);
  }

  @Override
  public Object getNextPrimaryKey(final String sequenceName) {
    final String sql = "SELECT nextval(?)";
    return JdbcUtils.selectLong(this, sql, sequenceName);
  }

  public String getSequenceName(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final String schema = getDatabaseSchemaName(Path.getPath(typePath));
    final String shortName = ShortNameProperty.getShortName(recordDefinition);
    final String sequenceName;
    if (Property.hasValue(shortName)) {
      if (this.useSchemaSequencePrefix) {
        sequenceName = schema + "." + shortName.toLowerCase() + "_seq";
      } else {
        sequenceName = shortName.toLowerCase() + "_seq";
      }
    } else {
      final String tableName = getDatabaseTableName(typePath);
      final String idFieldName = recordDefinition.getIdFieldName()
        .toLowerCase();
      if (this.useSchemaSequencePrefix) {
        sequenceName = schema + "." + tableName + "_" + idFieldName
          + "_seq";
      } else {
        sequenceName = tableName + "_" + idFieldName + "_seq";
      }
    }
    return sequenceName;

  }

  @Override
  @PostConstruct
  public void initialize() {
    super.initialize();
    final JdbcFieldAdder numberAttributeAdder = new JdbcFieldAdder(
      DataTypes.DECIMAL);
    addFieldAdder("numeric", numberAttributeAdder);

    final JdbcFieldAdder stringAttributeAdder = new JdbcFieldAdder(
      DataTypes.STRING);
    addFieldAdder("varchar", stringAttributeAdder);
    addFieldAdder("text", stringAttributeAdder);
    addFieldAdder("name", stringAttributeAdder);
    addFieldAdder("bpchar", stringAttributeAdder);

    final JdbcFieldAdder longAttributeAdder = new JdbcFieldAdder(
      DataTypes.LONG);
    addFieldAdder("int8", longAttributeAdder);
    addFieldAdder("bigint", longAttributeAdder);
    addFieldAdder("bigserial", longAttributeAdder);
    addFieldAdder("serial8", longAttributeAdder);

    final JdbcFieldAdder intAttributeAdder = new JdbcFieldAdder(
      DataTypes.INT);
    addFieldAdder("int4", intAttributeAdder);
    addFieldAdder("integer", intAttributeAdder);
    addFieldAdder("serial", intAttributeAdder);
    addFieldAdder("serial4", intAttributeAdder);

    final JdbcFieldAdder shortAttributeAdder = new JdbcFieldAdder(
      DataTypes.SHORT);
    addFieldAdder("int2", shortAttributeAdder);
    addFieldAdder("smallint", shortAttributeAdder);

    final JdbcFieldAdder floatAttributeAdder = new JdbcFieldAdder(
      DataTypes.FLOAT);
    addFieldAdder("float4", floatAttributeAdder);

    final JdbcFieldAdder doubleAttributeAdder = new JdbcFieldAdder(
      DataTypes.DOUBLE);
    addFieldAdder("float8", doubleAttributeAdder);
    addFieldAdder("double precision", doubleAttributeAdder);

    addFieldAdder("date", new JdbcFieldAdder(DataTypes.DATE_TIME));

    addFieldAdder("bool", new JdbcFieldAdder(DataTypes.BOOLEAN));

    final JdbcFieldAdder geometryFieldAdder = new PostgreSQLGeometryFieldAdder(
      this);
    addFieldAdder("geometry", geometryFieldAdder);
    setPrimaryKeySql("SELECT t.relname \"TABLE_NAME\", c.attname \"COLUMN_NAME\"" //
      + " FROM pg_namespace s" //
      + " join pg_class t on t.relnamespace = s.oid" //
      + " join pg_index i on i.indrelid = t.oid " //
      + " join pg_attribute c on c.attrelid = t.oid" //
      + " WHERE s.nspname = ? AND c.attnum = any(i.indkey) AND i.indisprimary");
    setSchemaPermissionsSql("select distinct t.table_schema as \"SCHEMA_NAME\" "
      + "from information_schema.role_table_grants t  "
      + "where (t.grantee  in (current_user, 'PUBLIC') or "
      + "t.grantee in (select role_name from information_schema.applicable_roles r where r.grantee = current_user)) and "
      + "privilege_type IN ('SELECT', 'INSERT','UPDATE','DELETE') ");
    setTablePermissionsSql("select distinct t.table_schema as \"SCHEMA_NAME\", t.table_name, t.privilege_type as \"PRIVILEGE\", d.description as \"REMARKS\" from information_schema.role_table_grants t join pg_namespace n on t.table_schema = n.nspname join pg_class c on (n.oid = c.relnamespace AND t.table_name = c.relname) left join pg_description d on d.objoid = c.oid "
      + "where t.table_schema = ? and "
      + "(t.grantee  in (current_user, 'PUBLIC') or t.grantee in (select role_name from information_schema.applicable_roles r where r.grantee = current_user)) AND "
      + "privilege_type IN ('SELECT', 'INSERT','UPDATE','DELETE') "
      + "order by t.table_schema, t.table_name, t.privilege_type");
  }

  protected void initSettings() {
    setIteratorFactory(new RecordStoreIteratorFactory(
      PostgreSQLRecordStore.class, "createPostgreSQLIterator"));
  }

  @Override
  public boolean isSchemaExcluded(final String schemaName) {
    return POSTGRESQL_INTERNAL_SCHEMAS.contains(schemaName);
  }

  public boolean isUseSchemaSequencePrefix() {
    return this.useSchemaSequencePrefix;
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    return new PostgreSQLJdbcQueryResultPager(this, getProperties(), query);
  }

  public void setUseSchemaSequencePrefix(final boolean useSchemaSequencePrefix) {
    this.useSchemaSequencePrefix = useSchemaSequencePrefix;
  }
}
