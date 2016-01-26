package com.revolsys.gis.esri.gdb.file;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.PathName;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class FileGdbWriter extends AbstractRecordWriter {
  private FileGdbRecordStore recordStore;

  private Map<String, Table> tablesByCatalogPath = new HashMap<>();

  private RecordDefinition recordDefinition;

  FileGdbWriter(final FileGdbRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  FileGdbWriter(final FileGdbRecordStore recordStore, final RecordDefinition recordDefinition) {
    this.recordStore = recordStore;
    this.recordDefinition = recordDefinition;
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    try {
      if (this.tablesByCatalogPath != null) {
        for (final String catalogPath : this.tablesByCatalogPath.keySet()) {
          this.recordStore.releaseTableAndWriteLock(catalogPath);
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
      if (this.tablesByCatalogPath.remove(catalogPath) != null) {
        this.recordStore.releaseTableAndWriteLock(catalogPath);
      }
    }
  }

  private void deleteRecord(final Record record) {
    final Table table = getTable(record);
    this.recordStore.deleteRecord(table, record);
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  private Table getTable(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final String catalogPath = this.recordStore.getCatalogPath(recordDefinition);
    Table table = this.tablesByCatalogPath.get(catalogPath);
    if (table == null) {
      table = this.recordStore.getTableWithWriteLock(recordDefinition);
      if (table != null) {
        this.tablesByCatalogPath.put(catalogPath, table);
      }
    }
    return table;
  }

  private void insertRecord(final Record record) {
    final Table table = getTable(record);
    this.recordStore.insertRecord(table, record);
  }

  public boolean isClosed() {
    return this.recordStore == null;
  }

  private void updateRecord(final Record record) {
    final Table table = getTable(record);
    this.recordStore.updateRecord(table, record);
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
