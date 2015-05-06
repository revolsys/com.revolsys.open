package com.revolsys.gis.esri.gdb.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import com.revolsys.beans.ObjectException;
import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.OidFieldDefinition;
import com.revolsys.io.AbstractRecordWriter;

public class FileGdbWriter extends AbstractRecordWriter {
  private Map<String, Table> tables = new HashMap<>();

  private CapiFileGdbRecordStore recordStore;

  FileGdbWriter(final CapiFileGdbRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    try {
      if (this.tables != null) {
        for (final String typePath : this.tables.keySet()) {
          this.recordStore.releaseTableAndWriteLock(typePath);
        }
      }
    } finally {
      this.tables = null;
      this.recordStore = null;
    }
  }

  public void closeTable(final String typePath) {
    if (this.tables != null) {
      synchronized (this.tables) {
        if (this.tables.remove(typePath) != null) {
          this.recordStore.releaseTableAndWriteLock(typePath);
        }
      }
    }
  }

  private void delete(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    final Table table = getTable(typePath);
    final EnumRows rows = this.recordStore.search(typePath, table, "OBJECTID",
      "OBJECTID=" + record.getValue("OBJECTID"), false);
    if (rows != null) {
      try {
        final Row row = this.recordStore.nextRow(rows);
        if (row != null) {
          try {
            this.recordStore.deleteRow(typePath, table, row);
            record.setState(RecordState.Deleted);
          } finally {
            this.recordStore.closeRow(row);
            this.recordStore.addStatistic("Delete", record);
          }
        }
      } finally {
        this.recordStore.closeEnumRows(rows);
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  private Table getTable(final String typePath) {
    synchronized (this) {
      Table table = this.tables.get(typePath);
      if (table == null) {
        table = this.recordStore.getTableWithWriteLock(typePath);
        if (table != null) {
          this.tables.put(typePath, table);
        }
      }
      return table;
    }
  }

  private void insert(final Record record) {
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(sourceRecordDefinition);

    validateRequired(record, recordDefinition);

    final String typePath = sourceRecordDefinition.getPath();
    final Table table = getTable(typePath);
    try {
      final Row row = this.recordStore.createRowObject(table);
      try {
        final List<Object> values = new ArrayList<Object>();
        for (final FieldDefinition attribute : recordDefinition.getFields()) {
          final String name = attribute.getName();
          try {
            final Object value = record.getValue(name);
            final AbstractFileGdbFieldDefinition esriAttribute = (AbstractFileGdbFieldDefinition)attribute;
            final Object esriValue = esriAttribute.setInsertValue(record, row, value);
            values.add(esriValue);
          } catch (final Throwable e) {
            throw new ObjectPropertyException(record, name, e);
          }
        }
        this.recordStore.insertRow(table, row);
        for (final FieldDefinition attribute : recordDefinition.getFields()) {
          final AbstractFileGdbFieldDefinition esriAttribute = (AbstractFileGdbFieldDefinition)attribute;
          try {
            esriAttribute.setPostInsertValue(record, row);
          } catch (final Throwable e) {
            throw new ObjectPropertyException(record, attribute.getName(), e);
          }
        }
        record.setState(RecordState.Persisted);
      } finally {
        this.recordStore.closeRow(row);
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

  public synchronized void openTable(final String typePath) {
    getTable(typePath);
  }

  private void update(final Record record) {
    final Object objectId = record.getValue("OBJECTID");
    if (objectId == null) {
      insert(record);
    } else {
      final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
      final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(sourceRecordDefinition);

      validateRequired(record, recordDefinition);

      final String typePath = sourceRecordDefinition.getPath();
      final Table table = getTable(typePath);
      final EnumRows rows = this.recordStore.search(typePath, table, "OBJECTID", "OBJECTID="
        + objectId, false);
      if (rows != null) {
        try {
          final Row row = this.recordStore.nextRow(rows);
          if (row != null) {
            try {
              final List<Object> esriValues = new ArrayList<Object>();
              try {
                for (final FieldDefinition attribute : recordDefinition.getFields()) {
                  final String name = attribute.getName();
                  try {
                    final Object value = record.getValue(name);
                    final AbstractFileGdbFieldDefinition esriAttribute = (AbstractFileGdbFieldDefinition)attribute;
                    esriValues.add(esriAttribute.setUpdateValue(record, row, value));
                  } catch (final Throwable e) {
                    throw new ObjectPropertyException(record, name, e);
                  }
                }
                this.recordStore.updateRow(typePath, table, row);
              } finally {
                this.recordStore.addStatistic("Update", record);
              }
            } catch (final ObjectException e) {
              if (e.getObject() == record) {
                throw e;
              } else {
                throw new ObjectException(record, e);
              }
            } catch (final Throwable e) {
              throw new ObjectException(record, e);
            } finally {
              this.recordStore.closeRow(row);
            }
          }
        } finally {
          this.recordStore.closeEnumRows(rows);
        }
      }
    }
  }

  private void validateRequired(final Record record, final RecordDefinition recordDefinition) {
    for (final FieldDefinition attribute : recordDefinition.getFields()) {
      final String name = attribute.getName();
      if (attribute.isRequired()) {
        final Object value = record.getValue(name);
        if (value == null && !(attribute instanceof OidFieldDefinition)) {
          throw new ObjectPropertyException(record, name, "Value required");
        }
      }
    }
  }

  @Override
  public synchronized void write(final Record record) {
    try {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final RecordStore recordStore = recordDefinition.getRecordStore();
      if (recordStore == this.recordStore) {
        switch (record.getState()) {
          case New:
            insert(record);
          break;
          case Modified:
            update(record);
          break;
          case Persisted:
          // No action required
          break;
          case Deleted:
            delete(record);
          break;
          default:
            throw new IllegalStateException("State not known");
        }
      } else {
        insert(record);
      }
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to write", e);
    }
  }
}
