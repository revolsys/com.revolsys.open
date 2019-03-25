package com.revolsys.gis.esri.gdb.file;

import java.util.function.Consumer;
import java.util.function.Function;

import com.revolsys.esri.filegdb.jni.EnumRows;
import com.revolsys.esri.filegdb.jni.Geodatabase;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.io.PathName;
import com.revolsys.logging.Logs;
import com.revolsys.util.CloseableValueHolder;
import com.revolsys.util.ValueWrapper;

public class TableReference extends CloseableValueHolder<Table> {

  private final TableWrapper closeable = new TableWrapper() {

    @Override
    public void close() {
      disconnect();
    }

    @Override
    public TableReference getTableReference() {
      return TableReference.this;
    }
  };

  private final TableWrapper locker = new TableWrapper() {

    @Override
    public void close() {
      synchronized (TableReference.this) {
        if (!isClosed()) {
          TableReference.this.lockCount--;
          if (TableReference.this.lockCount <= 0) {
            TableReference.this.lockCount = 0;
            try {
              final Table table = TableReference.this.value;
              if (table != null) {
                synchronized (table) {
                  setLoadOnlyMode(false);
                  table.freeWriteLock();
                }
              }
            } catch (final Exception e) {
              Logs.error(this,
                "Unable to free write lock for table: " + TableReference.this.catalogPath, e);
            }
          }
        }
      }
      disconnect();
    }

    @Override
    public TableReference getTableReference() {
      return TableReference.this;
    }
  };

  private int lockCount = 0;

  private final String catalogPath;

  private final ValueWrapper<Geodatabase> geodatabase;

  private final FileGdbRecordStore recordStore;

  private final PathName pathName;

  TableReference(final FileGdbRecordStore recordStore, final ValueWrapper<Geodatabase> geodatabase,
    final PathName pathName, final String catalogPath) {
    this.recordStore = recordStore;
    this.geodatabase = geodatabase.connect();
    this.pathName = pathName;
    this.catalogPath = catalogPath;
  }

  @Override
  public void closeAfter() {
    this.geodatabase.close();
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

  public PathName getPathName() {
    return this.pathName;
  }

  public FileGdbRecordStore getRecordStore() {
    return this.recordStore;
  }

  synchronized boolean isLocked() {
    return this.lockCount >= 0;
  }

  Row nextRow(final EnumRows rows) {
    if (rows == null) {
      return null;
    } else {
      synchronized (this.geodatabase) {
        return rows.next();
      }
    }
  }

  FileGdbEnumRowsIterator query(final String sql, final boolean recycling) {
    final TableWrapper table = connect();
    final EnumRows rows = this.geodatabase
      .valueFunctionSync(geodatabase -> geodatabase.query(sql, recycling));
    if (rows == null) {
      table.close();
      return null;
    } else {
      return new FileGdbEnumRowsIterator(table, rows);
    }
  }

  synchronized void setLoadOnlyMode(final boolean loadOnly) {
    // table.setLoadOnlyMode(loadOnly);
  }

  @Override
  public String toString() {
    return this.catalogPath;
  }

  @Override
  protected void valueClose(final Table table) {
    this.geodatabase.valueConsumeSync(geodatabase -> {
      try {
        geodatabase.closeTable(table);
      } catch (final Exception e) {
        Logs.error(this, "Unable to close table: " + this.catalogPath, e);
      } finally {
        table.delete();

      }
    });
  }

  @Override
  protected TableWrapper valueConnectCloseable() {
    return this.closeable;
  }

  @Override
  public void valueConsumeSync(final Consumer<Table> action) {
    synchronized (this.geodatabase) {
      super.valueConsumeSync(action);
    }
  }

  @Override
  public <V> V valueFunctionSync(final Function<Table, V> action) {
    synchronized (this.geodatabase) {
      return valueFunction(action);
    }
  }

  @Override
  public <V> V valueFunctionSync(final Function<Table, V> action, final V defaultValue) {
    synchronized (this.geodatabase) {
      return valueFunction(action, defaultValue);
    }
  }

  @Override
  protected Table valueNew() {
    return this.recordStore
      .threadGeodatabaseResult(geodatabase -> geodatabase.openTable(this.catalogPath));
  }

  synchronized TableWrapper writeLock() {
    final Table table = getValue();
    if (++this.lockCount == 1) {
      table.setWriteLock();
      setLoadOnlyMode(true);
    }
    return this.locker;
  }
}
