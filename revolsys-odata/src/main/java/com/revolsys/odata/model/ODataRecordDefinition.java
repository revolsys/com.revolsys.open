package com.revolsys.odata.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class ODataRecordDefinition extends RecordDefinitionImpl {

  private static Map<String, DataType> DATA_TYPE_MAP = new HashMap<>();

  static {
    DATA_TYPE_MAP.put("Edm.Boolean", DataTypes.BOOLEAN);
    DATA_TYPE_MAP.put("Edm.Byte", DataTypes.BYTE);
    DATA_TYPE_MAP.put("Edm.Int32", DataTypes.INT);
    DATA_TYPE_MAP.put("Edm.Int16", DataTypes.SHORT);
    DATA_TYPE_MAP.put("Edm.Int64", DataTypes.LONG);
    DATA_TYPE_MAP.put("Edm.Double", DataTypes.DOUBLE);
    DATA_TYPE_MAP.put("Edm.Decimal", DataTypes.DECIMAL);
    DATA_TYPE_MAP.put("Edm.String", DataTypes.STRING);
    DATA_TYPE_MAP.put("Edm.Date", DataTypes.TIMESTAMP);
    DATA_TYPE_MAP.put("Edm.Guid", DataTypes.UUID);

    DATA_TYPE_MAP.put("Edm.Geometry", GeometryDataTypes.GEOMETRY);
    DATA_TYPE_MAP.put("Edm.GeometryCollection", GeometryDataTypes.GEOMETRY_COLLECTION);
    DATA_TYPE_MAP.put("Edm.GeometryPoint", GeometryDataTypes.POINT);
    DATA_TYPE_MAP.put("Edm.GeometryMultiPoint", GeometryDataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put("Edm.GeometryLineString", GeometryDataTypes.LINE_STRING);
    DATA_TYPE_MAP.put("Edm.GeometryMultiLineString", GeometryDataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("Edm.GeometryPolygon", GeometryDataTypes.POLYGON);
    DATA_TYPE_MAP.put("Edm.GeometryMultiPolygon", GeometryDataTypes.MULTI_POLYGON);

    DATA_TYPE_MAP.put("Edm.Geography", GeometryDataTypes.GEOMETRY);
    DATA_TYPE_MAP.put("Edm.GeographyCollection", GeometryDataTypes.GEOMETRY_COLLECTION);
    DATA_TYPE_MAP.put("Edm.GeographyPoint", GeometryDataTypes.POINT);
    DATA_TYPE_MAP.put("Edm.GeographyMultiPoint", GeometryDataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put("Edm.GeographyLineString", GeometryDataTypes.LINE_STRING);
    DATA_TYPE_MAP.put("Edm.GeographyMultiLineString", GeometryDataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put("Edm.GeographyPolygon", GeometryDataTypes.POLYGON);
    DATA_TYPE_MAP.put("Edm.GeographyMultiPolygon", GeometryDataTypes.MULTI_POLYGON);

  }

  public ODataRecordDefinition(final ODataRecordStoreSchema schema, final PathName pathName,
    final JsonObject metadata, String entityTypeName) {
    super(schema, pathName);
    final int dotIndex = entityTypeName.lastIndexOf('.');
    final String entitySchemaName = entityTypeName.substring(0, dotIndex);
    entityTypeName = entityTypeName.substring(dotIndex + 1);
    final JsonObject entityTypeMap = metadata.getJsonObject(entitySchemaName, JsonObject.EMPTY);
    final JsonObject entityType = entityTypeMap.getJsonObject(entityTypeName, JsonObject.EMPTY);
    if (entityType.equalValue("$Kind", "EntityType")) {
      for (final String fieldName : entityType.keySet()) {
        if (!fieldName.startsWith("$")) {
          final JsonObject entityField = entityType.getJsonObject(fieldName);
          final DataType dataType = DATA_TYPE_MAP.getOrDefault(entityField.getString("$Type"),
            DataTypes.STRING);
          final boolean required = !entityField.getBoolean("$Nullable", true);
          final FieldDefinition fieldDefinition = new FieldDefinition(fieldName, dataType,
            required);
          final int length = entityField.getInteger("$MaxLength", 0);
          if (length != 0) {
            fieldDefinition.setLength(length);
          }
          final int precision = entityField.getInteger("$Precision", 0);
          final int scale = entityField.getInteger("$Scale", 0);
          if (scale == 0) {
            if (precision > 0) {
              fieldDefinition.setLength(precision);
            }
          } else {
            if (precision > 0) {
              fieldDefinition.setLength(precision - scale);
            }
            fieldDefinition.setScale(scale);
          }

          final int srid = entityField.getInteger("$SRID", 0);
          if (srid != 0) {
            final int axisCount = entityField.getJsonObject("@Geometry.axisCount", JsonObject.EMPTY)
              .getInteger("$Int", 2);
            final double scaleX = entityField.getJsonObject("@Geometry.scaleX", JsonObject.EMPTY)
              .getDouble("$Float", 0);
            final double scaleY = entityField.getJsonObject("@Geometry.scaleY", JsonObject.EMPTY)
              .getDouble("$Float", 0);
            final double scaleZ = entityField.getJsonObject("@Geometry.scaleZ", JsonObject.EMPTY)
              .getDouble("$Float", 0);
            final GeometryFactory geometryFactory = GeometryFactory.fixed(srid, axisCount, scaleX,
              scaleY, scaleZ);

            fieldDefinition.setGeometryFactory(geometryFactory);
          }
          addField(fieldDefinition);
        }
      }
      final List<String> idFieldNames = entityType.getValue("$Key");
      if (idFieldNames != null) {
        setIdFieldNames(idFieldNames);
      }
    }
  }

  @Override
  public Record newRecord(final MapEx values) {
    final Record record = newRecord();
    for (final FieldDefinition field : getFields()) {
      final String fieldName = field.getName();
      if (values.hasValue(fieldName)) {
        final Object value = values.getValue(field);
        record.setValue(field, value);
      }
    }
    return record;
  }
}
