package com.revolsys.gis.oracle.io;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;

public class OracleJdbcQueryIterator extends JdbcQueryIterator {

  public OracleJdbcQueryIterator(JdbcDataObjectStore dataStore, Query query,
    Map<String, Object> properties) {
    super(dataStore, query, properties);
  }

  @Override
  protected String getSql(Query query) {
    BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      query = query.clone();
      final String typePath = query.getTypeName();
      final DataObjectMetaData metaData = getMetaData();
      if (metaData == null) {
        throw new IllegalArgumentException("Unable to  find table " + typePath);
      } else {
        final Attribute geometryAttribute = metaData.getGeometryAttribute();
        final String geometryColumnName = geometryAttribute.getName();
        GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);

        final BoundingBox projectedBoundingBox = boundingBox.convert(geometryFactory);

        final double x1 = projectedBoundingBox.getMinX();
        final double y1 = projectedBoundingBox.getMinY();
        final double x2 = projectedBoundingBox.getMaxX();
        final double y2 = projectedBoundingBox.getMaxY();

        String whereClause = query.getWhereClause();
        final StringBuffer where = new StringBuffer();
        if (StringUtils.hasText(whereClause)) {
          where.append("(");
          where.append(whereClause);
          where.append(") AND ");
        }
        where.append(" SDO_RELATE(");
        where.append(geometryColumnName);
        where.append(",");
        where.append("MDSYS.SDO_GEOMETRY(2003,?,NULL,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?))");
        where.append(",'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'");
        query.setWhereClause(where.toString());
        query.addParameters(geometryFactory.getSRID(), x1, y1, x2, y2);
      }
      setQuery(query);
    }
    String sql = super.getSql(query);

    int offset = query.getOffset();
    int limit = query.getLimit();
    if (offset < 1 || limit < 0) {
      return sql;
    }
    sql = "SELECT * FROM (SELECT V.*,ROWNUM \"ROWN\" FROM (" + sql
      + ") V ) WHERE ROWN ";
    int startRowNum = offset + 1;
    int endRowNum = offset + limit;
    if (offset > 0) {
      if (limit < 0) {
        return sql + " >= " + startRowNum;
      } else {
        return sql + " BETWEEN " + startRowNum + " AND " + endRowNum;
      }
    } else {
      return sql + " <= " + endRowNum;
    }
  }

}
