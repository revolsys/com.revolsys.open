package com.revolsys.record.schema;

import java.util.Collections;
import java.util.List;

import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import org.jeometry.common.io.PathNameProxy;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.util.IconNameProxy;

public interface RecordDefinitionProxy extends PathNameProxy, IconNameProxy, GeometryFactoryProxy {
  default int getFieldCount() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return 0;
    } else {
      return recordDefinition.getFieldCount();
    }
  }

  default FieldDefinition getFieldDefinition(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getField(fieldName);
    }
  }

  default FieldDefinition getFieldDefinition(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getField(fieldIndex);
    }
  }

  default List<FieldDefinition> getFieldDefinitions() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return Collections.emptyList();
    } else {
      return recordDefinition.getFields();
    }
  }

  default int getFieldIndex(final CharSequence fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null || fieldName == null) {
      return -1;
    } else {
      return recordDefinition.getFieldIndex(fieldName.toString());
    }
  }

  default String getFieldName(final int fieldIndex) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getFieldName(fieldIndex);
    }
  }

  default List<String> getFieldNames() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return Collections.emptyList();
    } else {
      return recordDefinition.getFieldNames();
    }
  }

  default String getFieldTitle(final String name) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return name;
    } else {
      return recordDefinition.getFieldTitle(name);
    }
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return GeometryFactory.DEFAULT_3D;
    } else {
      return recordDefinition.getGeometryFactory();
    }
  }

  default String getGeometryFieldName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getGeometryFieldName();
    }
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
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getIdFieldName();
    }
  }

  default List<String> getIdFieldNames() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return Collections.emptyList();
    } else {
      return recordDefinition.getIdFieldNames();
    }
  }

  @Override
  default PathName getPathName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return null;
    } else {
      return recordDefinition.getPathName();
    }
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

  default <R extends RecordStore> R getRecordStore() {
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
    if (recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.hasField(name);
    }
  }

  default boolean hasIdField() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return false;
    } else {
      return recordDefinition.hasIdField();
    }
  }

  default boolean isIdField(final String fieldName) {
    if (fieldName == null) {
      return false;
    } else {
      final String idFieldName = getIdFieldName();
      return fieldName.equals(idFieldName);
    }
  }

}
