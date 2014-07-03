package com.revolsys.gis.oracle.esri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.io.DataObjectStoreSchema;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcDataObjectStore;
import com.revolsys.jts.geom.GeometryFactory;

public class ArcSdeStGeometryAttributeAdder extends JdbcAttributeAdder {
  private static final Logger LOG = LoggerFactory.getLogger(ArcSdeStGeometryAttributeAdder.class);

  private final AbstractJdbcDataObjectStore dataStore;

  public ArcSdeStGeometryAttributeAdder(
    final AbstractJdbcDataObjectStore dataStore) {
    this.dataStore = dataStore;

  }

  @Override
  public Attribute addAttribute(final RecordDefinitionImpl metaData,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final DataObjectStoreSchema schema = metaData.getSchema();
    final String typePath = metaData.getPath();
    final String owner = this.dataStore.getDatabaseSchemaName(schema);
    final String tableName = this.dataStore.getDatabaseTableName(typePath);
    final String columnName = name.toUpperCase();
    final int esriSrid = JdbcAttributeAdder.getIntegerColumnProperty(schema,
      typePath, columnName, ArcSdeConstants.ESRI_SRID_PROPERTY);
    if (esriSrid == -1) {
      LOG.error("Column not registered in SDE.ST_GEOMETRY table " + owner + "."
        + tableName + "." + name);
    }
    final int axisCount = JdbcAttributeAdder.getIntegerColumnProperty(schema,
      typePath, columnName, JdbcAttributeAdder.NUM_AXIS);
    if (axisCount == -1) {
      LOG.error("Column not found in SDE.GEOMETRY_COLUMNS table " + owner + "."
        + tableName + "." + name);
    }
    final DataType dataType = JdbcAttributeAdder.getColumnProperty(schema,
      typePath, columnName, JdbcAttributeAdder.GEOMETRY_TYPE);
    if (dataType == null) {
      LOG.error("Column not found in SDE.GEOMETRY_COLUMNS table " + owner + "."
        + tableName + "." + name);
    }

    final ArcSdeSpatialReference spatialReference = JdbcAttributeAdder.getColumnProperty(
      schema, typePath, columnName, ArcSdeConstants.SPATIAL_REFERENCE);

    final GeometryFactory geometryFactory = JdbcAttributeAdder.getColumnProperty(
      schema, typePath, columnName, JdbcAttributeAdder.GEOMETRY_FACTORY);

    final Attribute attribute = new ArcSdeStGeometryAttribute(dbName, name,
      dataType, required, description, null, spatialReference, axisCount);

    metaData.addAttribute(attribute);
    attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
    return attribute;
  }

}
