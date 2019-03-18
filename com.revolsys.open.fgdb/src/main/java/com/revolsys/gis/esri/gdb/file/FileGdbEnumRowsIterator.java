package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Row;

public class FileGdbEnumRowsIterator extends AbstractIterator<Row> {

  private TableReference table;

  private EnumRows rows;

  FileGdbEnumRowsIterator(final TableReference table, final EnumRows rows) {
    this.table = table;
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
      final TableReference table = this.table;
      if (table != null) {
        this.table = null;
        table.close();
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
    if (this.rows == null) {
      throw new NoSuchElementException();
    } else {
      closeObject();
      synchronized (this.rows) {
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
