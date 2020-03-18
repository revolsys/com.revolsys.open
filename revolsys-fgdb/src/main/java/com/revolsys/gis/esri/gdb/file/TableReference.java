package com.revolsys.gis.esri.gdb.file;

import java.util.function.Supplier;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.beans.ObjectException;
import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Envelope;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.io.BaseCloseable;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.CloseableValueHolder;
import com.revolsys.util.ValueHolder;

class TableReference extends CloseableValueHolder<Table> {
  private class EsriFileGdbTableConnection implements TableWrapper {

    @Override
    public void close() {
      disconnect();
    }

    @Override
    public TableReference getTableReference() {
      return TableReference.this;
    }

    @Override
    public ValueHolder<Table> getValueHolder() {
      return TableReference.this;
    }

    @Override
    public String toString() {
      return TableReference.this.toString();
    }
  }

  private class EsriFileGdbTableLock implements TableWrapper {
    private final boolean loadOnly;

    public EsriFileGdbTableLock(final boolean loadOnly) {
      this.loadOnly = loadOnly;
    }

    @Override
    public void close() {
      writeUnlock(this.loadOnly);
    }

    @Override
    public TableReference getTableReference() {
      return TableReference.this;
    }

    @Override
    public ValueHolder<Table> getValueHolder() {
      return TableReference.this;
    }

    @Override
    public String toString() {
      return TableReference.this.toString();
    }
  };

  private final TableWrapper locker = new EsriFileGdbTableLock(false);

  private final TableWrapper lockerLoadOnly = new EsriFileGdbTableLock(true);

  private int lockCount = 0;

  private int loadOnlyCount = 0;

  private final String catalogPath;

  private final GeodatabaseReference geodatabase;

  private BaseCloseable geodatabaseClosable;

  private final FileGdbRecordStore recordStore;

  private final PathName pathName;

  private final FileGdbRecordDefinition recordDefinition;

  private final String tableName;

  TableReference(final FileGdbRecordStore recordStore,
    final FileGdbRecordDefinition recordDefinition, final GeodatabaseReference geodatabase,
    final PathName pathName, final String catalogPath) {
    this.recordStore = recordStore;
    this.recordDefinition = recordDefinition;
    this.geodatabase = geodatabase;
    this.pathName = pathName;
    this.catalogPath = catalogPath;
    this.tableName = JdbcUtils.getQualifiedTableName(this.recordDefinition.getPath());
  }

  void closeRowsDo(final EnumRows rows) {
    if (rows != null) {
      try {
        rows.Close();
      } finally {
        rows.delete();
      }
    }
  }

  @Override
  public TableWrapper connect() {
    return (TableWrapper)super.connect();
  }

  boolean deleteRecordRow(final Record record) {
    final Integer objectId = record.getInteger("OBJECTID");
    if (objectId != null) {
      synchronized (this.geodatabase) {
        try (
          BaseCloseable lock = writeLock(false)) {
          final String deleteSql = "DELETE FROM " + this.tableName + " WHERE OBJECTID=" + objectId;
          final EnumRows rows = query(deleteSql, true);
          closeRowsDo(rows);

          record.setState(RecordState.DELETED);
          this.recordStore.addStatistic("Delete", record);
          return true;
        }
      }
    }
    return false;
  }

  public String getCatalogPath() {
    return this.catalogPath;
  }

  synchronized Row getNext(final EnumRows rows) {
    if (rows != null) {
      final Table table = getValue();
      if (table != null) {
        try {
          return rows.next();
        } finally {
          disconnect();
        }
      }
    }
    return null;
  }

  public PathName getPathName() {
    return this.pathName;
  }

  public synchronized int getRecordCount() {
    final Table table = getValue();
    if (table != null) {
      try {
        return table.getRowCount();
      } finally {
        disconnect();
      }
    }
    return 0;
  }

  public FileGdbRecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public FileGdbRecordStore getRecordStore() {
    return this.recordStore;
  }

