package com.revolsys.gis.esri.gdb.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.io.PathName;
import org.jeometry.common.io.PathNameProxy;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class FileGdbWriter extends AbstractRecordWriter {
  private FileGdbRecordStore recordStore;

  private final Map<PathName, TableWrapper> tablesByPathName = new HashMap<>();

  private FileGdbRecordDefinition fileGdbRecordDefinition;

  private TableWrapper table;

  private final boolean loadOnlyMode;

  private final List<TableWrapper> tables = new ArrayList<>();

  FileGdbWriter(final FileGdbRecordStore recordStore) {
    super(null);
    this.recordStore = recordStore;
    this.loadOnlyMode = false;
  }

  FileGdbWriter(final FileGdbRecordStore recordStore, final RecordDefinitionProxy recordDefinition,
    final FileGdbRecordDefinition fileGdbRecordDefinition, final boolean loadOnlyMode) {
    super(recordDefinition);
    this.recordStore = recordStore;
    this.loadOnlyMode = loadOnlyMode;
    if (recordDefinition != null) {
      this.fileGdbRecordDefinition = fileGdbRecordDefinition;
      final PathName pathName = recordDefinition.getPathName();
      final PathName fileGdbPathName = fileGdbRecordDefinition.getPathName();
      this.table = fileGdbRecordDefinition.lockTable(loadOnlyMode);
      this.tables.add(this.table);
      this.tablesByPathName.put(pathName, this.table);
      this.tablesByPathName.put(fileGdbPathName, this.table);
    }
  }

  @Override
  public void close() {
    synchronized (this.tablesByPathName) {
      try {
        for (final TableWrapper table : this.tables) {
          table.close();
        }
      } finally {
        this.fileGdbRecordDefinition = null;
        this.tablesByPathName.clear();
        this.tables.clear();
        this.recordStore = null;
        this.table = null;
      }
    }
  }

  public void closeTable(final PathName pathName) {
    if (pathName != null) {
      synchronized (this.tablesByPathName) {
        final TableWrapper table = this.tablesByPathName.remove(pathName);
        if (table != null) {
          if (table == this.table) {
            this.table = null;
            this.fileGdbRecordDefinition = null;
          }
          this.tables.remove(table);
          table.close();
          this.tablesByPathName.values().remove(table);
        }
      }
    }
  }

  public void closeTable(final PathNameProxy pathName) {
    if (pathName != null) {
      closeTable(pathName.getPathName());
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  public FileGdbRecordStore getRecordStore() {
    return this.recordStore;
  }

  private TableWrapper getTable(final RecordDefinition recordDefinition) {
    synchronized (this.tablesByPathName) {
      if (recordDefinition == null) {
        return null;
      } else if (this.table != null) {
        if (getRecordDefinition() == recordDefinition //
          || this.fileGdbRecordDefinition == recordDefinition //
        ) {
          return this.table;
        }
      }
      final PathName pathName = recordDefinition.getPathName();
      TableWrapper table = this.tablesByPathName.get(pathName);
      if (table == null) {
        final FileGdbRecordDefinition fileGdbRecordDefinition = this.recordStore
          .getRecordDefinition(recordDefinition);
        if (fileGdbRecordDefinition == null) {
          throw new IllegalArgumentException("Tables doesn't exist " + pathName);
        } else {
          final PathName fileGdbPathName = fileGdbRecordDefinition.getPathName();
          if (!fileGdbPathName.equals(pathName)) {
            table = this.tablesByPathName.get(fileGdbPathName);
            if (table != null) {
              this.tablesByPathName.put(pathName, table);
              return table;
            }
          }
          table = this.recordStore.getTableLocked(recordDefinition, this.loadOnlyMode);
          this.tables.add(table);
          this.tablesByPathName.put(pathName, table);
          this.tablesByPathName.put(fileGdbPathName, table);
        }
      }
      return table;
    }
  }

  public boolean isClosed() {
    return this.recordStore == null;
  }

  @Override
  public String toString() {
    if (this.fileGdbRecordDefinition == null) {
      return this.recordStore.toString();
    } else {
      return this.fileGdbRecordDefinition.getPathName() + "\n" + this.recordStore;
    }
  }

  @Override
  public void write(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final TableWrapper table = getTable(recordDefinition);
    if (table == null) {
      throw new IllegalArgumentException(
        getRecordStore() + " doesn't have table " + recordDefinition);
    } else {
      if (recordDefinition.equalsRecordStore(this.recordStore)) {
        switch (record.getState()) {
          case NEW:
            table.insertRecord(record);
          break;
          case MODIFIED:
            table.updateRecord(record);
          break;
          case PERSISTED:
          // No action required
          break;
          case DELETED:
            table.deleteRecord(record);
          break;
          default:
            throw new IllegalStateException("State not known");
        }
      } else {
        table.insertRecord(record);
      }
    }
  }

}
