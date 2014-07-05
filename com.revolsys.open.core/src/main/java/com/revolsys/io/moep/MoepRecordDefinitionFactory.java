package com.revolsys.io.moep;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.io.AbstractObjectWithProperties;

public class MoepRecordDefinitionFactory extends AbstractObjectWithProperties
  implements RecordDefinitionFactory {
  private static final Map<String, RecordDefinition> META_DATA_CACHE = new HashMap<String, RecordDefinition>();

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    synchronized (META_DATA_CACHE) {
      RecordDefinition recordDefinition = META_DATA_CACHE.get(typePath);
      if (recordDefinition == null) {
        recordDefinition = MoepConstants.createMetaData(typePath);
        META_DATA_CACHE.put(typePath, recordDefinition);
      }
      return recordDefinition;
    }
  }

}
