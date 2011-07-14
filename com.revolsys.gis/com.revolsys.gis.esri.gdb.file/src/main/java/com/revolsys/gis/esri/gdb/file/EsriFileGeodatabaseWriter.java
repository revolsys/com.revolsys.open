package com.revolsys.gis.esri.gdb.file;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private Geodatabase geodatabase;

  private EsriFileGeodatabaseDataObjectStore dataObjectStore;

  public EsriFileGeodatabaseWriter(
    EsriFileGeodatabaseDataObjectStore dataObjectStore) {
    this.dataObjectStore = dataObjectStore;
    this.geodatabase = dataObjectStore.getGeodatabase();
  }

  @PreDestroy
  public void close() {
    for (Entry<QName, Table> entry : tables.entrySet()) {
      QName name = entry.getKey();
      Table table = entry.getValue();
      try {
        geodatabase.CloseTable(table);
      } catch (Throwable e) {
        LOG.error("Unable to close table " + name);
      }
    }
    tables = null;
    dataObjectStore = null;
  }

  private void delete(final DataObject object) {
    final DataObjectMetaData objectMetaData = object.getMetaData();
    QName typeName = objectMetaData.getName();
    Table table = getTable(typeName);
    DataObjectMetaData metaData = dataObjectStore.getMetaData(objectMetaData);
    final String idAttributeName = metaData.getIdAttributeName();
    EnumRows rows = table.search(idAttributeName, idAttributeName + "="
      + object.getValue(idAttributeName), false);
    Row row = rows.next();
    table.Delete(row);
  }

  private void insert(final DataObject object) {
    QName typeName = object.getMetaData().getName();
    Table table = getTable(typeName);
    DataObjectMetaData metaData = dataObjectStore.getMetaData(typeName);

    Row row = table.createRowObject();
    for (Attribute attribute : metaData.getAttributes()) {
      String name = attribute.getName();
      Object value = object.getValue(name);
      AbstractEsriFileGeodatabaseAttribute esriAttribute = (AbstractEsriFileGeodatabaseAttribute)attribute;
      esriAttribute.setInsertValue(row, value);
    }
    table.Insert(row);
  }

  private Table getTable(QName typeName) {
    Table table = tables.get(typeName);
    if (table == null) {
      table = dataObjectStore.getTable(typeName);
      tables.put(typeName, table);
    }
    return table;
  }

  private void update(final DataObject object) {
    QName typeName = object.getMetaData().getName();
    Table table = getTable(typeName);
    DataObjectMetaData metaData = dataObjectStore.getMetaData(typeName);
    final String idAttributeName = metaData.getIdAttributeName();
    EnumRows rows = table.search(idAttributeName, idAttributeName + "="
      + object.getValue(idAttributeName), false);
    Row row = rows.next();

    for (Attribute attribute : metaData.getAttributes()) {
      String name = attribute.getName();
      Object value = object.getValue(name);
      AbstractEsriFileGeodatabaseAttribute esriAttribute = (AbstractEsriFileGeodatabaseAttribute)attribute;
      esriAttribute.setUpdateValue(row, value);
    }
    table.Update(row);
  }

  public synchronized void write(final DataObject object) {
    try {
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
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to write", e);
    }
  }
}
