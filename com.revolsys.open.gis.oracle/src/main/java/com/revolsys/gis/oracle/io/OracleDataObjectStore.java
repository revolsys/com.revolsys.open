package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.ResultPager;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.oracle.esri.ArcSdeObjectIdJdbcAttribute;
import com.revolsys.gis.oracle.esri.ArcSdeOracleStGeometryJdbcAttribute;
import com.revolsys.gis.oracle.esri.StGeometryAttributeAdder;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;

public class OracleDataObjectStore extends AbstractJdbcDataObjectStore {
  private boolean initialized;

  public OracleDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public OracleDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
    setExcludeTablePatterns(".*\\$");
    setSqlPrefix("BEGIN ");
    setSqlSuffix(";END;");
  }

  public OracleDataObjectStore(final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this(dataObjectFactory);
    setDataSource(dataSource);
  }

  @Override
  public AbstractIterator<DataObject> createIterator(Query query,
    Map<String, Object> properties) {
    return new OracleJdbcQueryIterator(this, query, properties);
  }

  public OracleDataObjectStore(OracleDatabaseFactory databaseFactory,
    Map<String, ? extends Object> connectionProperties) {
    super(databaseFactory);
    setExcludeTablePatterns(".*\\$");
    DataSource dataSource = databaseFactory.createDataSource(connectionProperties);
    setDataSource(dataSource);
    setSqlPrefix("BEGIN ");
    setSqlSuffix(";END;");
  }

  public OracleDataObjectStore(DataSource dataSource) {
    super(dataSource);
    setExcludeTablePatterns(".*\\$");
    setSqlPrefix("BEGIN ");
    setSqlSuffix(";END;");
  }

  @Override
  public ResultPager<DataObject> page(Query query) {
    return new OracleJdbcQueryResultPager(this, getProperties(), query);
  }

  private boolean useSchemaSequencePrefix = true;

  public void setUseSchemaSequencePrefix(boolean useSchemaSequencePrefix) {
    this.useSchemaSequencePrefix = useSchemaSequencePrefix;
  }

  public boolean isUseSchemaSequencePrefix() {
    return useSchemaSequencePrefix;
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    if (metaData == null) {
      return null;
    } else {
      final String typePath = metaData.getPath();
      final String schema = getDatabaseSchemaName(PathUtil.getPath(typePath));
      String shortName = ShortNameProperty.getShortName(metaData);
      final String sequenceName;
      if (StringUtils.hasText(shortName)) {
        if (useSchemaSequencePrefix) {
          sequenceName = schema + "." + shortName.toLowerCase() + "_SEQ";
        } else {
          sequenceName = shortName.toLowerCase() + "_SEQ";
        }
      } else {
        final String tableName = getDatabaseTableName(typePath);
        if (useSchemaSequencePrefix) {
          sequenceName = schema + "." + tableName + "_SEQ";
        } else {
          sequenceName = tableName + "_SEQ";
        }
      }
      return sequenceName;
    }
  }

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    String sequenceName = getSequenceName(metaData);
    return sequenceName + ".NEXTVAL";
  }

  public Object getNextPrimaryKey(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
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
        this, getDataSource(), getConnection());
      addAttributeAdder("ST_GEOMETRY", stGeometryAttributeAdder);
      addAttributeAdder("SDE.ST_GEOMETRY", stGeometryAttributeAdder);

      final OracleSdoGeometryAttributeAdder sdoGeometryAttributeAdder = new OracleSdoGeometryAttributeAdder(
        this, getDataSource());
      addAttributeAdder("SDO_GEOMETRY", sdoGeometryAttributeAdder);
      addAttributeAdder("MDSYS.SDO_GEOMETRY", sdoGeometryAttributeAdder);

      OracleBlobAttributeAdder blobAdder = new OracleBlobAttributeAdder();
      addAttributeAdder("BLOB", blobAdder);

      OracleClobAttributeAdder clobAdder = new OracleClobAttributeAdder();
      addAttributeAdder("CLOB", clobAdder);
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
            objectIdAttribute, connection, metaData.getPath());
          if (newObjectIdAttribute != null) {
            metaData.replaceAttribute(objectIdAttribute, newObjectIdAttribute);
          }
        } finally {
          releaseConnection(connection);
        }
      }
    }
  }

}
