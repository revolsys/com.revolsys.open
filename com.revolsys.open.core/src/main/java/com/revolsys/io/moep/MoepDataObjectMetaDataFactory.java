package com.revolsys.io.moep;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.io.AbstractObjectWithProperties;

public class MoepDataObjectMetaDataFactory extends AbstractObjectWithProperties
  implements RecordDefinitionFactory {
  private static final Map<String, RecordDefinition> META_DATA_CACHE = new HashMap<String, RecordDefinition>();

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    synchronized (META_DATA_CACHE) {
      RecordDefinition metaData = META_DATA_CACHE.get(typePath);
      if (metaData == null) {
        metaData = MoepConstants.createMetaData(typePath);
        META_DATA_CACHE.put(typePath, metaData);
      }
      return metaData;
    }
  }

}
