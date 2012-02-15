package com.revolsys.io.moep;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.io.AbstractObjectWithProperties;

public class MoepDataObjectMetaDataFactory extends AbstractObjectWithProperties
  implements DataObjectMetaDataFactory {
  private static final Map<QName, DataObjectMetaData> META_DATA_CACHE = new HashMap<QName, DataObjectMetaData>();

  public DataObjectMetaData getMetaData(final QName typeName) {
    synchronized (META_DATA_CACHE) {
      DataObjectMetaData metaData = META_DATA_CACHE.get(typeName);
      if (metaData == null) {
        metaData = MoepConstants.createMetaData(typeName);
        META_DATA_CACHE.put(typeName, metaData);
      }
      return metaData;
    }
  }

}
