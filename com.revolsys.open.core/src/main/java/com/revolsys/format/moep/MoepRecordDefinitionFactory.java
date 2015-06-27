package com.revolsys.format.moep;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.properties.BaseObjectWithProperties;

public class MoepRecordDefinitionFactory extends BaseObjectWithProperties implements
  RecordDefinitionFactory {
  private static final Map<String, RecordDefinition> RECORD_DEFINITION_CACHE = new HashMap<String, RecordDefinition>();

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    synchronized (RECORD_DEFINITION_CACHE) {
      RecordDefinition recordDefinition = RECORD_DEFINITION_CACHE.get(typePath);
      if (recordDefinition == null) {
        recordDefinition = MoepConstants.createRecordDefinition(typePath);
        RECORD_DEFINITION_CACHE.put(typePath, recordDefinition);
      }
      return recordDefinition;
    }
  }

}
