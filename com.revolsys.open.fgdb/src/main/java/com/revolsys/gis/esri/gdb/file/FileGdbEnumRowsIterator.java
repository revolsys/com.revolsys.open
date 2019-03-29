package com.revolsys.gis.esri.gdb.file;

import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Row;

public class FileGdbEnumRowsIterator extends AbstractIterator<Row> {

  private TableWrapper table;

  private EnumRows rows;

  FileGdbEnumRowsIterator(final TableWrapper table, final EnumRows rows) {
    if (table != null) {
      this.table = table.connect();
    }
    this.rows = rows;
  }

  @Override
  protected void closeDo() {
    synchronized (this) {
      closeObject();
      final TableWrapper table = this.table;
      if (table != null) {
        this.rows = table.closeRows(this.rows);
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
    closeObject();
    final Row row = this.table.nextRow(this.rows);
    if (row == null) {
      throw new NoSuchElementException();
    } else {
      return row;
    }
  }
}
