package com.revolsys.gis.data.io;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class DataObjectIteratorReader extends IteratorReader<DataObject>
  implements DataObjectReader {
  public DataObjectIteratorReader(final DataObjectIterator iterator) {
    super(iterator);
  }

  public DataObjectMetaData getMetaData() {
    final DataObjectIterator iterator = (DataObjectIterator)iterator();
    iterator.hasNext();
    return iterator.getMetaData();
  }

  @Override
  public String toString() {
    return "Reader=" + iterator().toString();
  }
}
