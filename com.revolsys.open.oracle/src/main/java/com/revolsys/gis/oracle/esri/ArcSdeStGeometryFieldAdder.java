package com.revolsys.gis.oracle.esri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.types.DataType;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jts.geom.GeometryFactory;

public class ArcSdeStGeometryFieldAdder extends JdbcFieldAdder {
  private static final Logger LOG = LoggerFactory.getLogger(ArcSdeStGeometryFieldAdder.class);

  private final AbstractJdbcRecordStore recordStore;

  public ArcSdeStGeometryFieldAdder(
    final AbstractJdbcRecordStore recordStore) {
    this.recordStore = recordStore;

  }

  @Override
  public FieldDefinition addField(final RecordDefinitionImpl recordDefinition,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final RecordStoreSchema schema = recordDefinition.getSchema();
    final String typePath = recordDefinition.getPath();
    final String owner = this.recordStore.getDatabaseSchemaName(schema);
    final String tableName = this.recordStore.getDatabaseTableName(typePath);
    final String columnName = name.toUpperCase();
    final int esriSrid = JdbcFieldAdder.getIntegerColumnProperty(schema,
      typePath, columnName, ArcSdeConstants.ESRI_SRID_PROPERTY);
    if (esriSrid == -1) {
      LOG.error("Column not registered in SDE.ST_GEOMETRY table " + owner + "."
          + tableName + "." + name);
    }
    final int axisCount = JdbcFieldAdder.getIntegerColumnProperty(schema,
      typePath, columnName, JdbcFieldAdder.NUM_AXIS);
    if (axisCount == -1) {
      LOG.error("Column not found in SDE.GEOMETRY_COLUMNS table " + owner + "."
          + tableName + "." + name);
    }
    final DataType dataType = JdbcFieldAdder.getColumnProperty(schema,
      typePath, columnName, JdbcFieldAdder.GEOMETRY_TYPE);
    if (dataType == null) {
      LOG.error("Column not found in SDE.GEOMETRY_COLUMNS table " + owner + "."
          + tableName + "." + name);
    }

    final ArcSdeSpatialReference spatialReference = JdbcFieldAdder.getColumnProperty(
      schema, typePath, columnName, ArcSdeConstants.SPATIAL_REFERENCE);

    final GeometryFactory geometryFactory = JdbcFieldAdder.getColumnProperty(
      schema, typePath, columnName, JdbcFieldAdder.GEOMETRY_FACTORY);

    final FieldDefinition attribute = new ArcSdeStGeometryFieldDefinition(dbName, name,
      dataType, required, description, null, spatialReference, axisCount);

    recordDefinition.addField(attribute);
    attribute.setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
    return attribute;
  }

}
