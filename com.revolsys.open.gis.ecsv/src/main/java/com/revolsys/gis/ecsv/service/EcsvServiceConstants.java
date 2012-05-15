package com.revolsys.gis.ecsv.service;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;

public final class EcsvServiceConstants {

  public static final String NS_PREFIX = "ecsv-ws";

  public static final String NS_URI = "http://ns.ecsv.org/ws";

  public static final String NAMESPACE_LIST = "namespaceList";

  public static final DataObjectMetaData NAMESPACE_LIST_METADATA;

  public static final String NAMESPACE_URI_ATTR = "namespaceUri";

  public static final String PATH_ATTR = "path";

  public static final String TYPE_LIST = "typeList";

  public static final DataObjectMetaData TYPE_LIST_METADATA;

  public static final String TYPE_NAME_ATTR = "typeName";

  static {
    final DataObjectMetaDataImpl namespaceListMetaData = new DataObjectMetaDataImpl(
      NAMESPACE_LIST);
    namespaceListMetaData.addAttribute(PATH_ATTR, DataTypes.STRING, true);
    namespaceListMetaData.addAttribute(NAMESPACE_URI_ATTR, DataTypes.ANY_URI,
      true);
    NAMESPACE_LIST_METADATA = namespaceListMetaData;
    final DataObjectMetaDataImpl typeListMetaData = new DataObjectMetaDataImpl(
      TYPE_LIST);
    typeListMetaData.addAttribute(PATH_ATTR, DataTypes.STRING, true);
    typeListMetaData.addAttribute(TYPE_NAME_ATTR, DataTypes.QNAME, true);
    TYPE_LIST_METADATA = typeListMetaData;
  }

  private EcsvServiceConstants() {
  }
}
