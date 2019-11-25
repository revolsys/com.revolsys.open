package com.revolsys.gis.esri.gdb.file;

import java.util.function.Consumer;
import java.util.function.Function;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.beans.ObjectException;
import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Geodatabase;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.io.BaseCloseable;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.util.CloseableValueHolder;
import com.revolsys.util.ValueHolder;
import com.revolsys.util.ValueWrapper;

public class TableReference extends CloseableValueHolder<Table> {
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

  private final ValueWrapper<Geodatabase> geodatabase;

  private ValueWrapper<Geodatabase> geodatabaseClosable;

  private final FileGdbRecordStore recordStore;

  private final PathName pathName;

  private final FileGdbRecordDefinition recordDefinition;

  TableReference(final FileGdbRecordStore recordStore,
    final FileGdbRecordDefinition recordDefinition, final ValueWrapper<Geodatabase> geodatabase,
    final PathName pathName, final String catalogPath) {
    this.recordStore = recordStore;
    this.recordDefinition = recordDefinition;
    this.geodatabase = geodatabase;
    this.pathName = pathName;
    this.catalogPath = catalogPath;
  }

  @Override
  public void closeAfter() {
    final ValueWrapper<Geodatabase> geodatabaseClosable = this.geodatabaseClosable;
    this.geodatabaseClosable = null;
    if (geodatabaseClosable != null) {
      geodatabaseClosable.close();
    }
  }

  EnumRows closeRows(final EnumRows rows) {
    if (rows != null) {
      this.geodatabase.valueConsumeSync(g -> {
        try {
          rows.Close();
        } finally {
          rows.delete();
        }
      });
    }
    return null;
  }

  @Override
  public TableWrapper connect() {
    return (TableWrapper)super.connect();
  }

  boolean deleteRecordRow(final Record record) {
    final Integer objectId = record.getInteger("OBJECTID");
    if (objectId != null) {
      // final String whereClause = "OBJECTID=" + objectId;
      synchronized (this.geodatabase) {
        // final Table table = getValue();
        try (
          BaseCloseable lock = writeLock(false)) {
          final String tableName = JdbcUtils.getQualifiedTableName(this.recordDefinition.getPath());
          final String deleteSql = "DELETE FROM " + tableName + " WHERE OBJECTID=" + objectId;
          final EnumRows rows = this.geodatabase.getValue().query(deleteSql, true);
          closeRows(rows);

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

  public PathName getPathName() {
    return this.pathName;
  }

  public FileGdbRecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public FileGdbRecordStore getRecordStore() {
    return this.recordStore;
  }

  synchronized boolean isLocked() {
    return this.lockCount >= 0;
  }

  @Override
  protected TableWrapper newCloseable() {
    return new EsriFileGdbTableConnection();
  }

  EnumRows query(final String sql, final boolean recycling) {
    return this.geodatabase.valueFunctionSync(geodatabase -> geodatabase.query(sql, recycling));
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
                table.updateRow(row);
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
              closeRows(rows);
            } finally {
              disconnect();
            }
          }
        }
      }
    }
    return false;
  }

  void validateRequired(final Record record) {
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
      this.recordStore.threadGeodatabaseResult(geodatabase -> {
        try {
          geodatabase.closeTable(table);
        } catch (final Exception e) {
          Logs.error(this, "Unable to close table: " + this.catalogPath, e);
        } finally {
          table.delete();
        }
        return null;
      });
    } finally {
      final ValueWrapper<Geodatabase> geodatabaseClosable = this.geodatabaseClosable;
      this.geodatabaseClosable = null;
      if (geodatabaseClosable != null) {
        geodatabaseClosable.close();
      }
    }
  }

  @Override
  public synchronized void valueConsumeSync(final Consumer<Table> action) {
    synchronized (this.geodatabase) {
      super.valueConsumeSync(action);
    }
  }

  @Override
  public synchronized <V> V valueFunctionSync(final Function<Table, V> action) {
    synchronized (this.geodatabase) {
      return valueFunction(action);
    }
  }

  @Override
  public synchronized <V> V valueFunctionSync(final Function<Table, V> action,
    final V defaultValue) {
    synchronized (this.geodatabase) {
      return valueFunction(action, defaultValue);
    }
  }

  @Override
  protected Table valueNew() {
    this.geodatabaseClosable = this.geodatabase.connect();
    return this.recordStore
      .threadGeodatabaseResult(geodatabase -> geodatabase.openTable(this.catalogPath));
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
