package com.revolsys.gis.data.io;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public interface DataObjectReader extends Reader<DataObject> {
  DataObjectMetaData getMetaData();
}
