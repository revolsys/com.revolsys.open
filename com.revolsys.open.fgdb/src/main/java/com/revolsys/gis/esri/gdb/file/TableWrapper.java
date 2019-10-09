package com.revolsys.gis.esri.gdb.file;

import java.util.function.Supplier;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;

import com.revolsys.beans.ObjectException;
import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Envelope;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;
import com.revolsys.util.ValueHolderWrapper;

public interface TableWrapper extends ValueHolderWrapper<Table>, BaseCloseable {

  default EnumRows closeRows(final EnumRows rows) {
    final TableReference tableReference = getTableReference();
    return tableReference.closeRows(rows);
  }

  @Override
  default TableWrapper connect() {
    final TableReference tableReference = getTableReference();
    return tableReference.connect();
  }

  default boolean deleteRecord(final Record record) {
    final FileGdbRecordStore recordStore = getRecordStore();
    final TableReference tableReference = getTableReference();
    return tableReference.modifyRecordRow(record, "OBJECTID", (table, row) -> {
      table.deleteRow(row);
      record.setState(RecordState.DELETED);
      recordStore.addStatistic("Delete", record);
    });
  }

  default PathName getPathName() {
    final TableReference tableReference = getTableReference();
    return tableReference.getPathName();
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
    final FileGdbRecordStore recordStore = getRecordStore();
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = recordStore
      .getRecordDefinition(sourceRecordDefinition);

    validateRequired(record, recordDefinition);

    try {
      final TableReference tableReference = getTableReference();
      final Row row = tableReference.valueFunctionSync(table -> table.createRowObject());

      try {
        for (final FieldDefinition field : recordDefinition.getFields()) {
          final String name = field.getName();
          try {
            final Object value = record.getValue(name);
            final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
            esriField.setInsertValue(record, row, value);
          } catch (final Throwable e) {
            throw new ObjectPropertyException(record, name, e);
          }
        }
        tableReference.valueConsumeSync(table -> table.insertRow(row));
        if (sourceRecordDefinition == recordDefinition) {
          for (final FieldDefinition field : recordDefinition.getFields()) {
            final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
            try {
              esriField.setPostInsertValue(record, row);
            } catch (final Throwable e) {
              throw new ObjectPropertyException(record, field.getName(), e);
            }
          }
          record.setState(RecordState.PERSISTED);
        }
      } finally {
        row.delete();
        recordStore.addStatistic("Insert", record);
      }
    } catch (final ObjectException e) {
      if (e.getObject() == record) {
        throw e;
      } else {
        throw new ObjectException(record, e);
      }
    } catch (final Throwable e) {
      throw new ObjectException(record, e);
    }
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
    final TableReference tableReference = getTableReference();
    final EnumRows rows = tableReference.valueFunctionSync(table -> {
      try {
        return table.search(fields, whereClause, recycling);
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
      return null;
    });
    return new FileGdbEnumRowsIterator(this, rows);
  }

  default FileGdbEnumRowsIterator search(final String fields, final String whereClause,
    final Envelope boundingBox, final boolean recycling) {
    EnumRows rows = null;
    if (!boundingBox.IsEmpty()) {
      final TableReference tableReference = getTableReference();
      rows = tableReference.valueFunctionSync(table -> {
        try {
          return table.search(fields, whereClause, boundingBox, recycling);
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
        return null;
      });
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
      final FileGdbRecordStore recordStore = getRecordStore();
      final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
      final RecordDefinition recordDefinition = recordStore
        .getRecordDefinition(sourceRecordDefinition);

      validateRequired(record, recordDefinition);

      final TableReference tableReference = getTableReference();
      tableReference.modifyRecordRow(record, "*", (table, row) -> {
        try {
          for (final FieldDefinition field : recordDefinition.getFields()) {
            final String name = field.getName();
            try {
              final Object value = record.getValue(name);
              final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
              esriField.setUpdateValue(record, row, value);
            } catch (final Throwable e) {
              throw new ObjectPropertyException(record, name, e);
            }
          }
          table.updateRow(row);
          record.setState(RecordState.PERSISTED);
          recordStore.addStatistic("Update", record);
        } catch (final ObjectException e) {
          if (e.getObject() == record) {
            throw e;
          } else {
            throw new ObjectException(record, e);
          }
        } catch (final Throwable e) {
          throw new ObjectException(record, e);
        }
      });
    }
  }

  private void validateRequired(final Record record, final RecordDefinition recordDefinition) {
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String name = field.getName();
      if (field.isRequired()) {
        final Object value = record.getValue(name);
        if (value == null && !((AbstractFileGdbFieldDefinition)field).isAutoCalculated()) {
          throw new ObjectPropertyException(record, name, "Value required");
        }
      }
    }
  }

  default void withTableLock(final Runnable action) {
    try (
      BaseCloseable lock = writeLock()) {
      valueConsumeSync(table -> action.run());
    }
  }

  default <V> V withTableLock(final Supplier<V> action) {
    try (
      BaseCloseable lock = writeLock()) {
      return valueFunctionSync(table -> action.get());
    }
  }

  default TableWrapper writeLock() {
    final TableReference tableReference = getTableReference();
    return tableReference.writeLock(false);
  }
}
