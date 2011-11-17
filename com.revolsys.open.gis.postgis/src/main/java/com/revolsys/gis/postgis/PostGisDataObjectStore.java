package com.revolsys.gis.postgis;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.postgresql.geometric.PGbox;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.Query;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;

public class PostGisDataObjectStore extends AbstractJdbcDataObjectStore {

  public PostGisDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public PostGisDataObjectStore(final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  public PostGisDataObjectStore(final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this(dataObjectFactory);
    setDataSource(dataSource);
  }

  @Override
  public String getGeneratePrimaryKeySql(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    return "nextval('" + sequenceName + "')";
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    final String schema = getDatabaseSchemaName(typeName.getNamespaceURI());
    final String tableName = getDatabaseTableName(typeName);
    final String idAttributeName = metaData.getIdAttributeName().toLowerCase();
    final String sequenceName = schema + "." + tableName + "_"
      + idAttributeName + "_seq";
    return sequenceName;
  }

  public Object getNextPrimaryKey(final DataObjectMetaData metaData) {
    final String shortName = ShortNameProperty.getShortName(metaData);
    String sequenceName;
    if (shortName == null) {
      sequenceName = getSequenceName(metaData);
    } else {
      sequenceName = shortName + "_SEQ";
    }
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
      this, getDataSource());
    addAttributeAdder("geometry", geometryAttributeAdder);
  }

  public Query createBoundingBoxQuery(final Query query,
    final BoundingBox boundingBox) {
    Query boundingBoxQuery = query.clone();
    final QName typeName = boundingBoxQuery.getTypeName();
    DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Unable to  find table " + typeName);
    } else {
      final double x1 = boundingBox.getMinX();
      final double y1 = boundingBox.getMinY();
      final double x2 = boundingBox.getMaxX();
      final double y2 = boundingBox.getMaxY();
      String whereClause = boundingBoxQuery.getWhereClause();
      final String geometryAttributeName = metaData.getGeometryAttributeName();
      if (StringUtils.hasText(whereClause)) {
        whereClause = "(" + whereClause + ") AND " +
        		geometryAttributeName +
        		" && ?";
      } else {
        whereClause = geometryAttributeName +
        		" && ?";
      }
      boundingBoxQuery.setWhereClause(whereClause);
      final PGbox box = new PGbox(x1, y1, x2, y2);
      boundingBoxQuery.addParameter(box);
      return boundingBoxQuery;
    }
  }
}
