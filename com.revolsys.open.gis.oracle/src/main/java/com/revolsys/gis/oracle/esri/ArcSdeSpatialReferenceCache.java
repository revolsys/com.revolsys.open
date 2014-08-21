package com.revolsys.gis.oracle.esri;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.gis.cs.WktCsParser;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;

public class ArcSdeSpatialReferenceCache {

  public static ArcSdeSpatialReferenceCache get(
    final AbstractJdbcRecordStore recordStore) {
    ArcSdeSpatialReferenceCache spatialReferences = recordStore.getProperty("esriSpatialReferences");
    if (spatialReferences == null) {
      spatialReferences = new ArcSdeSpatialReferenceCache(recordStore);
      recordStore.setProperty("esriSpatialReferences", spatialReferences);
    }
    return spatialReferences;
  }

  public static ArcSdeSpatialReferenceCache get(
    final RecordStoreSchema schema) {
    final AbstractJdbcRecordStore recordStore = (AbstractJdbcRecordStore)schema.getRecordStore();
    return get(recordStore);

  }

  public static ArcSdeSpatialReference getSpatialReference(
    final RecordStoreSchema schema, final int esriSrid) {
    return get(schema).getSpatialReference(esriSrid);
  }

  private final Map<Integer, ArcSdeSpatialReference> spatialReferences = new HashMap<Integer, ArcSdeSpatialReference>();

  private AbstractJdbcRecordStore recordStore;

  public ArcSdeSpatialReferenceCache() {
  }

  public ArcSdeSpatialReferenceCache(final AbstractJdbcRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public synchronized ArcSdeSpatialReference getSpatialReference(
    final int esriSrid) {
    ArcSdeSpatialReference spatialReference = this.spatialReferences.get(esriSrid);
    if (spatialReference == null) {
      spatialReference = getSpatialReference(
        "SELECT SRID, SR_NAME, X_OFFSET, Y_OFFSET, Z_OFFSET, M_OFFSET, XYUNITS, Z_SCALE, M_SCALE, CS_ID, DEFINITION FROM SDE.ST_SPATIAL_REFERENCES WHERE SRID = ?",
        esriSrid);
      if (spatialReference == null) {
        spatialReference = getSpatialReference(
          "SELECT SRID, DESCRIPTION, FALSEX, FALSEY, FALSEZ, FALSEM, XYUNITS, ZUNITS, MUNITS, AUTH_SRID, SRTEXT FROM SDE.SPATIAL_REFERENCES WHERE SRID = ?",
          esriSrid);
      }
    }
    return spatialReference;
  }

  protected ArcSdeSpatialReference getSpatialReference(final String sql,
    final int esriSrid) {
    try {
      final Connection connection = this.recordStore.getSqlConnection();
      try {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {

          statement = connection.prepareStatement(sql);
          statement.setInt(1, esriSrid);
          resultSet = statement.executeQuery();
          if (resultSet.next()) {
            final String name = resultSet.getString(2);
            final BigDecimal xOffset = resultSet.getBigDecimal(3);
            final BigDecimal yOffset = resultSet.getBigDecimal(4);
            final BigDecimal zOffset = resultSet.getBigDecimal(5);
            final BigDecimal mOffset = resultSet.getBigDecimal(6);
            final BigDecimal scale = resultSet.getBigDecimal(7);
            final BigDecimal zScale = resultSet.getBigDecimal(8);
            final BigDecimal mScale = resultSet.getBigDecimal(9);
            int srid = resultSet.getInt(10);
            final String wkt = resultSet.getString(11);
            final GeometryFactory geometryFactory;
            if (srid <= 0) {
              final CoordinateSystem coordinateSystem = new WktCsParser(wkt).parse();
              final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
              srid = esriCoordinateSystem.getId();
              if (srid <= 0) {
                geometryFactory = GeometryFactory.fixed(coordinateSystem,
                  3, scale.doubleValue(), zScale.doubleValue());
              } else {
                geometryFactory = GeometryFactory.fixed(srid, 3,
                  scale.doubleValue(), zScale.doubleValue());
              }
            } else {
              geometryFactory = GeometryFactory.fixed(srid, 3,
                scale.doubleValue(), zScale.doubleValue());
            }

            final ArcSdeSpatialReference spatialReference = new ArcSdeSpatialReference(
              geometryFactory);
            spatialReference.setEsriSrid(esriSrid);
            spatialReference.setName(name);
            spatialReference.setXOffset(xOffset);
            spatialReference.setYOffset(yOffset);
            spatialReference.setZOffset(zOffset);
            spatialReference.setMOffset(mOffset);
            spatialReference.setXyScale(scale);
            spatialReference.setZScale(zScale);
            spatialReference.setMScale(mScale);
            spatialReference.setSrid(srid);
            spatialReference.setCsWkt(wkt);
            this.spatialReferences.put(esriSrid, spatialReference);
            return spatialReference;
          }
        } finally {
          JdbcUtils.close(statement, resultSet);
        }
      } finally {
        this.recordStore.releaseSqlConnection(connection);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to get srid " + esriSrid, e);
    }
    return null;
  }

}
