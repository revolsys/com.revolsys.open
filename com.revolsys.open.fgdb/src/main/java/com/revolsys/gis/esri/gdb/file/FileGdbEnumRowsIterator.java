package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public class FileGdbEnumRowsIterator extends AbstractIterator<Row> {
  private FileGdbRecordStore recordStore;

  private EnumRows rows;

  FileGdbEnumRowsIterator(final FileGdbRecordStore recordStore, final EnumRows rows) {
    this.recordStore = recordStore;
    this.rows = rows;
    synchronized (recordStore.enumRowsToClose) {
      recordStore.enumRowsToClose.add(this);
    }
  }

  private void closeObject() {
    final Row previousRow = getObject();
    if (previousRow != null) {
      previousRow.close();
    }
  }

  @Override
  protected void doClose() {
    synchronized (this) {
      closeObject();
      final EnumRows rows = this.rows;
      final FileGdbRecordStore recordStore = this.recordStore;
      this.recordStore = null;
      this.rows = null;
      if (rows != null) {
        try {
          synchronized (recordStore.enumRowsToClose) {
            recordStore.enumRowsToClose.remove(this);
          }
        } finally {
          try {
            rows.Close();
          } finally {
            rows.delete();
          }
        }
      }
    }
  }

  @Override
  protected Row getNext() {
    synchronized (this) {
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
