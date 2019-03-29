package com.revolsys.gis.esri.gdb.file;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.PathName;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class FileGdbWriter extends AbstractRecordWriter {
  private FileGdbRecordStore recordStore;

  private final Map<String, TableWrapper> tablesByCatalogPath = new HashMap<>();

  private RecordDefinition recordDefinition;

  private TableWrapper table;

  private PathName pathName;

  FileGdbWriter(final FileGdbRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  FileGdbWriter(final FileGdbRecordStore recordStore, final RecordDefinition recordDefinition) {
    this.recordStore = recordStore;
    if (recordDefinition != null) {
      this.pathName = recordDefinition.getPathName();
      this.table = recordStore.getTableLocked(recordDefinition);
      this.recordDefinition = recordDefinition;
    }
  }

  @Override
  public void close() {
    try {
      synchronized (this.tablesByCatalogPath) {
        for (final TableWrapper table : this.tablesByCatalogPath.values()) {
          table.close();
        }
      }
      final TableWrapper table = this.table;
      this.table = null;
      if (table != null) {
        table.close();
      }
    } finally {
      this.tablesByCatalogPath.clear();
      this.recordStore = null;
    }
  }

  public void closeTable(final PathName typePath) {
    synchronized (this.tablesByCatalogPath) {
      final String catalogPath = this.recordStore.getCatalogPath(typePath);
      final TableWrapper table = this.tablesByCatalogPath.remove(catalogPath);
      if (table != null) {
        table.close();
      }
    }
  }

  private void deleteRecord(final Record record) {
    final TableWrapper table = getTable(record);
    if (table != null) {
      table.deleteRecord(record);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public FileGdbRecordStore getRecordStore() {
    return this.recordStore;
  }

  private TableWrapper getTable(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return getTable(recordDefinition);
  }

  private TableWrapper getTable(final RecordDefinition recordDefinition) {
    if (this.recordDefinition != null) {
      if (this.recordDefinition == recordDefinition) {
        return this.table;
      } else if (recordDefinition.getPathName().equals(this.pathName)) {
        return this.table;
      }
    }
    final String catalogPath = this.recordStore.getCatalogPath(recordDefinition);
    synchronized (this.tablesByCatalogPath) {
      TableWrapper table = this.tablesByCatalogPath.get(catalogPath);
      if (table == null) {
        table = this.recordStore.getTableLocked(recordDefinition);
        // TODO lock
        if (table != null) {
          this.tablesByCatalogPath.put(catalogPath, table);
        }
      }
      return table;
    }
  }

  private void insertRecord(final Record record) {
    final TableWrapper table = getTable(record);
    if (table != null) {
      table.insertRecord(record);
    }
  }

  public boolean isClosed() {
    return this.recordStore == null;
  }

  @Override
  public String toString() {
    if (this.pathName == null) {
      return this.recordStore.toString();
    } else {
      return this.pathName + "\n" + this.recordStore;
    }
  }

  private void updateRecord(final Record record) {
    final TableWrapper table = getTable(record);
    if (table != null) {
      table.updateRecord(record);
    }
  }

  @Override
  public void write(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    if (recordStore == this.recordStore) {
      switch (record.getState()) {
        case NEW:
          insertRecord(record);
        break;
        case MODIFIED:
          updateRecord(record);
        break;
        case PERSISTED:
        // No action required
        break;
        case DELETED:
          deleteRecord(record);
        break;
        default:
          throw new IllegalStateException("State not known");
      }
    } else {
      insertRecord(record);
    }
  }
}
