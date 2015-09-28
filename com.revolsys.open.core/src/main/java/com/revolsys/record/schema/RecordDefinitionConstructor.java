package com.revolsys.record.schema;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.PathName;

public class RecordDefinitionConstructor {

  private final RecordDefinitionImpl recordDefinition;

  public RecordDefinitionConstructor(final PathName pathName) {
    this.recordDefinition = new RecordDefinitionImpl(pathName);
  }

  public RecordDefinitionConstructor(final String pathName) {
    this(PathName.newPathName(pathName));
  }

  public RecordDefinitionConstructor addField(final String fieldName, final DataType type) {
    this.recordDefinition.addField(fieldName, type);
    return this;
  }

  public RecordDefinitionConstructor addField(final String fieldName, final DataType type,
    final boolean required) {
    this.recordDefinition.addField(fieldName, type, required);
    return this;
  }

  public RecordDefinitionConstructor addField(final String fieldName, final DataType type,
    final int length) {
    this.recordDefinition.addField(fieldName, type, length, false);
    return this;
  }

  public RecordDefinitionConstructor addField(final String fieldName, final DataType type,
    final int length, final boolean required) {
    this.recordDefinition.addField(fieldName, type, length, required);
    return this;
  }

  public RecordDefinitionConstructor addField(final String fieldName, final DataType type,
    final int length, final int scale) {
    this.recordDefinition.addField(fieldName, type, length, scale);
    return this;
  }

  public RecordDefinitionConstructor addField(final String fieldName, final DataType type,
    final int length, final int scale, final boolean required) {
    this.recordDefinition.addField(fieldName, type, length, scale, required);
    return this;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public RecordDefinitionConstructor setGeometryFactory(final GeometryFactory geometryFactory) {
    this.recordDefinition.setGeometryFactory(geometryFactory);
    return this;
  }
}
