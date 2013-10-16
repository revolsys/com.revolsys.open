package com.revolsys.gis.oracle.esri;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcConstants;
import com.revolsys.jdbc.io.SqlFunction;

public class StGeometryAttributeAdder extends JdbcAttributeAdder {
  private static final Logger LOG = LoggerFactory.getLogger(StGeometryAttributeAdder.class);

  private final boolean available = true;

  private final AbstractJdbcDataObjectStore dataStore;

  public StGeometryAttributeAdder(final AbstractJdbcDataObjectStore dataStore) {
    this.dataStore = dataStore;

  }

  @Override
  public Attribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String name, final String dataTypeName, final int sqlType,
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
        columnName, ArcSdeOracleStGeometryJdbcAttribute.NUM_AXIS);
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

      final SpatialReference spatialReference = getColumnProperty(schema,
        typePath, columnName,
        ArcSdeOracleStGeometryJdbcAttribute.SPATIAL_REFERENCE);

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
        final GeometryFactory geometryFactory = GeometryFactory.getFactory(
          srid, scaleXy, scaleZ);
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

}
