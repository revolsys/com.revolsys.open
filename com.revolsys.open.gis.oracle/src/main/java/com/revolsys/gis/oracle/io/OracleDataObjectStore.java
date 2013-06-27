package com.revolsys.gis.oracle.io;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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
import com.revolsys.gis.data.query.SqlCondition;
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

  public static final List<String> ORACLE_INTERNAL_SCHEMAS = Arrays.asList(
    "ANONYMOUS", "APEX_030200", "AURORA$JIS$UTILITY$",
    "AURORA$ORB$UNAUTHENTICATED", "AWR_STAGE", "CSMIG", "CTXSYS", "DBSNMP",
    "DEMO", "DIP", "DMSYS", "DSSYS", "EXFSYS", "LBACSYS", "MDSYS", "OLAPSYS",
    "ORACLE_OCM", "ORDDATA", "ORDPLUGINS", "ORDSYS", "OSE$HTTP$ADMIN", "OUTLN",
    "PERFSTAT", "SDE", "SYS", "SYSTEM", "TRACESVR", "TSMSYS", "WMSYS", "XDB");

  private boolean useSchemaSequencePrefix = true;

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

  public OracleDataObjectStore(final DataSource dataSource) {
    super(dataSource);
    setExcludeTablePatterns(".*\\$");
    setSqlPrefix("BEGIN ");
    setSqlSuffix(";END;");
  }

  public OracleDataObjectStore(final OracleDatabaseFactory databaseFactory,
    final Map<String, ? extends Object> connectionProperties) {
    super(databaseFactory);
    setExcludeTablePatterns(".*\\$");
    final DataSource dataSource = databaseFactory.createDataSource(connectionProperties);
    setDataSource(dataSource);
    setSqlPrefix("BEGIN ");
    setSqlSuffix(";END;");
  }

  protected Query addBoundingBoxFilter(Query query) {
    final BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      final String typePath = query.getTypeName();
      final DataObjectMetaData metaData = getMetaData(typePath);
      if (metaData == null) {
        throw new IllegalArgumentException("Unable to  find table " + typePath);
      } else {
        query = query.clone();
        final Attribute geometryAttribute = metaData.getGeometryAttribute();
        final String geometryColumnName = geometryAttribute.getName();
        final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);

        final BoundingBox projectedBoundingBox = boundingBox.convert(geometryFactory);

        final double x1 = projectedBoundingBox.getMinX();
        final double y1 = projectedBoundingBox.getMinY();
        final double x2 = projectedBoundingBox.getMaxX();
        final double y2 = projectedBoundingBox.getMaxY();

        if (geometryAttribute instanceof OracleSdoGeometryJdbcAttribute) {
          final String where = " SDO_RELATE("
            + geometryColumnName
            + ","
            + "MDSYS.SDO_GEOMETRY(2003,?,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)),'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'";
          query.and(new SqlCondition(where, geometryFactory.getSRID(), x1, y1,
            x2, y2));
        } else if (geometryAttribute instanceof ArcSdeOracleStGeometryJdbcAttribute) {
          final String where = " SDE.ST_ENVINTERSECTS(" + geometryColumnName
            + ", ?, ?, ?, ?) = 1";
          query.and(new SqlCondition(where, x1, y1, x2, y2));
        } else {
          throw new IllegalArgumentException("Unbown geometry attribute :"
            + geometryAttribute);
        }
      }
    }
    return query;
  }

  @Override
  public AbstractIterator<DataObject> createIterator(final Query query,
    final Map<String, Object> properties) {
    return new OracleJdbcQueryIterator(this, query, properties);
  }

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    return sequenceName + ".NEXTVAL";
  }

  @Override
  public Object getNextPrimaryKey(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    return getNextPrimaryKey(sequenceName);
  }

  @Override
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
  public int getRowCount(final Query query) {
    final Query bboxQuery = addBoundingBoxFilter(query);
    if (bboxQuery != query) {
      query.setAttributeNames("count(*))");
    }
    return super.getRowCount(query);
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    if (metaData == null) {
      return null;
    } else {
      final String typePath = metaData.getPath();
      final String schema = getDatabaseSchemaName(PathUtil.getPath(typePath));
      final String shortName = ShortNameProperty.getShortName(metaData);
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

      final OracleBlobAttributeAdder blobAdder = new OracleBlobAttributeAdder();
      addAttributeAdder("BLOB", blobAdder);

      final OracleClobAttributeAdder clobAdder = new OracleClobAttributeAdder();
      addAttributeAdder("CLOB", clobAdder);
      setPermissionsSql("select distinct owner \"SCHEMA\", table_name, privilege "
        + "from ALL_TAB_PRIVS_RECD P "
        + "where privilege in ('SELECT', 'INSERT', 'UPDATE', 'DELETE') AND ( "
        + "EXISTS (SELECT * FROM ALL_VIEWS V WHERE V.OWNER = P.OWNER AND V.VIEW_NAME = P.TABLE_NAME) OR "
        + "EXISTS (SELECT * FROM ALL_TABLES T WHERE T.OWNER = P.OWNER AND T.TABLE_NAME = P.TABLE_NAME)   ) "
        + " order by owner, table_name, privilege");
    }
  }

  @Override
  public boolean isSchemaExcluded(final String schemaName) {
    return ORACLE_INTERNAL_SCHEMAS.contains(schemaName);
  }

  public boolean isUseSchemaSequencePrefix() {
    return useSchemaSequencePrefix;
  }

  @Override
  public ResultPager<DataObject> page(final Query query) {
    return new OracleJdbcQueryResultPager(this, getProperties(), query);
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

  public void setUseSchemaSequencePrefix(final boolean useSchemaSequencePrefix) {
    this.useSchemaSequencePrefix = useSchemaSequencePrefix;
  }

}
