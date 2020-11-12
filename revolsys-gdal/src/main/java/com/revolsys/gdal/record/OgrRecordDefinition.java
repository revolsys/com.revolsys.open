package com.revolsys.gdal.record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.GeomFieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.osr.SpatialReference;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.gdal.Gdal;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.util.Property;

public class OgrRecordDefinition extends RecordDefinitionImpl {
  public static OgrRecordDefinition newRecordDefinition(final OgrRecordStore recordStore,
    final RecordStoreSchema schema, final Layer layer) {
    final String layerName = layer.GetName();
    final PathName typePath = PathName.newPathName(layerName);
    return new OgrRecordDefinition(recordStore, schema, layer, typePath);
  }

  private final List<String> columnNames = new ArrayList<>();

  public OgrRecordDefinition(final OgrRecordStore recordStore, final RecordStoreSchema schema,
    final Layer layer, final PathName typePath) {
    super(schema, typePath);

    final String driverName = recordStore.getDriverName();
    /** This primes the layer so that the fidColumn is loaded correctly. */
    layer.GetNextFeature();

    String idFieldName = layer.GetFIDColumn();
    if (!Property.hasValue(idFieldName)) {
      idFieldName = "rowid";
    }
    final FeatureDefn layerDefinition = layer.GetLayerDefn();
    if (OgrRecordStore.SQLITE.equals(driverName) || OgrRecordStore.GEO_PAKCAGE.equals(driverName)) {
      addField(idFieldName, DataTypes.LONG, true);
      setIdFieldName(idFieldName);
    }
    for (int fieldIndex = 0; fieldIndex < layerDefinition.GetFieldCount(); fieldIndex++) {
      final FieldDefn fieldDefinition = layerDefinition.GetFieldDefn(fieldIndex);
      final String fieldName = fieldDefinition.GetName();
      this.columnNames.add(fieldName);
      final int fieldType = fieldDefinition.GetFieldType();
      final int fieldWidth = fieldDefinition.GetWidth();
      final int fieldPrecision = fieldDefinition.GetPrecision();
      DataType fieldDataType;
      switch (fieldType) {
        case 0:
          fieldDataType = DataTypes.INT;
        break;
        case 2:
          fieldDataType = DataTypes.DOUBLE;
        break;
        case 4:
        case 6:
          fieldDataType = DataTypes.STRING;
        break;
        case 9:
          fieldDataType = DataTypes.SQL_DATE;
        break;
        case 11:
          fieldDataType = DataTypes.DATE_TIME;
        break;

        default:
          fieldDataType = DataTypes.STRING;
          final String fieldTypeName = fieldDefinition.GetFieldTypeName(fieldType);
          Logs.error(this, "Unsupported field type " + recordStore.getFile() + " " + fieldName
            + ": " + fieldTypeName);
        break;
      }
      final FieldDefinition field = new FieldDefinition(fieldName, fieldDataType, fieldWidth,
        fieldPrecision, false);
      addField(field);
    }
    for (int fieldIndex = 0; fieldIndex < layerDefinition.GetGeomFieldCount(); fieldIndex++) {
      final GeomFieldDefn fieldDefinition = layerDefinition.GetGeomFieldDefn(fieldIndex);
      final String fieldName = fieldDefinition.GetName();
      final int geometryFieldType = fieldDefinition.GetFieldType();
      DataType geometryFieldDataType;
      int axisCount = 2;
      switch (geometryFieldType) {
        case 1:
          geometryFieldDataType = GeometryDataTypes.POINT;
        break;
        case 2:
          geometryFieldDataType = GeometryDataTypes.LINE_STRING;
        break;
        case 3:
          geometryFieldDataType = GeometryDataTypes.POLYGON;
        break;
        case 4:
          geometryFieldDataType = GeometryDataTypes.MULTI_POINT;
        break;
        case 5:
          geometryFieldDataType = GeometryDataTypes.MULTI_LINE_STRING;
        break;
        case 6:
          geometryFieldDataType = GeometryDataTypes.MULTI_POLYGON;
        break;
        case 7:
          geometryFieldDataType = GeometryDataTypes.GEOMETRY_COLLECTION;
        break;
        case 101:
          geometryFieldDataType = GeometryDataTypes.LINEAR_RING;
        break;
        case 0x80000000 + 1:
          geometryFieldDataType = GeometryDataTypes.POINT;
          axisCount = 3;
        break;
        case 0x80000000 + 2:
          geometryFieldDataType = GeometryDataTypes.LINE_STRING;
          axisCount = 3;
        break;
        case 0x80000000 + 3:
          geometryFieldDataType = GeometryDataTypes.POLYGON;
          axisCount = 3;
        break;
        case 0x80000000 + 4:
          geometryFieldDataType = GeometryDataTypes.MULTI_POINT;
          axisCount = 3;
        break;
        case 0x80000000 + 5:
          geometryFieldDataType = GeometryDataTypes.MULTI_LINE_STRING;
          axisCount = 3;
        break;
        case 0x80000000 + 6:
          geometryFieldDataType = GeometryDataTypes.MULTI_POLYGON;
          axisCount = 3;
        break;
        case 0x80000000 + 7:
          geometryFieldDataType = GeometryDataTypes.GEOMETRY_COLLECTION;
          axisCount = 3;
        break;

        default:
          geometryFieldDataType = GeometryDataTypes.GEOMETRY;
        break;
      }
      final SpatialReference spatialReference = fieldDefinition.GetSpatialRef();
      final GeometryFactory geometryFactory = Gdal.getGeometryFactory(spatialReference, axisCount);
      final FieldDefinition field = new FieldDefinition(fieldName, geometryFieldDataType, false);
      field.setGeometryFactory(geometryFactory);
      addField(field);
    }
  }

  @Override
  public void appendSelectAll(final Query query, final Appendable sql) {
    try {
      boolean first = true;
      for (final String columnName : this.columnNames) {
        if (first) {
          first = false;
        } else {
          sql.append(", ");
        }
        sql.append(columnName);
      }
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }
}
