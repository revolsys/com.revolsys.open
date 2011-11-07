package com.revolsys.gis.oracle.esri;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.gis.jdbc.io.JdbcConstants;
import com.revolsys.gis.jdbc.io.SqlFunction;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.jdbc.JdbcUtils;

public class StGeometryAttributeAdder extends JdbcAttributeAdder {
  private static final Logger LOG = LoggerFactory.getLogger(StGeometryAttributeAdder.class);

  private Connection connection;

  private DataSource dataSource;

  private final SpatialReferenceCache spatialReferences;

  private boolean available = true;

  public StGeometryAttributeAdder(
    final Connection connection) {
    this.connection = connection;
    spatialReferences = new SpatialReferenceCache(connection, dataSource);
  }

  public StGeometryAttributeAdder(
    final DataSource dataSource) {
    this.dataSource = dataSource;
    spatialReferences = new SpatialReferenceCache(connection, dataSource);
  }

  public StGeometryAttributeAdder(
    final DataSource dataSource,
    final Connection connection) {
    this.dataSource = dataSource;
    this.connection = connection;
    spatialReferences = new SpatialReferenceCache(connection, dataSource);
  }

  @Override
  public Attribute addAttribute(
    final DataObjectMetaDataImpl metaData,
    final String name,
    final int sqlType,
    final int length,
    final int scale,
    final boolean required) {
    if (available) {
      final QName typeName = metaData.getName();
      final String owner = typeName.getNamespaceURI().toUpperCase();
      final String tableName = typeName.getLocalPart().toUpperCase();
      final String columnName = name.toUpperCase();
      final DataObjectStoreSchema schema = metaData.getSchema();
      final int esriSrid = getIntegerColumnProperty(schema, typeName,
        columnName, ArcSdeOracleStGeometryJdbcAttribute.ESRI_SRID_PROPERTY);
      if (esriSrid == -1) {
        LOG.error("Column not registered in SDE.ST_GEOMETRY table " + owner
          + "." + tableName + "." + name);
      }
      final int dimension = getIntegerColumnProperty(schema, typeName,
        columnName,
        ArcSdeOracleStGeometryJdbcAttribute.COORDINATE_DIMENSION_PROPERTY);
      if (dimension == -1) {
        LOG.error("Column not found in SDE.GEOMETRY_COLUMNS table " + owner
          + "." + tableName + "." + name);
      }
      final DataType dataType = getColumnProperty(schema, typeName, columnName,
        ArcSdeOracleStGeometryJdbcAttribute.DATA_TYPE);
      if (dataType == null) {
        LOG.error("Column not found in SDE.GEOMETRY_COLUMNS table " + owner
          + "." + tableName + "." + name);
      }

      final SpatialReference spatialReference = spatialReferences.getSpatialReference(esriSrid);

      final Attribute attribute = new ArcSdeOracleStGeometryJdbcAttribute(name,
        dataType, length, scale, required, null, spatialReference, dimension);

      metaData.addAttribute(attribute);
      attribute.setProperty(JdbcConstants.FUNCTION_INTERSECTS, new SqlFunction(
        "SDE.ST_ENVINTERSECTS(", ") = 1"));
      attribute.setProperty(JdbcConstants.FUNCTION_BUFFER, new SqlFunction(
        "SDE.ST_BUFFER(", ")"));
      if (spatialReference != null) {
        final int srid = spatialReference.getSrid();
        final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(srid);
        final Double scaleXy = spatialReference.getXyScale();
        final Double scaleZ = spatialReference.getZScale();
        GeometryFactory geometryFactory = new GeometryFactory(coordinateSystem,
          new SimpleCoordinatesPrecisionModel(scaleXy, scaleZ));
        attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
          geometryFactory);
      }
      return attribute;
    } else {
      throw new IllegalStateException(
        "SDE is not installed or available from this user account");
    }
  }

  private <T> T getColumnProperty(
    final DataObjectStoreSchema schema,
    final QName typeName,
    final String columnName,
    final String propertyName) {
    final Map<QName, Map<String, Map<String, Object>>> esriColumnProperties = schema.getProperty(ArcSdeOracleStGeometryJdbcAttribute.ESRI_SCHEMA_PROPERTY);
    final Map<String, Map<String, Object>> columnsProperties = esriColumnProperties.get(typeName);
    if (columnsProperties != null) {
      final Map<String, Object> properties = columnsProperties.get(columnName);
      if (properties != null) {
        final Object value = properties.get(propertyName);
        return (T)value;
      }
    }
    return null;
  }

  private int getIntegerColumnProperty(
    final DataObjectStoreSchema schema,
    final QName typeName,
    final String columnName,
    final String propertyName) {
    final Object value = getColumnProperty(schema, typeName, columnName,
      propertyName);
    if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      return -1;
    }
  }

  @Override
  public void initialize(
    final DataObjectStoreSchema schema) {
    if (available) {
      Map<QName, Map<String, Map<String, Object>>> esriColumnProperties = schema.getProperty(ArcSdeOracleStGeometryJdbcAttribute.ESRI_SCHEMA_PROPERTY);
      if (esriColumnProperties == null) {
        esriColumnProperties = new HashMap<QName, Map<String, Map<String, Object>>>();
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
            JdbcUtils.close(connection);
          }
        }
      } catch (final SQLException e) {
        available = false;
      }
    }
  }

  private void initializeColumnProperties(
    final DataObjectStoreSchema schema,
    final Connection connection,
    final Map<QName, Map<String, Map<String, Object>>> esriColumnProperties)
    throws SQLException {
    final String schemaName = schema.getName().toUpperCase();
    final String sql = "select SG.TABLE_NAME, SG.COLUMN_NAME, SG.SRID, GC.GEOMETRY_TYPE, GC.COORD_DIMENSION from SDE.ST_GEOMETRY_COLUMNS SG LEFT JOIN SDE.GEOMETRY_COLUMNS GC ON SG.OWNER = GC.F_TABLE_SCHEMA AND SG.TABLE_NAME = GC.F_TABLE_NAME where SG.OWNER = ?";
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
          final int numAxis = resultSet.getInt(5);
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeOracleStGeometryJdbcAttribute.ESRI_SRID_PROPERTY,
            srid);
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName,
            ArcSdeOracleStGeometryJdbcAttribute.COORDINATE_DIMENSION_PROPERTY,
            numAxis);
          setColumnProperty(esriColumnProperties, schemaName, tableName,
            columnName, ArcSdeOracleStGeometryJdbcAttribute.DATA_TYPE,
            ArcSdeConstants.getGeometryDataType(geometryType));
        }
      } finally {
        JdbcUtils.close(resultSet);
      }
    } finally {
      JdbcUtils.close(statement);
    }
  }

  private void setColumnProperty(
    final Map<QName, Map<String, Map<String, Object>>> esriColumnProperties,
    final String schemaName,
    final String tableName,
    final String columnName,
    final String propertyName,
    final Object propertyValue) {
    final QName typeName = new QName(schemaName, tableName);
    Map<String, Map<String, Object>> typeColumnMap = esriColumnProperties.get(typeName);
    if (typeColumnMap == null) {
      typeColumnMap = new HashMap<String, Map<String, Object>>();
      esriColumnProperties.put(typeName, typeColumnMap);
    }
    Map<String, Object> columnProperties = typeColumnMap.get(columnName);
    if (columnProperties == null) {
      columnProperties = new HashMap<String, Object>();
      typeColumnMap.put(columnName, columnProperties);
    }
    columnProperties.put(propertyName, propertyValue);
  }
}
