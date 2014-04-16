package com.revolsys.gis.spatialite.io;

import java.util.Map;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.jts.geom.BoundingBox;

public class SpatiaLiteJdbcQueryIterator extends JdbcQueryIterator {

  public SpatiaLiteJdbcQueryIterator(
    final AbstractJdbcDataObjectStore dataStore, final Query query,
    final Map<String, Object> properties) {
    super(dataStore, query, properties);
  }

  @Override
  protected String getSql(final Query query) {
    final BoundingBox boundingBox = query.getBoundingBox();
    if (boundingBox != null) {
      final String typePath = query.getTypeName();
      final DataObjectMetaData metaData = getMetaData();
      if (metaData == null) {
        throw new IllegalArgumentException("Unable to  find table " + typePath);
      } else {
        // query = query.clone();
        // final String geometryAttributeName =
        // metaData.getGeometryAttributeName();
        // final GeometryFactoryI geometryFactory =
        // metaData.getGeometryFactory();
        // boundingBox = boundingBox.convert(geometryFactory);
        // final double x1 = boundingBox.getMinX();
        // final double y1 = boundingBox.getMinY();
        // final double x2 = boundingBox.getMaxX();
        // final double y2 = boundingBox.getMaxY();
        //
        // final PGbox box = new PGbox(x1, y1, x2, y2);
        // query.and(new BinaryCondition(geometryAttributeName, "&&", box));
        // setQuery(query);
        // TODO BBOX query
      }
    }

    String sql = super.getSql(query);
    final int offset = query.getOffset();
    final int limit = query.getLimit();
    if (offset > 0) {
      sql += " OFFSET " + offset;
    }
    if (limit > -1) {
      sql += " LIMIT " + limit;
    }
    return sql;
  }

}
