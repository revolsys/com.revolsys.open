package com.revolsys.gis.oracle.esri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.types.DataType;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jts.geom.GeometryFactory;

public class ArcSdeStGeometryAttributeAdder extends JdbcAttributeAdder {
  private static final Logger LOG = LoggerFactory.getLogger(ArcSdeStGeometryAttributeAdder.class);

  private final AbstractJdbcRecordStore recordStore;

  public ArcSdeStGeometryAttributeAdder(
    final AbstractJdbcRecordStore recordStore) {
    this.recordStore = recordStore;

  }

  @Override
  public Attribute addAttribute(final RecordDefinitionImpl recordDefinition,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final RecordStoreSchema schema = recordDefinition.getSchema();
    final String typePath = recordDefinition.getPath();
    final String owner = this.recordStore.getDatabaseSchemaName(schema);
    final String tableName = this.recordStore.getDatabaseTableName(typePath);
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

    recordDefinition.addAttribute(attribute);
    attribute.setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
    return attribute;
  }

}
