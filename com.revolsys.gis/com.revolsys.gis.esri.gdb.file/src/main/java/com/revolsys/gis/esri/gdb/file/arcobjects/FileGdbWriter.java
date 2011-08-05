package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IQueryFilter;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.IRowBuffer;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.Cleaner;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.AbstractFileGdbAttribute;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.util.NoOp;
import com.revolsys.io.AbstractWriter;

public class FileGdbWriter extends AbstractWriter<DataObject> {
  private static final Logger LOG = LoggerFactory.getLogger(FileGdbWriter.class);

  private Map<QName, ICursor> cursors = new HashMap<QName, ICursor>();

  private Map<QName, IRowBuffer> rowBuffers = new HashMap<QName, IRowBuffer>();

  private List<FeatureClass> featureClasses = new ArrayList<FeatureClass>();

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
    for (final Entry<QName, ICursor> entry : cursors.entrySet()) {
      final QName name = entry.getKey();
      final ICursor cursor = entry.getValue();
      try {
        cursor.flush();
        Cleaner.release(cursor);
      } catch (final Throwable e) {
        LOG.error("Unable to close " + name);
      }
    }
    for (FeatureClass featureClass : featureClasses) {
      try {
        featureClass.setLoadOnlyMode(false);
      } catch (final Throwable e) {
        LOG.error("Unable to close " + featureClass);
      }
    }
    for (IRowBuffer rowBuffer : rowBuffers.values()) {
      try {
        Cleaner.release(rowBuffer);
      } catch (final Throwable e) {
        LOG.error("Unable to close " + rowBuffer);
      }
    }
    rowBuffers = null;
    cursors = null;
    featureClasses = null;
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
    final Object objectId = object.getValue("OBJECTID");
    try {
      final ITable table = dataObjectStore.getITable(typeName);
      if (table instanceof IFeatureClass) {
        IFeatureClass iFeatureClass = (IFeatureClass)table;
        FeatureClass featureClass = new FeatureClass(iFeatureClass);
        featureClasses.add(featureClass);
        featureClass.setLoadOnlyMode(true);
      }
      final IQueryFilter query = new QueryFilter();
      query.setWhereClause("OBJECTID=" + objectId);
      table.deleteSearchedRows(query);
      getDeleteStatistics().add(object);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to delete record " + typeName
        + ".OBJECTID=" + objectId);
    }
  }

  private ICursor getCursor(final QName typeName) throws AutomationException,
    IOException {
    ICursor cursor = cursors.get(typeName);
    if (cursor == null) {
      final ITable table = dataObjectStore.getITable(typeName);

      cursor = table.insert(true);

      cursors.put(typeName, cursor);
    }
    return cursor;
  }

  private IRowBuffer getRowBuffer(final QName typeName)
    throws AutomationException, IOException {
    IRowBuffer rowBuffer = rowBuffers.get(typeName);
    if (rowBuffer == null) {
      final ITable table = dataObjectStore.getITable(typeName);

      rowBuffer = table.createRowBuffer();

      rowBuffers.put(typeName, rowBuffer);
    }
    return rowBuffer;
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
      final ICursor cursor = getCursor(typeName);
      final IRowBuffer row = getRowBuffer(typeName);
      for (final Attribute attribute : metaData.getAttributes()) {
        final String name = attribute.getName();
        final Object value = object.getValue(name);
        final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
        esriAttribute.setInsertValue(row, value);
      }
      final Object id = cursor.insertRow(row);

      object.setValue("OBJECTID", id);
      for (final Attribute attribute : metaData.getAttributes()) {
        final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
        esriAttribute.setPostInsertValue(object, row);
      }
      int geometryAttributeIndex = metaData.getGeometryAttributeIndex();
      if (geometryAttributeIndex != -1) {
        IGeometry iGeometry = (IGeometry)row.getValue(geometryAttributeIndex);
        Cleaner.release(iGeometry);
      }
      getInsertStatistics().add(object);
      i++;
      if (i % 1000 == 0) {
        cursor.flush();
        System.out.println(com.esri.arcgis.interop.Cleaner.getActiveObjectCount());
      }
    } catch (final Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Unable to insert row \n:" + object.toString());
      }
      throw new RuntimeException("Unable to insert row", e);
    }
  }

  private void update(final DataObject object) {
    final DataObjectMetaData objectMetaData = object.getMetaData();
    final DataObjectMetaData metaData = dataObjectStore.getMetaData(objectMetaData);
    final QName typeName = metaData.getName();
    final Object objectId = object.getValue("OBJECTID");
    try {
      final ITable table = dataObjectStore.getITable(typeName);
      final IQueryFilter query = new QueryFilter();
      query.setWhereClause("OBJECTID=" + objectId);
      final ICursor updateCursor = table.update(query, true);
      final IRow row = updateCursor.nextRow();
      if (row == null) {
        LOG.error("Unable to update row as it does not exist" + typeName
          + ".OBJECTID=" + objectId);
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final Object value = object.getValue(name);
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setUpdateValue(row, value);
        }
        updateCursor.updateRow(row);
        updateCursor.flush();
      } else {
        getUpdateStatistics().add(object);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to update record " + typeName
        + ".OBJECTID=" + objectId);
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
