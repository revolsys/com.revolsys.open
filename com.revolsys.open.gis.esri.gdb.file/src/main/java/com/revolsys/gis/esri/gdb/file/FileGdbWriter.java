package com.revolsys.gis.esri.gdb.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.OidAttribute;
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
      if (this.recordStore != null) {
        for (final String typePath : this.tables.keySet()) {
          try {
            this.recordStore.closeTable(typePath);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(FileGdbWriter.class).error(
              "Unable to close table:" + typePath, e);
          }
        }
      }
    } finally {
      this.tables = null;
      this.recordStore = null;
    }
  }

  public synchronized void closeTable(final String typeName) {
    if (this.tables != null) {
      synchronized (this.tables) {
        final Table table = this.tables.remove(typeName);
        if (table != null) {
          this.recordStore.closeTable(typeName);
        }
      }
    }
  }

  private void delete(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    final Table table = getTable(typePath);
    final EnumRows rows = this.recordStore.search(table, "OBJECTID",
      "OBJECTID=" + record.getValue("OBJECTID"), false);
    if (rows != null) {
      try {
        final Row row = this.recordStore.nextRow(rows);
        if (row != null) {
          try {
            this.recordStore.deletedRow(table, row);
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

  private Table getTable(final String typePath) {
    Table table = this.tables.get(typePath);
    if (table == null) {
      table = this.recordStore.getTable(typePath);
      if (table != null) {
        this.tables.put(typePath, table);
        this.recordStore.setWriteLock(typePath);
      }
    }
    return table;
  }

  private void insert(final Record record) {
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(sourceRecordDefinition);
    final String typePath = sourceRecordDefinition.getPath();
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      final String name = attribute.getName();
      if (attribute.isRequired()) {
        final Object value = record.getValue(name);
        if (value == null && !(attribute instanceof OidAttribute)) {
          throw new IllegalArgumentException("Atribute " + typePath + "."
              + name + " is required");
        }
      }
    }
    final Table table = getTable(typePath);
    try {
      final Row row = this.recordStore.createRowObject(table);
      try {
        final List<Object> values = new ArrayList<Object>();
        for (final Attribute attribute : recordDefinition.getAttributes()) {
          final String name = attribute.getName();
          final Object value = record.getValue(name);
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          final Object esriValue = esriAttribute.setInsertValue(record, row,
            value);
          values.add(esriValue);
        }
        this.recordStore.insertRow(table, row);
        for (final Attribute attribute : recordDefinition.getAttributes()) {
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setPostInsertValue(record, row);
        }
        record.setState(RecordState.Persisted);
      } finally {
        this.recordStore.closeRow(row);
        this.recordStore.addStatistic("Insert", record);
      }
    } catch (final IllegalArgumentException e) {
      throw new RuntimeException("Unable to insert row " + e.getMessage()
        + "\n" + record.toString(), e);
    } catch (final RuntimeException e) {
      if (LoggerFactory.getLogger(FileGdbWriter.class).isDebugEnabled()) {
        LoggerFactory.getLogger(FileGdbWriter.class).debug(
          "Unable to insert row \n:" + record.toString());
      }
      throw new RuntimeException("Unable to insert row", e);
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
      final String typePath = sourceRecordDefinition.getPath();
      final Table table = getTable(typePath);
      final EnumRows rows = this.recordStore.search(table, "OBJECTID",
        "OBJECTID=" + objectId, false);
      if (rows != null) {
        try {
          final Row row = this.recordStore.nextRow(rows);
          if (row != null) {
            try {
              final List<Object> esriValues = new ArrayList<Object>();
              try {
                for (final Attribute attribute : recordDefinition.getAttributes()) {
                  final String name = attribute.getName();
                  final Object value = record.getValue(name);
                  final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
                  esriValues.add(esriAttribute.setUpdateValue(record, row,
                    value));
                }
                this.recordStore.updateRow(table, row);
              } finally {
                this.recordStore.addStatistic("Update", record);
              }
            } catch (final IllegalArgumentException e) {
              LoggerFactory.getLogger(FileGdbWriter.class).error(
                "Unable to update row " + e.getMessage() + "\n"
                    + record.toString(), e);
            } catch (final RuntimeException e) {
              LoggerFactory.getLogger(FileGdbWriter.class).error(
                "Unable to update row \n:" + record.toString());
              if (LoggerFactory.getLogger(FileGdbWriter.class).isDebugEnabled()) {
                LoggerFactory.getLogger(FileGdbWriter.class).debug(
                  "Unable to update row \n:" + record.toString());
              }
              throw new RuntimeException("Unable to update row", e);
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
