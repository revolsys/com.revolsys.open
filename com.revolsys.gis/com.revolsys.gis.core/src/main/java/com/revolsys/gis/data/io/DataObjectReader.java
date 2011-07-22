package com.revolsys.gis.data.io;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Reader;

public interface DataObjectReader extends Reader<DataObject> {
  DataObjectMetaData getMetaData();
}
