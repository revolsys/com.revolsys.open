package com.revolsys.gis.esri.gdb.file;

import java.util.function.Supplier;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;

import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Envelope;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;
import com.revolsys.util.ValueHolderWrapper;

public interface TableWrapper extends ValueHolderWrapper<Table>, BaseCloseable {

  default void closeRows(final EnumRows rows) {
    final TableReference tableReference = getTableReference();
    tableReference.closeRowsDo(rows);
  }

  @Override
  default TableWrapper connect() {
    final TableReference tableReference = getTableReference();
    return tableReference.connect();
  }

  default boolean deleteRecord(final Record record) {
    final TableReference tableReference = getTableReference();
    return tableReference.deleteRecordRow(record);
  }

  default Row getNext(final EnumRows rows) {
    final TableReference tableReference = getTableReference();
    return tableReference.getNext(rows);
  }

  default PathName getPathName() {
    final TableReference tableReference = getTableReference();
    return tableReference.getPathName();
  }

  default RecordDefinition getRecordDefinition() {
    return getTableReference().getRecordDefinition();
  }

  default FileGdbRecordStore getRecordStore() {
    final TableReference tableReference = getTableReference();
    return tableReference.getRecordStore();
  }

  TableReference getTableReference();

  @Override
  default Table getValue() {
    final TableReference tableReference = getTableReference();
    return tableReference.getValue();
  }

  default void insertRecord(final Record record) {
    final TableReference tableReference = getTableReference();
    tableReference.insertRecord(record);
  }

  default boolean isClosed() {
    final TableReference tableReference = getTableReference();
    return tableReference.isClosed();
  }

  default boolean isLocked() {
    final TableReference tableReference = getTableReference();
    return tableReference.isLocked();
  }

  default FileGdbEnumRowsIterator query(final String sql, final boolean recycling) {
    final TableReference tableReference = getTableReference();
    final EnumRows rows = tableReference.query(sql, recycling);
    return new FileGdbEnumRowsIterator(this, rows);
  }

  default FileGdbEnumRowsIterator search(final String fields, final String whereClause,
    final boolean recycling) {
    EnumRows rows = null;
    final TableReference tableReference = getTableReference();
    try {
      rows = tableReference.search(fields, whereClause, recycling);
    } catch (final Throwable e) {
      if (!isClosed()) {
        final StringBuilder logQuery = new StringBuilder("ERROR executing query SELECT ");
        logQuery.append(fields);
        logQuery.append(" FROM ");
        logQuery.append(tableReference.getCatalogPath());
        if (Property.hasValue(whereClause)) {
          logQuery.append(" WHERE ");
          logQuery.append(whereClause);
        }
        throw Exceptions.wrap(logQuery.toString(), e);
      }
    }
    return new FileGdbEnumRowsIterator(this, rows);
  }

  default FileGdbEnumRowsIterator search(final String fields, final String whereClause,
    final Envelope boundingBox, final boolean recycling) {
    EnumRows rows = null;
    if (!boundingBox.IsEmpty()) {
      final TableReference tableReference = getTableReference();
      try {
        rows = tableReference.search(fields, whereClause, boundingBox, recycling);
      } catch (final Throwable e) {
        if (!isClosed()) {
          final StringBuilder logQuery = new StringBuilder("ERROR executing query SELECT ");
          logQuery.append(fields);
          logQuery.append(" FROM ");
          logQuery.append(tableReference.getCatalogPath());
          logQuery.append(" WHERE ");
          if (Property.hasValue(whereClause)) {
            logQuery.append(whereClause);
            logQuery.append(" AND");
          }
          logQuery.append("GEOMETRY intersects ");
          logQuery.append(BoundingBox.bboxToWkt(//
            boundingBox.getXMin(), //
            boundingBox.getYMin(), //
            boundingBox.getXMax(), //
            boundingBox.getYMax()//
          ));
          throw Exceptions.wrap(logQuery.toString(), e);
        }
      }
    }
    return new FileGdbEnumRowsIterator(this, rows);
  }

  default void setLoadOnlyMode(final boolean loadOnly) {
    final TableReference tableReference = getTableReference();
    tableReference.setLoadOnlyMode(loadOnly);
  }

  default void updateRecord(final Record record) {
    final Object objectId = record.getValue("OBJECTID");
    if (objectId == null) {
      insertRecord(record);
    } else if (record.getState() == RecordState.MODIFIED) {
      final TableReference tableReference = getTableReference();
      tableReference.updateRecordRow(record);
    }
  }

  default void withTableLock(final Runnable action) {
    final TableReference tableReference = getTableReference();
    tableReference.withTableLock(action);
  }

  default <V> V withTableLock(final Supplier<V> action) {
    try (
      BaseCloseable lock = writeLock()) {
      final TableReference tableReference = getTableReference();
      return tableReference.withTableLock(action);
    }
  }

  default TableWrapper writeLock() {
    final TableReference tableReference = getTableReference();
    return tableReference.writeLock(false);
  }
}
