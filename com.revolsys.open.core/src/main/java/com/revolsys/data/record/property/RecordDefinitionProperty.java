package com.revolsys.data.record.property;

import com.revolsys.data.record.schema.RecordDefinition;

public interface RecordDefinitionProperty extends Cloneable {

  RecordDefinitionProperty clone();

  String getPropertyName();

  RecordDefinition getRecordDefinition();

  void setRecordDefinition(RecordDefinition recordDefinition);
}
