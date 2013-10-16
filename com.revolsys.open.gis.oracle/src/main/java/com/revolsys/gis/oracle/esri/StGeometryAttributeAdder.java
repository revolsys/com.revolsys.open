package com.revolsys.gis.oracle.esri;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.PathUtil;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcConstants;
import com.revolsys.jdbc.io.SqlFunction;

public class StGeometryAttributeAdder extends JdbcAttributeAdder {
  private static final Logger LOG = LoggerFactory.getLogger(StGeometryAttributeAdder.class);

  private Connection connection;

  private DataSource dataSource;

  private final SpatialReferenceCache spatialReferences;

  private boolean available = true;

  private AbstractJdbcDataObjectStore dataStore;

  public StGeometryAttributeAdder(AbstractJdbcDataObjectStore dataStore,
    final DataSource dataSource, final Connection connection) {
    this.dataStore = dataStore;
    this.dataSource = dataSource;
    this.connection = connection;
    spatialReferences = new SpatialReferenceCache(connection, dataSource);
  }

  @Override
  public Attribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String name, String dataTypeName, final int sqlType,
    final int length, final int scale, final boolean required) {
    if (available) {
      final DataObjectStoreSchema schema = metaData.getSchema();
      final String typePath = metaData.getPath();
      final String owner = dataStore.getDatabaseSchemaName(schema);
      final String tableName = dataStore.getDatabaseTableName(typePath);
      final String columnName = name.toUpperCase();
      final int esriSrid = getIntegerColumnProperty(schema, typePath,
        columnName, ArcSdeOracleStGeometryJdbcAttribute.ESRI_SRID_PROPERTY);
      if (esriSrid == -1) {
        LOG.error("Column not registered in SDE.ST_GEOMETRY table " + owner
          + "." + tableName + "." + name);
      }
      final int numAxis = getIntegerColumnProperty(schema, typePath,
        columnName,
        ArcSdeOracleStGeometryJdbcAttribute.NUM_AXIS);
      if (numAxis == -1) {
        LOG.error("Column not found in SDE.GEOMETRY_COLUMNS table " + owner
          + "." + tableName + "." + name);
      }
      final DataType dataType = getColumnProperty(schema, typePath, columnName,
        ArcSdeOracleStGeometryJdbcAttribute.DATA_TYPE);
      if (dataType == null) {
        LOG.error("Column not found in SDE.GEOMETRY_COLUMNS table " + owner
          + "." + tableName + "." + name);
      }

      final SpatialReference spatialReference = spatialReferences.getSpatialReference(esriSrid);

      final Attribute attribute = new ArcSdeOracleStGeometryJdbcAttribute(name,
        dataType, required, null, spatialReference, numAxis);

      metaData.addAttribute(attribute);
      attribute.setProperty(JdbcConstants.FUNCTION_INTERSECTS, new SqlFunction(
        "SDE.ST_ENVINTERSECTS(", ") = 1"));
      attribute.setProperty(JdbcConstants.FUNCTION_BUFFER, new SqlFunction(
        "SDE.ST_BUFFER(", ")"));
      if (spatialReference != null) {
        final int srid = spatialReference.getSrid();
        final Double scaleXy = spatialReference.getXyScale();
        final Double scaleZ = spatialReference.getZScale();
        GeometryFactory geometryFactory = GeometryFactory.getFactory(srid,
          scaleXy, scaleZ);
        attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
          geometryFactory);
      }
      return attribute;
    } else {
      throw new IllegalStateException(
        "SDE is not installed or available from this user account");
    }
  }

  @SuppressWarnings("unchecked")
