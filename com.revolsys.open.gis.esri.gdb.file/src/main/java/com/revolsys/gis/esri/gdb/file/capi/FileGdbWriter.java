package com.revolsys.gis.esri.gdb.file.capi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbAttribute;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.AbstractWriter;

public class FileGdbWriter extends AbstractWriter<DataObject> {
  private static final Logger LOG = LoggerFactory.getLogger(FileGdbWriter.class);

  private Map<QName, Table> tables = new HashMap<QName, Table>();

  private final Geodatabase geodatabase;

  private CapiFileGdbDataObjectStore dataObjectStore;

  private Statistics insertStatistics;

  private Statistics deleteStatistics;

  private Statistics updateStatistics;

  FileGdbWriter(final CapiFileGdbDataObjectStore dataObjectStore) {
    this.dataObjectStore = dataObjectStore;
    this.geodatabase = dataObjectStore.getGeodatabase();
  }

  @Override
  @PreDestroy
  public void close() {
    for (final Entry<QName, Table> entry : tables.entrySet()) {
      final QName name = entry.getKey();
      final Table table = entry.getValue();
      try {
        table.freeWriteLock();
        table.setLoadOnlyMode(false);
        geodatabase.CloseTable(table);
      } catch (final Throwable e) {
        LOG.error("Unable to close table " + name);
      }
    }
    tables = null;
    dataObjectStore = null;
    if (insertStatistics != null) {
      insertStatistics.disconnect();
      insertStatistics = null;
    }
    if (updateStatistics != null) {
      updateStatistics.disconnect();
      updateStatistics = null;
    }
    if (deleteStatistics != null) {
      deleteStatistics.disconnect();
      deleteStatistics = null;
    }
  }

  private void delete(final DataObject object) {
    final DataObjectMetaData objectMetaData = object.getMetaData();
    final QName typeName = objectMetaData.getName();
    final Table table = getTable(typeName);
    final Row row = getRow(table, object);
    try {
      table.deleteRow(row);
    } finally {
      row.delete();
      getDeleteStatistics().add(object);
    }
  }

  public Statistics getDeleteStatistics() {
    if (deleteStatistics == null) {
      final String label = dataObjectStore.getLabel();
      if (label == null) {
        deleteStatistics = new Statistics("Delete");
      } else {
        deleteStatistics = new Statistics(label + " Delete");
      }
      deleteStatistics.connect();
    }
    return deleteStatistics;
  }

  public Statistics getInsertStatistics() {
    if (insertStatistics == null) {
      final String label = dataObjectStore.getLabel();
      if (label == null) {
        insertStatistics = new Statistics("Insert");
      } else {
        insertStatistics = new Statistics(label + " Insert");
      }
      insertStatistics.connect();
    }
    return insertStatistics;
  }

  public Row getRow(final Table table, final DataObject object) {
    final EnumRows rows = table.search("OBJECTID",
      "OBJECTID=" + object.getValue("OBJECTID"), false);
    final Row row = rows.next();
    return row;
  }

  private Table getTable(final QName typeName) {
    Table table = tables.get(typeName);
    if (table == null) {
      table = dataObjectStore.getTable(typeName);
      if (table != null) {
        tables.put(typeName, table);
        table.setWriteLock();
        table.setLoadOnlyMode(true);
      }
    }
    return table;
  }

  public Statistics getUpdateStatistics() {
    if (updateStatistics == null) {
      final String label = dataObjectStore.getLabel();
      if (label == null) {
        updateStatistics = new Statistics("Update");
      } else {
        updateStatistics = new Statistics(label + " Update");
      }
      updateStatistics.connect();
    }
    return updateStatistics;
  }

  private void insert(final DataObject object) {
    DataObjectMetaData sourceMetaData = object.getMetaData();
    final DataObjectMetaData metaData = dataObjectStore.getMetaData(sourceMetaData);
    final QName typeName = sourceMetaData.getName();
    final Table table = getTable(typeName);
    try {
      final Row row = table.createRowObject();
      try {
        List<Object> values = new ArrayList<Object>();
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final Object value = object.getValue(name);
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          Object esriValue = esriAttribute.setInsertValue(row, value);
          values.add(esriValue);
        }
        table.insertRow(row);
        for (final Attribute attribute : metaData.getAttributes()) {
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setPostInsertValue(object, row);
        }
      } finally {
        row.delete();
        getInsertStatistics().add(object);
      }
    } catch (final IllegalArgumentException e) {
      LOG.error(
        "Unable to insert row " + e.getMessage() + "\n" + object.toString(), e);
    } catch (final RuntimeException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Unable to insert row \n:" + object.toString());
      }
      throw new RuntimeException("Unable to insert row", e);
    }
  }

  private void update(final DataObject object) {
    DataObjectMetaData sourceMetaData = object.getMetaData();
    final DataObjectMetaData metaData = dataObjectStore.getMetaData(sourceMetaData);
    final QName typeName = sourceMetaData.getName();
    final Table table = getTable(typeName);
    final Row row = getRow(table, object);
    try {
      try {
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final Object value = object.getValue(name);
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setUpdateValue(row, value);
        }
        table.updateRow(row);
      } finally {
        row.delete();
        getUpdateStatistics().add(object);
      }
    } catch (final IllegalArgumentException e) {
      LOG.error(
        "Unable to insert row " + e.getMessage() + "\n" + object.toString(), e);
    } catch (final RuntimeException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Unable to insert row \n:" + object.toString());
      }
      throw new RuntimeException("Unable to insert row", e);
    }
  }

  public synchronized void write(final DataObject object) {
    try {
      final DataObjectMetaData metaData = object.getMetaData();
      final DataObjectStore dataObjectStore = metaData.getDataObjectStore();
      if (dataObjectStore == this.dataObjectStore) {
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
