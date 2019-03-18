package com.revolsys.gis.esri.gdb.file;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.PathName;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class FileGdbWriter extends AbstractRecordWriter {
  private FileGdbRecordStore recordStore;

  private Map<String, TableReference> tablesByCatalogPath = new HashMap<>();

  private RecordDefinition recordDefinition;

  private TableReference table;

  private PathName pathName;

  FileGdbWriter(final FileGdbRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  FileGdbWriter(final FileGdbRecordStore recordStore, final RecordDefinition recordDefinition) {
    this.recordStore = recordStore;
    if (recordDefinition != null) {
      this.pathName = recordDefinition.getPathName();
      this.table = getTable(recordDefinition);
      this.recordDefinition = recordDefinition;
    }
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    try {
      if (this.tablesByCatalogPath != null) {
        for (final TableReference table : this.tablesByCatalogPath.values()) {
          table.close();
        }
      }
    } finally {
      this.tablesByCatalogPath = null;
      this.recordStore = null;
    }
  }

  public synchronized void closeTable(final PathName typePath) {
    if (this.tablesByCatalogPath != null) {
      final String catalogPath = this.recordStore.getCatalogPath(typePath);
      final TableReference table = this.tablesByCatalogPath.remove(catalogPath);
      if (table != null) {
        // TODO release write lock
        table.close();
      }
    }
  }

  private void deleteRecord(final Record record) {
    final TableReference table = getTable(record);
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

  private TableReference getTable(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return getTable(recordDefinition);
  }

  private TableReference getTable(final RecordDefinition recordDefinition) {
    if (this.recordDefinition != null) {
      if (this.recordDefinition == recordDefinition) {
        return this.table;
      } else if (recordDefinition.getPathName().equals(this.pathName)) {
        return this.table;
      }
    }
    final String catalogPath = this.recordStore.getCatalogPath(recordDefinition);
    synchronized (this.tablesByCatalogPath) {
      TableReference table = this.tablesByCatalogPath.get(catalogPath);
      if (table == null) {
        table = this.recordStore.getTable(recordDefinition);
        // TODO lock
        if (table != null) {
          this.tablesByCatalogPath.put(catalogPath, table);
        }
      }
      return table;
    }
  }

  private void insertRecord(final Record record) {
    final TableReference table = getTable(record);
    if (table != null) {
      table.insertRecord(record);
    }
  }

  public boolean isClosed() {
    return this.recordStore == null;
  }

  private void updateRecord(final Record record) {
    final TableReference table = getTable(record);
    if (table != null) {
      table.updateRecord(record);
    }
  }

  @Override
  public synchronized void write(final Record record) {
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
