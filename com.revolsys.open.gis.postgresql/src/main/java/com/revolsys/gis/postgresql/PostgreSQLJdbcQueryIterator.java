package com.revolsys.gis.postgresql;

import java.util.Map;

import org.postgresql.geometric.PGbox;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;

public class PostgreSQLJdbcQueryIterator extends JdbcQueryIterator {

  public PostgreSQLJdbcQueryIterator(JdbcDataObjectStore dataStore,
    Query query, Map<String, Object> properties) {
    super(dataStore, query, properties);
  }

  @Override
  protected String getSql(Query query) {
    BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      final String typePath = query.getTypeName();
      final DataObjectMetaData metaData = getMetaData();
      if (metaData == null) {
        throw new IllegalArgumentException("Unable to  find table " + typePath);
      } else {
        query = query.clone();
        String whereClause = query.getWhereClause();
        final String geometryAttributeName = metaData.getGeometryAttributeName();
        if (StringUtils.hasText(whereClause)) {
          whereClause = "(" + whereClause + ") AND " + geometryAttributeName
            + " && ?";
        } else {
          whereClause = geometryAttributeName + " && ?";
        }
        query.setWhereClause(whereClause);
        GeometryFactory geometryFactory = metaData.getGeometryFactory();
        boundingBox = boundingBox.convert(geometryFactory);
        final double x1 = boundingBox.getMinX();
        final double y1 = boundingBox.getMinY();
        final double x2 = boundingBox.getMaxX();
        final double y2 = boundingBox.getMaxY();

        final PGbox box = new PGbox(x1, y1, x2, y2);
        query.addParameter(box);
        setQuery(query);
      }
    }

    String sql = super.getSql(query);

    int offset = query.getOffset();
    int limit = query.getLimit();
    if (offset > 0) {
      sql += " OFFSET " + offset;
    }
    if (limit > -1) {
      sql += " LIMIT " + limit;
    }
    return sql;
  }

}
