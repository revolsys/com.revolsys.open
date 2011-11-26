package com.revolsys.gis.oracle.esri;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.jdbc.JdbcUtils;

public class SpatialReferenceCache {

  private Connection connection;

  private DataSource dataSource;

  private final Map<Integer, SpatialReference> spatialReferences = new HashMap<Integer, SpatialReference>();

  public SpatialReferenceCache() {
  }

  public SpatialReferenceCache(final Connection connection) {
    setConnection(connection);
  }

  public SpatialReferenceCache(final Connection connection,
    final DataSource dataSource) {
    setConnection(connection);
    setDataSource(dataSource);
  }

  public SpatialReferenceCache(final DataSource dataSource) {
    setDataSource(dataSource);
  }

  public Connection getConnection() {
    return connection;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public SpatialReference getSpatialReference(final int esriSrid) {
    SpatialReference spatialReference = spatialReferences.get(esriSrid);
    if (spatialReference == null) {
      final String sql = "SELECT SRID, SR_NAME, X_OFFSET, Y_OFFSET, Z_OFFSET, M_OFFSET, XYUNITS, Z_SCALE, M_SCALE, CS_ID, DEFINITION FROM SDE.ST_SPATIAL_REFERENCES WHERE SRID = ?";
      try {
        Connection connection = this.connection;
        if (dataSource != null) {
          connection = JdbcUtils.getConnection(dataSource);
        }
        try {
          PreparedStatement statement = null;
          ResultSet resultSet = null;
          try {

            statement = connection.prepareStatement(sql);
            statement.setInt(1, esriSrid);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
              final int srid = resultSet.getInt(10);
              final BigDecimal scale = resultSet.getBigDecimal(7);
              final BigDecimal zScale = resultSet.getBigDecimal(8);
              final GeometryFactory geometryFactory = GeometryFactory.getFactory(
                srid, scale.intValue(), zScale.intValue());
              spatialReference = new SpatialReference(geometryFactory);
              spatialReference.setEsriSrid(esriSrid);
              spatialReference.setName(resultSet.getString(2));
              spatialReference.setXOffset(resultSet.getBigDecimal(3));
              spatialReference.setYOffset(resultSet.getBigDecimal(4));
              spatialReference.setZOffset(resultSet.getBigDecimal(5));
              spatialReference.setMOffset(resultSet.getBigDecimal(6));
              spatialReference.setXyScale(scale);
              spatialReference.setZScale(zScale);
              spatialReference.setMScale(resultSet.getBigDecimal(9));
              spatialReference.setSrid(srid);
              spatialReference.setCsWkt(resultSet.getString(11));
              spatialReferences.put(esriSrid, spatialReference);
            }
          } finally {
            JdbcUtils.close(statement, resultSet);
          }
        } finally {
          if (this.dataSource != null) {
            JdbcUtils.close(connection);
          }
        }
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to get srid " + esriSrid, e);
      }
    }
    return spatialReference;
  }

  public void setConnection(final Connection connection) {
    this.connection = connection;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
