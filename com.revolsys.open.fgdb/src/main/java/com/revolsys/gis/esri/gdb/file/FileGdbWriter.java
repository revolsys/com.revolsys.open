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
  private FileGdbRecordStore recordStore;

  private Map<String, Table> tables = new HashMap<>();

  FileGdbWriter(final FileGdbRecordStore recordStore) {
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

  public synchronized void closeTable(final String typePath) {
    if (this.tables != null) {
      if (this.tables.remove(typePath) != null) {
        this.recordStore.releaseTableAndWriteLock(typePath);
      }
    }
  }

  private void delete(final Record record) {
    final Object objectId = record.getValue("OBJECTID");
    if (objectId != null) {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final String typePath = recordDefinition.getPath();
      final Table table = getTable(typePath);
      final String whereClause = "OBJECTID=" + objectId;
      final EnumRows rows = this.recordStore.search(typePath, table, "OBJECTID", whereClause,
        false);
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
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  private synchronized Table getTable(final String typePath) {
    Table table = this.tables.get(typePath);
    if (table == null) {
      table = this.recordStore.getTableWithWriteLock(typePath);
      if (table != null) {
        this.tables.put(typePath, table);
      }
    }
    return table;
  }

  private void insert(final Record record) {
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = this.recordStore
      .getRecordDefinition(sourceRecordDefinition);

    validateRequired(record, recordDefinition);

    final String typePath = sourceRecordDefinition.getPath();
    final Table table = getTable(typePath);
    try {
      final Row row = this.recordStore.createRowObject(table);
      try {
        final List<Object> values = new ArrayList<>();
        for (final FieldDefinition field : recordDefinition.getFields()) {
          final String name = field.getName();
          try {
            final Object value = record.getValue(name);
            final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
            final Object esriValue = esriField.setInsertValue(record, row, value);
            values.add(esriValue);
          } catch (final Throwable e) {
            throw new ObjectPropertyException(record, name, e);
          }
        }
        this.recordStore.insertRow(table, row);
        if (sourceRecordDefinition == recordDefinition) {
          for (final FieldDefinition field : recordDefinition.getFields()) {
            final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
            try {
              esriField.setPostInsertValue(record, row);
            } catch (final Throwable e) {
              throw new ObjectPropertyException(record, field.getName(), e);
            }
          }
          record.setState(RecordState.Persisted);
        }
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
      final RecordDefinition recordDefinition = this.recordStore
        .getRecordDefinition(sourceRecordDefinition);

      validateRequired(record, recordDefinition);

      final String typePath = sourceRecordDefinition.getPath();
      final Table table = getTable(typePath);
      final String whereClause = "OBJECTID=" + objectId;
      final EnumRows rows = this.recordStore.search(typePath, table, "*", whereClause, true);
      if (rows != null) {
        try {
          final Row row = this.recordStore.nextRow(rows);
          if (row != null) {
            try {
              final List<Object> esriValues = new ArrayList<>();
              try {
                for (final FieldDefinition field : recordDefinition.getFields()) {
                  final String name = field.getName();
                  try {
                    final Object value = record.getValue(name);
                    final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
                    final Object esriValue = esriField.setUpdateValue(record, row, value);
                    esriValues.add(esriValue);
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
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String name = field.getName();
      if (field.isRequired()) {
        final Object value = record.getValue(name);
        if (value == null && !(field instanceof OidFieldDefinition)) {
          throw new ObjectPropertyException(record, name, "Value required");
        }
      }
    }
  }

  @Override
  public synchronized void write(final Record record) {
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
  }
}
