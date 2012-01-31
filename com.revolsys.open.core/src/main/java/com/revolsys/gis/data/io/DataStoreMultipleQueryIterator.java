package com.revolsys.gis.data.io;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.AbstractMultipleIterator;
import com.revolsys.gis.data.model.DataObject;

public class DataStoreMultipleQueryIterator extends
  AbstractMultipleIterator<DataObject> {

  final DataObjectStoreQueryReader reader;

  public DataStoreMultipleQueryIterator(DataObjectStoreQueryReader reader) {
    this.reader = reader;
  }

  private int queryIndex = 0;

  @Override
  public AbstractIterator<DataObject> getNextIterator() throws NoSuchElementException {
    final AbstractIterator<DataObject> iterator = reader.createQueryIterator(queryIndex);
    queryIndex++;
    return iterator;
  }

}
