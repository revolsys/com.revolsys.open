package com.revolsys.record.schema;

import java.util.List;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.io.PathName;
import com.revolsys.io.PathNameProxy;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.util.IconNameProxy;

public interface RecordDefinitionProxy extends PathNameProxy, IconNameProxy, GeometryFactoryProxy {
  default int getFieldCount() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldCount();
  }

  default FieldDefinition getFieldDefinition(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getField(fieldName);
  }

  default FieldDefinition getFieldDefinition(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getField(fieldIndex);
  }

  default List<FieldDefinition> getFieldDefinitions() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFields();
  }

  default int getFieldIndex(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldIndex(fieldName);
  }

  default String getFieldName(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldName(fieldIndex);
  }

  default List<String> getFieldNames() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldNames();
  }

  default String getFieldTitle(final String name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getFieldTitle(name);
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return GeometryFactory.DEFAULT;
    } else {
      return recordDefinition.getGeometryFactory();
    }
  }

  default String getGeometryFieldName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getGeometryFieldName();
  }

  @Override
  default String getIconName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return "table";
    } else {
      return recordDefinition.getIconName();
    }
  }

  default String getIdFieldName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getIdFieldName();
  }

  default List<String> getIdFieldNames() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getIdFieldNames();
  }

  @Override
  default PathName getPathName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.getPathName();
  }

  RecordDefinition getRecordDefinition();

  default <R extends Record> RecordFactory<R> getRecordFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordFactory();
    }
  }

  default RecordStore getRecordStore() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getRecordStore();
    }
  }

  /**
   * Checks to see if the definition for this record has a field with the
   * specified name.
   *
   * @param name The name of the field.
   * @return True if the record has a field with the specified name.
   */
  default boolean hasField(final CharSequence name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return recordDefinition.hasField(name);
  }
}