private <T> T getColumnProperty(final DataObjectStoreSchema schema,
    final String typePath, final String columnName, final String propertyName) {
    final Map<String, Map<String, Map<String, Object>>> esriColumnProperties = schema.getProperty(ArcSdeOracleStGeometryJdbcAttribute.ESRI_SCHEMA_PROPERTY);
    final Map<String, Map<String, Object>> columnsProperties = esriColumnProperties.get(typePath);
    if (columnsProperties != null) {
      final Map<String, Object> properties = columnsProperties.get(columnName);
      if (properties != null) {
        final Object value = properties.get(propertyName);
        return (T)value;
      }
    }
    return null;
  }

  private int getIntegerColumnProperty(final DataObjectStoreSchema schema,
    final String typePath, final String columnName, final String propertyName) {
    final Object value = getColumnProperty(schema, typePath, columnName,
      propertyName);
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      return -1;
    }
  }

  @Override
  public void initialize(final DataObjectStoreSchema schema) {
    if (available) {
      Map<String, Map<String, Map<String, Object>>> esriColumnProperties = schema.getProperty(ArcSdeOracleStGeometryJdbcAttribute.ESRI_SCHEMA_PROPERTY);
      if (esriColumnProperties == null) {
        esriColumnProperties = new HashMap<String, Map<String, Map<String, Object>>>();
        schema.setProperty(
          ArcSdeOracleStGeometryJdbcAttribute.ESRI_SCHEMA_PROPERTY,
          esriColumnProperties);
      }
      try {
        Connection connection;
        if (dataSource == null) {
          connection = this.connection;
        } else {
          connection = JdbcUtils.getConnection(dataSource);
        }
        try {
          initializeColumnProperties(schema, connection, esriColumnProperties);
        } finally {
          if (dataSource != null) {
            JdbcUtils.release(connection, dataSource);
          }
        }
      } catch (final SQLException e) {
        available = false;
      }
    }
  }

  private void initializeColumnProperties(final DataObjectStoreSchema schema,
    final Connection connection,
    final Map<String, Map<String, Map<String, Object>>> esriColumnProperties)
    throws SQLException {
    final String schemaName = dataStore.getDatabaseSchemaName(schema);
    final String sql = "SELECT GC.F_TABLE_NAME, GC.F_GEOMETRY_COLUMN, GC.SRID, GC.GEOMETRY_TYPE, GC.COORD_DIMENSION, SG.GEOMETRY_TYPE GEOMETRY_DATA_TYPE FROM SDE.GEOMETRY_COLUMNS GC LEFT OUTER JOIN SDE.ST_GEOMETRY_COLUMNS SG ON GC.F_TABLE_SCHEMA = SG.OWNER AND GC.F_TABLE_NAME = SG.TABLE_NAME WHERE GC.F_TABLE_SCHEMA = ?";
    final PreparedStatement statement = connection.prepareStatement(sql);
    try {
      statement.setString(1, schemaName);
      final ResultSet resultSet = statement.executeQuery();
      try {
        while (resultSet.next()) {
          final String tableName = resultSet.getString(1);
          final String columnName = resultSet.getString(2);
          final int srid = resultSet.getInt(3);
          final int geometryType = resultSet.getInt(4);
         
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeOracleStGeometryJdbcAttribute.ESRI_SRID_PROPERTY,
            srid);
          
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeOracleStGeometryJdbcAttribute.DATA_TYPE,
            ArcSdeConstants.getGeometryDataType(geometryType));
          
          final int numAxis = resultSet.getInt(5);
           setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName,
            ArcSdeOracleStGeometryJdbcAttribute.NUM_AXIS,
            numAxis);
        
          String geometryColumnType = resultSet.getString(6);
          if (!StringUtils.hasText(geometryColumnType)) {
        	  geometryColumnType = "SDEBINARY";
          }  setColumnProperty(esriColumnProperties, schemaName, tableName,
                  columnName,
                  ArcSdeOracleStGeometryJdbcAttribute.GEOMETRY_COLUMN_TYPE,
                  geometryColumnType);
        }
      } finally {
        JdbcUtils.close(resultSet);
      }
    } finally {
      JdbcUtils.close(statement);
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
