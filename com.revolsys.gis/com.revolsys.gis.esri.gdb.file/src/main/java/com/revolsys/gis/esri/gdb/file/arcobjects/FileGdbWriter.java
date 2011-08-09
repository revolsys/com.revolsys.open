package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.system.Cleaner;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.AbstractWriter;

public class FileGdbWriter extends AbstractWriter<DataObject> {
  private static final Logger LOG = LoggerFactory.getLogger(FileGdbWriter.class);

  private Map<DataObjectMetaData, Object> cursors = new HashMap<DataObjectMetaData, Object>();

  private Map<DataObjectMetaData, Object> rowBuffers = new HashMap<DataObjectMetaData, Object>();

  private ArcObjectsFileGdbDataObjectStore dataObjectStore;

  private Statistics insertStatistics;

  private Statistics deleteStatistics;

  private Statistics updateStatistics;

  private int i = 0;

  FileGdbWriter(final ArcObjectsFileGdbDataObjectStore dataObjectStore) {
    this.dataObjectStore = dataObjectStore;
  }

  @Override
  @PreDestroy
  public void close() {
    for (final Entry<DataObjectMetaData, Object> entry : cursors.entrySet()) {
      DataObjectMetaData metaData = entry.getKey();
      Object cursor = entry.getValue();
      flush(cursor);
      ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
        "setLoadOnlyMode", metaData, false);
      ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class, "release",
        cursor);
    }
    for (final Object rowBuffer : rowBuffers.values()) {
      ArcObjectsFileGdbDataObjectStore.release(rowBuffer);
    }
    rowBuffers = null;
    cursors = null;
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
    // final DataObjectMetaData objectMetaData = object.getMetaData();
    // final QName typeName = objectMetaData.getName();
    // final Object objectId = object.getValue("OBJECTID");
    // try {
    // final ITable table = getITable(typeName);
    // if (table instanceof IFeatureClass) {
    // IFeatureClass iFeatureClass = (IFeatureClass)table;
    // FeatureClass featureClass = new FeatureClass(iFeatureClass);
    // featureClasses.add(featureClass);
    // featureClass.setLoadOnlyMode(true);
    // }
    // final IQueryFilter query = new QueryFilter();
    // query.setWhereClause("OBJECTID=" + objectId);
    // table.deleteSearchedRows(query);
    // getDeleteStatistics().add(object);
    // } catch (final Exception e) {
    // throw new RuntimeException("Unable to delete record " + typeName
    // + ".OBJECTID=" + objectId);
    // }
  }

  public void flush(final Object cursor) {
    ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class, "flush",
      cursor);
  }

  private boolean editing;

  private Object getCursor(final DataObjectMetaData metaData) {
    synchronized (cursors) {

      Object cursor = cursors.get(metaData);
      if (cursor == null) {
        if (!editing) {
          startEditing();
        }
        cursor = ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
          "createInsertCursor", metaData);
        cursors.put(metaData, cursor);
      }
      return cursor;
    }
  }

  private void startEditing() {
    ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
      "startEditing", dataObjectStore.getWorkspace());
    this.editing = true;
  }

  private void stopEditing() {
    ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
      "stopEditing", dataObjectStore.getWorkspace());
    this.editing = false;
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

  private Object getRowBuffer(final DataObjectMetaData metaData) {
    Object rowBuffer = rowBuffers.get(metaData);
    if (rowBuffer == null) {
      rowBuffer = ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
        "createRowBuffer", metaData);
      rowBuffers.put(metaData, rowBuffer);
    }
    return rowBuffer;
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
    final QName typeName = object.getMetaData().getName();
    final DataObjectMetaData metaData = dataObjectStore.getMetaData(typeName);
    try {
      final Object cursor = getCursor(metaData);
      ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class, "insert",
        cursor, metaData, object);
      getInsertStatistics().add(object);
      i++;
      if (i % 1000 == 0) {
        flush(cursor);
        stopEditing();
        startEditing();
      }
      if (i % 10000 == 0) {
        System.out.println(i);
      }
    } catch (final IllegalArgumentException e) {
      LOG.error("Unable to insert row \n:" + object.toString(),e);
    } catch (final Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Unable to insert row \n:" + object.toString(),e);
      }
      throw new RuntimeException("Unable to insert row", e);
    }
  }

  private void update(final DataObject object) {
    // final DataObjectMetaData objectMetaData = object.getMetaData();
    // final DataObjectMetaData metaData =
    // dataObjectStore.getMetaData(objectMetaData);
    // final QName typeName = metaData.getName();
    // final Object objectId = object.getValue("OBJECTID");
    // try {
    // final ITable table = getITable(typeName);
    // final IQueryFilter query = new QueryFilter();
    // query.setWhereClause("OBJECTID=" + objectId);
    // final ICursor updateCursor = table.update(query, true);
    // final IRow row = updateCursor.nextRow();
    // if (row == null) {
    // LOG.error("Unable to update row as it does not exist" + typeName
    // + ".OBJECTID=" + objectId);
    // for (final Attribute attribute : metaData.getAttributes()) {
    // final String name = attribute.getName();
    // final Object value = object.getValue(name);
    // final AbstractFileGdbAttribute esriAttribute =
    // (AbstractFileGdbAttribute)attribute;
    // esriAttribute.setUpdateValue(row, value);
    // }
    // updateCursor.updateRow(row);
    // updateCursor.flush();
    // } else {
    // getUpdateStatistics().add(object);
    // }
    // } catch (final Exception e) {
    // throw new RuntimeException("Unable to update record " + typeName
    // + ".OBJECTID=" + objectId);
    // }
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
