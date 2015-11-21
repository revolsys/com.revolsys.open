package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public class FileGdbEnumRowsIterator extends AbstractIterator<Row> {
  private EnumRows rows;

  FileGdbEnumRowsIterator(final EnumRows rows) {
    this.rows = rows;
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
