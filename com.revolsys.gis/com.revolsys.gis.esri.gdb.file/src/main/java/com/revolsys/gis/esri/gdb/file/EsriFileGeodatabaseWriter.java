package com.revolsys.gis.esri.gdb.file;

import java.util.HashMap;
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
import com.revolsys.gis.esri.gdb.file.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.file.swig.Table;
import com.revolsys.gis.esri.gdb.file.type.AbstractEsriFileGeodatabaseAttribute;
import com.revolsys.io.AbstractWriter;

public class EsriFileGeodatabaseWriter extends AbstractWriter<DataObject> {
  private static final Logger LOG = LoggerFactory.getLogger(EsriFileGeodatabaseWriter.class);

  private Map<QName, Table> tables = new HashMap<QName, Table>();

  private final Geodatabase geodatabase;

  private EsriFileGeodatabaseDataObjectStore dataObjectStore;

  EsriFileGeodatabaseWriter(
    final EsriFileGeodatabaseDataObjectStore dataObjectStore) {
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
        geodatabase.CloseTable(table);
      } catch (final Throwable e) {
        LOG.error("Unable to close table " + name);
      }
    }
    tables = null;
    dataObjectStore = null;
  }

  private void delete(final DataObject object) {
    final DataObjectMetaData objectMetaData = object.getMetaData();
    final QName typeName = objectMetaData.getName();
    final Table table = getTable(typeName);
    final DataObjectMetaData metaData = dataObjectStore.getMetaData(objectMetaData);
    final String idAttributeName = metaData.getIdAttributeName();
    final EnumRows rows = table.search(idAttributeName, idAttributeName + "="
      + object.getValue(idAttributeName), false);
    final Row row = rows.next();
    table.Delete(row);
  }

  private Table getTable(final QName typeName) {
    Table table = tables.get(typeName);
    if (table == null) {
      table = dataObjectStore.getTable(typeName);
      tables.put(typeName, table);
    }
    return table;
  }

  private void insert(final DataObject object) {
    final QName typeName = object.getMetaData().getName();
    final Table table = getTable(typeName);
    final DataObjectMetaData metaData = dataObjectStore.getMetaData(typeName);

    final Row row = table.createRowObject();
    for (final Attribute attribute : metaData.getAttributes()) {
      final String name = attribute.getName();
      final Object value = object.getValue(name);
      final AbstractEsriFileGeodatabaseAttribute esriAttribute = (AbstractEsriFileGeodatabaseAttribute)attribute;
      esriAttribute.setInsertValue(row, value);
    }
    table.Insert(row);
  }

  private void update(final DataObject object) {
    final QName typeName = object.getMetaData().getName();
    final Table table = getTable(typeName);
    final DataObjectMetaData metaData = dataObjectStore.getMetaData(typeName);
    final String idAttributeName = metaData.getIdAttributeName();
    final EnumRows rows = table.search(idAttributeName, idAttributeName + "="
      + object.getValue(idAttributeName), false);
    final Row row = rows.next();

    for (final Attribute attribute : metaData.getAttributes()) {
      final String name = attribute.getName();
      final Object value = object.getValue(name);
      final AbstractEsriFileGeodatabaseAttribute esriAttribute = (AbstractEsriFileGeodatabaseAttribute)attribute;
      esriAttribute.setUpdateValue(row, value);
    }
    table.Update(row);
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
