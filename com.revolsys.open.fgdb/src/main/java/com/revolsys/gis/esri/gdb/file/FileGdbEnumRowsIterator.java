package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Row;

public class FileGdbEnumRowsIterator extends AbstractIterator<Row> {

  private final FileGdbRecordStore recordStore;

  private EnumRows rows;

  FileGdbEnumRowsIterator(final FileGdbRecordStore recordStore, final EnumRows rows) {
    this.recordStore = recordStore;
    this.rows = rows;
  }

  @Override
  protected void closeDo() {
    synchronized (this) {
      closeObject();
      final EnumRows rows = this.rows;
      this.rows = null;
      if (rows != null) {
        try {
          rows.Close();
        } finally {
          rows.delete();
        }
      }
    }
  }

  private void closeObject() {
    final Row previousRow = getObject();
    if (previousRow != null) {
      previousRow.close();
    }
  }

  @Override
  protected Row getNext() {
    synchronized (this.recordStore.getApiSync()) {
      if (this.rows == null) {
        throw new NoSuchElementException();
      } else {
        closeObject();
        final Row row = this.rows.next();
        if (row == null) {
          throw new NoSuchElementException();
        } else {
          return row;
        }
      }
    }
  }
}
