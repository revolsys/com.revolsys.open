package com.revolsys.io.moep;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.io.AbstractObjectWithProperties;

public class MoepDataObjectMetaDataFactory extends AbstractObjectWithProperties
  implements DataObjectMetaDataFactory {
  private static final Map<String, DataObjectMetaData> META_DATA_CACHE = new HashMap<String, DataObjectMetaData>();

  @Override
  public DataObjectMetaData getMetaData(final String typePath) {
    synchronized (META_DATA_CACHE) {
      DataObjectMetaData metaData = META_DATA_CACHE.get(typePath);
      if (metaData == null) {
        metaData = MoepConstants.createMetaData(typePath);
        META_DATA_CACHE.put(typePath, metaData);
      }
      return metaData;
    }
  }

}
