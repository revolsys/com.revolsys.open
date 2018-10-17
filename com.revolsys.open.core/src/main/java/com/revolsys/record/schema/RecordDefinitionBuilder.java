package com.revolsys.record.schema;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathName;

public class RecordDefinitionBuilder {

  private final RecordDefinitionImpl recordDefinition;

  public RecordDefinitionBuilder() {
    this("");
  }

  public RecordDefinitionBuilder(final PathName pathName) {
    this.recordDefinition = new RecordDefinitionImpl(pathName);
  }

  public RecordDefinitionBuilder(final RecordDefinitionProxy recordDefinition) {
    this(recordDefinition.getPathName());
    for (final FieldDefinition fieldDefinition : recordDefinition.getFieldDefinitions()) {
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

  public RecordDefinitionBuilder addField(final FieldDefinition field) {
    this.recordDefinition.addField(field.clone());
    return this;
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

  public RecordDefinitionBuilder setGeometryFieldName(final String fieldName) {
    this.recordDefinition.setGeometryFieldName(fieldName);
    return this;
  }

  public RecordDefinitionBuilder setPathName(final String path) {
    this.recordDefinition.setPathName(path);
    return this;
  }
}