  void insertRecord(final Record record) {
    final FileGdbRecordStore recordStore = getRecordStore();
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = recordStore
      .getRecordDefinition(sourceRecordDefinition);

    try {
      validateRequired(record);
      synchronized (this) {
        final Table table = getValue();
        if (table != null) {
          try {
            final Row row = table.createRowObject();

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
              synchronized (this.geodatabase) {
                table.insertRow(row);
              }
              if (sourceRecordDefinition == recordDefinition) {
                record.setState(RecordState.INITIALIZING);
                try {
                  for (final FieldDefinition field : recordDefinition.getFields()) {
                    final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
                    try {
                      esriField.setPostInsertValue(record, row);
                    } catch (final Throwable e) {
                      throw new ObjectPropertyException(record, field.getName(), e);
                    }
                  }
                } finally {
                  record.setState(RecordState.PERSISTED);
                }
              }
            } finally {
              row.delete();
              recordStore.addStatistic("Insert", record);
            }
          } finally {
            disconnect();
          }
        }
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

  synchronized boolean isLocked() {
    return this.lockCount >= 0;
  }

  @Override
  protected TableWrapper newCloseable() {
    return new EsriFileGdbTableConnection();
  }

  synchronized EnumRows query(final String sql, final boolean recycling) {
    return this.geodatabase.query(sql, recycling);
  }

  synchronized EnumRows search(final String subfields, final String whereClause,
    final boolean recycling) {
    final Table table = getValue();
    if (table != null) {
      try {
        return table.search(subfields, whereClause, recycling);
      } finally {
        disconnect();
      }
    }
    return null;
  }

  synchronized EnumRows search(final String subfields, final String whereClause,
    final Envelope boundingBox, final boolean recycling) {
    final Table table = getValue();
    if (table != null) {
      try {
        return table.search(subfields, whereClause, boundingBox, recycling);
      } finally {
        disconnect();
      }
    }
    return null;
  }

  synchronized void setLoadOnlyMode(final boolean loadOnly) {
    // table.setLoadOnlyMode(loadOnly);
  }

  @Override
  public String toString() {
    return this.recordStore.getFileName() + "\t" + this.catalogPath;
  }

  boolean updateRecordRow(final Record record) {
    final Integer objectId = record.getInteger("OBJECTID");
    if (objectId != null) {
      validateRequired(record);
      final String whereClause = "OBJECTID=" + objectId;
      synchronized (this.geodatabase) {
        final Table table = getValue();
        try (
          BaseCloseable lock = writeLock(false)) {
          final EnumRows rows = table.search("*", whereClause, false);
          try {
            final Row row = rows.next();
            if (row != null) {
              try {
                for (final FieldDefinition field : this.recordDefinition.getFields()) {
                  final String name = field.getName();
                  try {
                    final Object value = record.getValue(name);
                    final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
                    esriField.setUpdateValue(record, row, value);
                  } catch (final Throwable e) {
                    throw new ObjectPropertyException(record, name, e);
                  }
                }
                synchronized (table) {
                  table.updateRow(row);
                }
                record.setState(RecordState.PERSISTED);
              } catch (final ObjectException e) {
                if (e.getObject() == record) {
                  throw e;
                } else {
                  throw new ObjectException(record, e);
                }
              } catch (final Throwable e) {
                throw new ObjectException(record, e);
              }
              row.delete();
              this.recordStore.addStatistic("Update", record);

              return true;
            }
          } finally {
            try {
              closeRowsDo(rows);
            } finally {
              disconnect();
            }
          }
        }
      }
    }
    return false;
  }

  private void validateRequired(final Record record) {
    for (final FieldDefinition field : this.recordDefinition.getFields()) {
      final String name = field.getName();
      if (field.isRequired()) {
        final Object value = record.getValue(name);
        if (value == null && !((AbstractFileGdbFieldDefinition)field).isAutoCalculated()) {
          throw new ObjectPropertyException(record, name, "Value required");
        }
      }
    }
  }

  @Override
  protected void valueClose(final Table table) {
    try {
      // System.out.println("CL\tt\t" + this);
      this.geodatabase.closeTable(table, this.catalogPath);
    } finally {
      final BaseCloseable geodatabaseClosable = this.geodatabaseClosable;
      this.geodatabaseClosable = null;
      if (geodatabaseClosable != null) {
        // System.out.println("DI\tg\t" + this.geodatabase);
        geodatabaseClosable.close();
      }
    }
  }

  @Override
  protected Table valueNew() {
    // System.out.println("CO\tg\t" + this.geodatabase);
    this.geodatabaseClosable = this.geodatabase.connect();
    // System.out.println("OP\tt\t" + this);
    return this.geodatabase.openTable(this.catalogPath);
  }

  void withTableLock(final Runnable action) {
    try (
      BaseCloseable lock = writeLock(false)) {

      final Table table = getValue();
      if (table != null) {
        try {
          action.run();
        } finally {
          disconnect();
        }
      }
    }
  }

  <V> V withTableLock(final Supplier<V> action) {
    try (
      BaseCloseable lock = writeLock(false)) {

      final Table table = getValue();
      if (table != null) {
        try {
          return action.get();
        } finally {
          disconnect();
        }
      }
    }
    return null;
  }

  synchronized TableWrapper writeLock(final boolean loadOnlyMode) {
    final Table table = getValue();
    final boolean locked = this.lockCount > 0;
    this.lockCount++;
    if (!locked) {
      this.recordStore.lockTable(table);

    }
    if (loadOnlyMode) {
      if (++this.loadOnlyCount == 1) {
        table.setLoadOnlyMode(true);
      }
      return this.lockerLoadOnly;
    } else {
      return this.locker;
    }
  }

  private synchronized void writeUnlock(final boolean loadOnly) {
    try {
      if (!isClosed()) {
        final Table table = this.value;
        if (loadOnly) {
          final boolean wasLoadOnly = this.loadOnlyCount > 0;
          this.loadOnlyCount--;
          if (this.loadOnlyCount <= 0) {
            this.loadOnlyCount = 0;
            if (table != null && wasLoadOnly) {
              table.setLoadOnlyMode(false);
            }
          }
        }

        final boolean wasLocked = this.lockCount > 0;
        this.lockCount--;
        if (this.lockCount <= 0) {
          this.lockCount = 0;
          if (table != null && wasLocked) {
            this.recordStore.unlockTable(table);
          }
        }
      }
    } catch (final Exception e) {
      Logs.error(this, "Unable to free write lock for table: " + this, e);
    } finally {
      disconnect();
    }
  }
}
