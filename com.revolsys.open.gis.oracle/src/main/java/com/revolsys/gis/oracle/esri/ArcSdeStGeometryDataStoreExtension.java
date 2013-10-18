package com.revolsys.gis.oracle.esri;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreExtension;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.oracle.io.OracleDataObjectStore;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;

public class ArcSdeStGeometryDataStoreExtension implements
  DataObjectStoreExtension {

  public ArcSdeStGeometryDataStoreExtension() {
  }

  @Override
  public void initialize(final DataObjectStore dataStore, Map<String,Object> connectionProperties) {
    final OracleDataObjectStore oracleDataStore = (OracleDataObjectStore)dataStore;
    final JdbcAttributeAdder stGeometryAttributeAdder = new ArcSdeStGeometryAttributeAdder(
      oracleDataStore);
    oracleDataStore.addAttributeAdder("ST_GEOMETRY", stGeometryAttributeAdder);
    oracleDataStore.addAttributeAdder("SDE.ST_GEOMETRY",
      stGeometryAttributeAdder);
  }

  private void initializeColumnProperties(final DataObjectStoreSchema schema,
    final String schemaName, final Connection connection,
    final Map<String, Map<String, Map<String, Object>>> esriColumnProperties)
    throws SQLException {

    final String sql = "SELECT GC.F_TABLE_NAME, GC.F_GEOMETRY_COLUMN, GC.SRID, GC.GEOMETRY_TYPE, GC.COORD_DIMENSION, SG.GEOMETRY_TYPE GEOMETRY_DATA_TYPE FROM SDE.GEOMETRY_COLUMNS GC LEFT OUTER JOIN SDE.ST_GEOMETRY_COLUMNS SG ON GC.F_TABLE_SCHEMA = SG.OWNER AND GC.F_TABLE_NAME = SG.TABLE_NAME WHERE GC.F_TABLE_SCHEMA = ?";
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      statement.setString(1, schemaName);
      final ResultSet resultSet = statement.executeQuery();
      try {
        while (resultSet.next()) {
          final String tableName = resultSet.getString(1);
          final String columnName = resultSet.getString(2);

          final int esriSrid = resultSet.getInt(3);
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeConstants.ESRI_SRID_PROPERTY, esriSrid);

          final ArcSdeSpatialReference spatialReference = ArcSdeSpatialReferenceCache.getSpatialReference(
            schema, esriSrid);
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeConstants.SPATIAL_REFERENCE, spatialReference);

          final int geometryType = resultSet.getInt(4);
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeConstants.DATA_TYPE,
            ArcSdeConstants.getGeometryDataType(geometryType));

          final int numAxis = resultSet.getInt(5);
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeConstants.NUM_AXIS, numAxis);

          String geometryColumnType = resultSet.getString(6);
          if (!StringUtils.hasText(geometryColumnType)) {
            geometryColumnType = ArcSdeConstants.SDEBINARY;
          }
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeConstants.GEOMETRY_COLUMN_TYPE,
            geometryColumnType);
        }
      } finally {
        JdbcUtils.close(resultSet);
      }
    } finally {
      JdbcUtils.close(statement);
    }
  }

  @Override
  public boolean isEnabled(final DataObjectStore dataStore) {
    return ArcSdeConstants.isSdeAvailable(dataStore);
  }

  @Override
  public void postProcess(final DataObjectStoreSchema schema) {
    final AbstractJdbcDataObjectStore dataStore = (AbstractJdbcDataObjectStore)schema.getDataStore();
    for (final DataObjectMetaData metaData : schema.getTypes()) {
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute instanceof ArcSdeStGeometryAttribute) {
        ArcSdeConstants.addObjectIdAttribute(dataStore, metaData);
      }
    }
  }

  @Override
  public void preProcess(final DataObjectStoreSchema schema) {
    final DataObjectStore dataStore = schema.getDataStore();
    final OracleDataObjectStore oracleDataStore = (OracleDataObjectStore)dataStore;
    Map<String, Map<String, Map<String, Object>>> esriColumnProperties = schema.getProperty(ArcSdeConstants.ESRI_SCHEMA_PROPERTY);
    if (esriColumnProperties == null) {
      esriColumnProperties = new HashMap<String, Map<String, Map<String, Object>>>();
      schema.setProperty(ArcSdeConstants.ESRI_SCHEMA_PROPERTY,
        esriColumnProperties);
    }
    try {
      final Connection connection = oracleDataStore.getSqlConnection();
      try {
        final String schemaName = oracleDataStore.getDatabaseSchemaName(schema);
        initializeColumnProperties(schema, schemaName, connection,
          esriColumnProperties);
      } finally {
        oracleDataStore.releaseSqlConnection(connection);
      }
    } catch (final SQLException e) {
      LoggerFactory.getLogger(getClass()).error(
        "Unable to get ArcSDE metadata for schema " + schema.getName(), e);
    }
  }

  private void setColumnProperty(
    final Map<String, Map<String, Map<String, Object>>> esriColumnProperties,
    final String schemaName, final String tableName, final String columnName,
    final String propertyName, final Object propertyValue) {
    final String typePath = PathUtil.toPath(schemaName, tableName);
    Map<String, Map<String, Object>> typeColumnMap = esriColumnProperties.get(typePath);
    if (typeColumnMap == null) {
      typeColumnMap = new HashMap<String, Map<String, Object>>();
      esriColumnProperties.put(typePath, typeColumnMap);
    }
    Map<String, Object> columnProperties = typeColumnMap.get(columnName);
    if (columnProperties == null) {
      columnProperties = new HashMap<String, Object>();
      typeColumnMap.put(columnName, columnProperties);
    }
    columnProperties.put(propertyName, propertyValue);
  }

}
