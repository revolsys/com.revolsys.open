package com.revolsys.gis.data.io;

import java.util.Iterator;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public interface DataObjectIterator extends Iterator<DataObject> {
  public DataObjectMetaData getMetaData();
}
