package com.revolsys.gis.esri.gdb.file.capi;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.AbstractMultipleIteratorReader;

public class FileGdbReader extends AbstractMultipleIteratorReader<DataObject> {

  private List<String> typePaths = new ArrayList<String>();

  private final CapiFileGdbDataObjectStore dataStore;

  private BoundingBox boundingBox;

  private int index = 0;

  public FileGdbReader(final CapiFileGdbDataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  @Override
  protected AbstractIterator<DataObject> getNextIterator() {
    if (index < typePaths.size()) {
      final String typePath = typePaths.get(index);
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(dataStore,
        typePath);
      if (boundingBox != null) {
        iterator.setBoundingBox(boundingBox);
      }
      index++;
      return iterator;
    } else {
      return null;
    }
  }

  public List<String> getTypeNames() {
    return typePaths;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setTypeNames(final List<String> typePaths) {
    this.typePaths = typePaths;
  }

  @Override
  public String toString() {
    return "Reader " + dataStore.getLabel() + " " + typePaths + " "
      + boundingBox;
  }
}
