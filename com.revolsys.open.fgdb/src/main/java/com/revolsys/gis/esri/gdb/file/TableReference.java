package com.revolsys.gis.esri.gdb.file;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Geodatabase;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
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
    @Override
    public void close() {
      writeUnlock();
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

  private final TableWrapper locker = new EsriFileGdbTableLock();

  private int lockCount = 0;

  private final String catalogPath;

  private final ValueWrapper<Geodatabase> geodatabase;

  private ValueWrapper<Geodatabase> geodatabaseClosable;

  private final FileGdbRecordStore recordStore;

  private final PathName pathName;

  TableReference(final FileGdbRecordStore recordStore, final ValueWrapper<Geodatabase> geodatabase,
    final PathName pathName, final String catalogPath) {
    this.recordStore = recordStore;
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

  public String getCatalogPath() {
    return this.catalogPath;
  }

  public PathName getPathName() {
    return this.pathName;
  }

  public FileGdbRecordStore getRecordStore() {
    return this.recordStore;
  }

  synchronized boolean isLocked() {
    return this.lockCount >= 0;
  }

  boolean modifyRecordRow(final Record record, String fieldSpec, final BiConsumer<Table, Row> action) {
    final Integer objectId = record.getInteger("OBJECTID");
    if (objectId != null) {
      final String whereClause = "OBJECTID=" + objectId;
      return valueFunctionSync(table -> {
        try (
          BaseCloseable lock = writeLock(false)) {
          final EnumRows rows = table.search(fieldSpec, whereClause, false);
          try {
            final Row row = rows.next();
            if (row != null) {
              action.accept(table, row);
              row.delete();
              return true;
            }
          } finally {
            closeRows(rows);
          }
        }
        return false;
      }, false);
    }
    return false;
  }

  @Override
  protected TableWrapper newCloseable() {
    return new EsriFileGdbTableConnection();
  }

  synchronized EnumRows query(final String sql, final boolean recycling) {
    return this.geodatabase.valueFunctionSync(geodatabase -> geodatabase.query(sql, recycling));
  }

  synchronized void setLoadOnlyMode(final boolean loadOnly) {
    // table.setLoadOnlyMode(loadOnly);
  }

  @Override
  public String toString() {
    return this.recordStore.getFileName() + "\t" + this.catalogPath;
  }

  @Override
  protected void valueClose(final Table table) {
    try {
      this.geodatabase.valueConsumeSync(geodatabase -> {
        try {
          geodatabase.closeTable(table);
        } catch (final Exception e) {
          Logs.error(this, "Unable to close table: " + this.catalogPath, e);
        } finally {
          table.delete();
        }
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
      if (loadOnlyMode) {
        table.setLoadOnlyMode(true);
      }
    }
    return this.locker;
  }

  private synchronized void writeUnlock() {
    try {
      if (!isClosed()) {
        final boolean locked = this.lockCount > 0;
        this.lockCount--;
        if (this.lockCount <= 0) {
          this.lockCount = 0;
          final Table table = this.value;
          if (table != null && locked) {
            table.setLoadOnlyMode(false);
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
