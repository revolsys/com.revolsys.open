package com.revolsys.gis.esri.gdb.file;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.record.Record;
import com.revolsys.io.AbstractMultipleIteratorReader;
import com.revolsys.jts.geom.BoundingBox;

public class FileGdbReader extends AbstractMultipleIteratorReader<Record> {

  private List<String> typePaths = new ArrayList<String>();

  private final CapiFileGdbRecordStore recordStore;

  private BoundingBox boundingBox;

  private int index = 0;

  public FileGdbReader(final CapiFileGdbRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  @Override
  protected AbstractIterator<Record> getNextIterator() {
    if (index < typePaths.size()) {
      final String typePath = typePaths.get(index);
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(recordStore,
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
    return "Reader " + recordStore.getLabel() + " " + typePaths + " "
      + boundingBox;
  }
}
