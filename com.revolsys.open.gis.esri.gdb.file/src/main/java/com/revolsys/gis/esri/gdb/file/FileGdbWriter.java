package com.revolsys.gis.esri.gdb.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.OidAttribute;
import com.revolsys.io.AbstractWriter;

public class FileGdbWriter extends AbstractWriter<Record> {
  private Map<String, Table> tables = new HashMap<String, Table>();

  private CapiFileGdbRecordStore recordStore;

  FileGdbWriter(final CapiFileGdbRecordStore recordStore) {
    this.recordStore = recordStore;
  }

  @Override
  @PreDestroy
  public void close() {
    try {
      if (recordStore != null) {
        for (final Entry<String, Table> entry : tables.entrySet()) {
          final Table table = entry.getValue();
          try {
            recordStore.freeWriteLock(table);
          } catch (final Throwable e) {
            LoggerFactory.getLogger(FileGdbWriter.class).error(
              "Unable to close table", e);
          }
        }
      }
    } finally {
      this.tables = null;
      this.recordStore = null;
    }
  }

  private void delete(final Record object) {
    final RecordDefinition objectMetaData = object.getRecordDefinition();
    final String typePath = objectMetaData.getPath();
    final Table table = getTable(typePath);
    final EnumRows rows = recordStore.search(table, "OBJECTID", "OBJECTID="
      + object.getValue("OBJECTID"), false);
    if (rows != null) {
      try {
        final Row row = recordStore.nextRow(rows);
        if (row != null) {
          try {
            recordStore.deletedRow(table, row);
            object.setState(RecordState.Deleted);
          } finally {
            recordStore.closeRow(row);
            recordStore.addStatistic("Delete", object);
          }
        }
      } finally {
        recordStore.closeEnumRows(rows);
      }
    }
  }

  private Table getTable(final String typePath) {
    Table table = tables.get(typePath);
    if (table == null) {
      table = recordStore.getTable(typePath);
      if (table != null) {
        tables.put(typePath, table);
        recordStore.setWriteLock(table);
      }
    }
    return table;
  }

  private void insert(final Record record) {
    final RecordDefinition sourceMetaData = record.getRecordDefinition();
    final RecordDefinition recordDefinition = recordStore.getRecordDefinition(sourceMetaData);
    final String typePath = sourceMetaData.getPath();
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
      final Row row = recordStore.createRowObject(table);
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
        recordStore.insertRow(table, row);
        for (final Attribute attribute : recordDefinition.getAttributes()) {
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setPostInsertValue(record, row);
        }
        record.setState(RecordState.Persisted);
      } finally {
        recordStore.closeRow(row);
        recordStore.addStatistic("Insert", record);
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

  private void update(final Record object) {
    final Object objectId = object.getValue("OBJECTID");
    if (objectId == null) {
      insert(object);
    } else {
      final RecordDefinition sourceMetaData = object.getRecordDefinition();
      final RecordDefinition recordDefinition = recordStore.getRecordDefinition(sourceMetaData);
      final String typePath = sourceMetaData.getPath();
      final Table table = getTable(typePath);
      final EnumRows rows = recordStore.search(table, "OBJECTID", "OBJECTID="
        + objectId, false);
      if (rows != null) {
        try {
          final Row row = recordStore.nextRow(rows);
          if (row != null) {
            try {
              final List<Object> esriValues = new ArrayList<Object>();
              try {
                for (final Attribute attribute : recordDefinition.getAttributes()) {
                  final String name = attribute.getName();
                  final Object value = object.getValue(name);
                  final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
                  esriValues.add(esriAttribute.setUpdateValue(object, row,
                    value));
                }
                recordStore.updateRow(table, row);
              } finally {
                recordStore.addStatistic("Update", object);
              }
            } catch (final IllegalArgumentException e) {
              LoggerFactory.getLogger(FileGdbWriter.class).error(
                "Unable to update row " + e.getMessage() + "\n"
                  + object.toString(), e);
            } catch (final RuntimeException e) {
              LoggerFactory.getLogger(FileGdbWriter.class).error(
                "Unable to update row \n:" + object.toString());
              if (LoggerFactory.getLogger(FileGdbWriter.class).isDebugEnabled()) {
                LoggerFactory.getLogger(FileGdbWriter.class).debug(
                  "Unable to update row \n:" + object.toString());
              }
              throw new RuntimeException("Unable to update row", e);
            } finally {
              recordStore.closeRow(row);
            }
          }
        } finally {
          recordStore.closeEnumRows(rows);
        }
      }
    }
  }

  @Override
  public void write(final Record object) {
    try {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      final RecordStore recordStore = recordDefinition.getRecordStore();
      if (recordStore == this.recordStore) {
        switch (object.getState()) {
          case New:
            insert(object);
          break;
          case Modified:
            update(object);
          break;
          case Persisted:
          // No action required
          break;
          case Deleted:
            delete(object);
          break;
          default:
            throw new IllegalStateException("State not known");
        }
      } else {
        insert(object);
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
