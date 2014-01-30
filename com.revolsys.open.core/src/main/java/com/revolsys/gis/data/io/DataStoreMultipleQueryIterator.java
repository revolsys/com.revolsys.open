package com.revolsys.gis.data.io;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.collection.AbstractMultipleIterator;
import com.revolsys.gis.data.model.DataObject;

public class DataStoreMultipleQueryIterator extends
  AbstractMultipleIterator<DataObject> {

  private DataObjectStoreQueryReader reader;

  private int queryIndex = 0;

  public DataStoreMultipleQueryIterator(final DataObjectStoreQueryReader reader) {
    this.reader = reader;
  }

  @Override
  public void doClose() {
    super.doClose();
    reader = null;
  }

  @Override
  public AbstractIterator<DataObject> getNextIterator()
    throws NoSuchElementException {
    if (reader == null) {
      throw new NoSuchElementException();
    } else {
      final AbstractIterator<DataObject> iterator = reader.createQueryIterator(queryIndex);
      queryIndex++;
      return iterator;
    }
  }

}
