package com.revolsys.gis.esri.gdb.file;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.data.record.Record;
import com.revolsys.io.AbstractMultipleIteratorReader;
import com.revolsys.jts.geom.BoundingBox;

public class FileGdbReader extends AbstractMultipleIteratorReader<Record> {

  private BoundingBox boundingBox;

  private int index = 0;

  private final FileGdbRecordStore recordStore;

  private List<String> typePaths = new ArrayList<>();

  public FileGdbReader(final FileGdbRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  protected AbstractIterator<Record> getNextIterator() {
    if (this.index < this.typePaths.size()) {
      final String typePath = this.typePaths.get(this.index);
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this.recordStore, typePath);
      if (this.boundingBox != null) {
        iterator.setBoundingBox(this.boundingBox);
      }
      this.index++;
      return iterator;
    } else {
      return null;
    }
  }

  public List<String> getTypeNames() {
    return this.typePaths;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setTypeNames(final List<String> typePaths) {
    this.typePaths = typePaths;
  }

  @Override
  public String toString() {
    return "Reader " + this.recordStore.getLabel() + " " + this.typePaths + " " + this.boundingBox;
  }
}
