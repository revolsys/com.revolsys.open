package com.revolsys.record.schema;

import java.util.Collection;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.GeometryFactory;

public class RecordDefinitionBuilder {

  private final RecordDefinitionImpl recordDefinition;

  public RecordDefinitionBuilder() {
    this("");
  }

  public RecordDefinitionBuilder(final PathName pathName) {
    this.recordDefinition = new RecordDefinitionImpl(pathName);
  }

  public RecordDefinitionBuilder(final RecordDefinitionProxy recordDefinition,
    final Collection<String> fieldNames) {
    this(recordDefinition.getPathName());
    for (final String fieldName : fieldNames) {
      final FieldDefinition fieldDefinition = recordDefinition.getFieldDefinition(fieldName);
      addField(fieldDefinition);
    }
    this.recordDefinition.setIdFieldNames(recordDefinition.getIdFieldNames());
    this.recordDefinition.setGeometryFieldName(recordDefinition.getGeometryFieldName());
    this.recordDefinition.setGeometryFactory(recordDefinition.getGeometryFactory());
  }

  public RecordDefinitionBuilder(final String pathName) {
    this(PathName.newPathName(pathName));
  }

  public RecordDefinitionBuilder addField(final DataType type) {
    final String fieldName = type.getName();
    this.recordDefinition.addField(fieldName, type);
    return this;
  }

  public void addField(final FieldDefinition field) {
    this.recordDefinition.addField(field.clone());
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type) {
    this.recordDefinition.addField(fieldName, type);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final boolean required) {
    this.recordDefinition.addField(fieldName, type, required);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final int length) {
    this.recordDefinition.addField(fieldName, type, length, false);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final int length, final boolean required) {
    this.recordDefinition.addField(fieldName, type, length, required);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final int length, final int scale) {
    this.recordDefinition.addField(fieldName, type, length, scale);
    return this;
  }

  public RecordDefinitionBuilder addField(final String fieldName, final DataType type,
    final int length, final int scale, final boolean required) {
    this.recordDefinition.addField(fieldName, type, length, scale, required);
    return this;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public RecordDefinition newRecordDefinition(final RecordStore recordStore) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordStore.getRecordDefinition(recordDefinition);
  }

  public RecordDefinitionBuilder setGeometryFactory(final GeometryFactory geometryFactory) {
    this.recordDefinition.setGeometryFactory(geometryFactory);
    return this;
  }

  public void setIdFieldName(final String name) {
    this.recordDefinition.setIdFieldName(name);
  }
}
