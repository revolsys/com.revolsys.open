package com.revolsys.gis.esri.gdb.file;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Function;

import com.revolsys.beans.ObjectException;
import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Envelope;
import com.revolsys.esri.filegdb.jni.Geodatabase;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.PathName;
import com.revolsys.logging.Logs;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;

public class TableReference implements Closeable {
  private Table table;

  private int referenceCount = 1;

  private int lockCount = 0;

  private final String catalogPath;

  private Geodatabase geodatabase;

  private final FileGdbRecordStore recordStore;

  private final PathName pathName;

  TableReference(final FileGdbRecordStore recordStore, final Geodatabase geodatabase,
    final Table table, final PathName pathName, final String catalogPath) {
    this.recordStore = recordStore;
    this.pathName = pathName;
    this.catalogPath = catalogPath;
    setTable(geodatabase, table);
  }

  public void acceptTable(final Consumer<Table> action) {
    action.accept(this.table);
  }

  public <V> V applyTable(final Function<Table, V> action) {
    return action.apply(this.table);
  }

  @Override
  public synchronized void close() {
    if (this.referenceCount-- <= 0) {
      this.referenceCount = 0;
      final Table table = this.table;
      final Geodatabase geodatabase = this.geodatabase;
      this.geodatabase = null;
      this.table = null;
      try {
        geodatabase.closeTable(table);
      } catch (final Exception e) {
        Logs.error(this, "Unable to close table: " + this.catalogPath, e);
      } finally {
        table.delete();

      }
    }
  }

  public boolean deleteRecord(final Record record) {
    final Integer objectId = record.getInteger("OBJECTID");
    final PathName typePath = record.getPathName();
    if (objectId != null) {
      final String whereClause = "OBJECTID=" + objectId;
      try (
        BaseCloseable lock = writeLock();
        final FileGdbEnumRowsIterator rows = search(typePath, "OBJECTID", whereClause, false)) {
        for (final Row row : rows) {
          synchronized (this.table) {
            final boolean loadOnly = isLocked();
            if (loadOnly) {
              setLoadOnlyMode(false);
            }
            this.table.deleteRow(row);
            if (loadOnly) {
              setLoadOnlyMode(true);
            }
          }
          record.setState(RecordState.DELETED);
          this.recordStore.addStatistic("Delete", record);
          return true;
        }
      }
    }
    return false;
  }

  public PathName getPathName() {
    return this.pathName;
  }

  void insertRecord(final Record record) {
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = this.recordStore
      .getRecordDefinition(sourceRecordDefinition);

    validateRequired(record, recordDefinition);

    final PathName typePath = recordDefinition.getPathName();
    if (this.table == null) {
      throw new ObjectException(record, "Cannot find table: " + typePath);
    } else {
      try {
        final Row row = newRowObject();

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
          insertRow(row);
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
          this.recordStore.addStatistic("Insert", record);
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
  }

  protected synchronized void insertRow(final Row row) {
    if (isOpen()) {
      this.table.insertRow(row);
    }
  }

  public synchronized boolean isClosed() {
    return this.referenceCount == 0;
  }

  public synchronized boolean isLocked() {
    return this.lockCount >= 0;
  }

  public synchronized boolean isOpen() {
    return this.referenceCount > 0;
  }

  private synchronized void lock() {
    if (++this.lockCount == 1) {
      this.table.setWriteLock();
      this.table.setLoadOnlyMode(true);
    }
  }

  protected synchronized Row newRowObject() {
    if (isOpen()) {
      return this.table.createRowObject();
    } else {
      return null;
    }
  }

  private synchronized void releaseWriteLock() {
    if (this.lockCount-- <= 0) {
      this.lockCount = 0;
      try {
        synchronized (this.table) {
          setLoadOnlyMode(false);
          this.table.freeWriteLock();
        }
      } catch (final Exception e) {
        Logs.error(this, "Unable to free write lock for table: " + this.catalogPath, e);
      }
    }
  }

  public synchronized FileGdbEnumRowsIterator search(final Object typePath, final String fields,
    final String whereClause, final boolean recycling) {
    EnumRows rows = null;
    if (isOpen()) {
      try {
        rows = this.table.search(fields, whereClause, recycling);
      } catch (final Throwable e) {
        if (!isClosed()) {
          final StringBuilder logQuery = new StringBuilder("ERROR executing query SELECT ");
          logQuery.append(fields);
          logQuery.append(" FROM ");
          logQuery.append(typePath);
          if (Property.hasValue(whereClause)) {
            logQuery.append(" WHERE ");
            logQuery.append(whereClause);
          }
          throw Exceptions.wrap(logQuery.toString(), e);
        }

      }
    }
    return new FileGdbEnumRowsIterator(this, rows);
  }

  public synchronized FileGdbEnumRowsIterator search(final Object typePath, final String fields,
    final String whereClause, final Envelope boundingBox, final boolean recycling) {
    EnumRows rows = null;
    if (!boundingBox.IsEmpty()) {
      if (isOpen()) {
        try {
          rows = this.table.search(fields, whereClause, boundingBox, recycling);
        } catch (final Throwable e) {
          if (!isClosed()) {
            final StringBuilder logQuery = new StringBuilder("ERROR executing query SELECT ");
            logQuery.append(fields);
            logQuery.append(" FROM ");
            logQuery.append(typePath);
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
    }
    return new FileGdbEnumRowsIterator(this, rows);
  }

  synchronized void setLoadOnlyMode(final boolean loadOnly) {
    // table.setLoadOnlyMode(loadOnly);
  }

  void setTable(final Geodatabase geodatabase, final Table table) {
    this.geodatabase = geodatabase;
    this.table = table;
  }

  @Override
  public String toString() {
    return this.catalogPath;
  }

  void updateRecord(final Record record) {
    final Object objectId = record.getValue("OBJECTID");
    if (objectId == null) {
      insertRecord(record);
    } else {
      final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
      final RecordDefinition recordDefinition = this.recordStore
        .getRecordDefinition(sourceRecordDefinition);

      validateRequired(record, recordDefinition);

      final PathName typePath = sourceRecordDefinition.getPathName();
      final String whereClause = "OBJECTID=" + objectId;
      try (
        final FileGdbEnumRowsIterator rows = search(typePath, "*", whereClause, false)) {
        for (final Row row : rows) {
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
            updateRow(row);
            record.setState(RecordState.PERSISTED);
            this.recordStore.addStatistic("Update", record);
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
      }
    }
  }

  protected synchronized void updateRow(final Row row) {
    if (isOpen()) {
      final boolean loadOnly = isLocked();
      if (loadOnly) {
        setLoadOnlyMode(false);
      }
      this.table.updateRow(row);
      if (loadOnly) {
        setLoadOnlyMode(true);
      }
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

  public synchronized BaseCloseable writeLock() {
    lock();
    return this::releaseWriteLock;
  }
}
