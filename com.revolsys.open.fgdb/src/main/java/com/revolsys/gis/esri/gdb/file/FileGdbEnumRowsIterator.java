package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Row;

public class FileGdbEnumRowsIterator extends AbstractIterator<Row> {

  private final TableWrapper table;

  private EnumRows rows;

  private boolean closed;

  FileGdbEnumRowsIterator(final TableWrapper table, final EnumRows rows) {
    this.table = table;
    this.rows = rows;
  }

  @Override
  protected synchronized void closeDo() {
    closeObject();
    final TableWrapper table = this.table;
    if (table != null) {
      if (!this.closed) {
        final EnumRows rows = this.rows;
        if (rows != null) {
          this.rows = null;
          table.closeRows(rows);
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
  protected synchronized Row getNext() {
    closeObject();
    final Row row = this.table.getNext(this.rows);
    if (row != null) {
      return row;
    }
    throw new NoSuchElementException();
  }

  @Override
  public String toString() {
    return this.table.toString();
  }
}
