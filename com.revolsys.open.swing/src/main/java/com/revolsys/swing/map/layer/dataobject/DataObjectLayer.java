package com.revolsys.swing.map.layer.dataobject;

import java.awt.Component;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.map.layer.Layer;
import com.vividsolutions.jts.geom.Geometry;

public interface DataObjectLayer extends Layer {
  void addNewRecord();

  void addSelectedRecords(Collection<? extends LayerDataObject> records);

  void addSelectedRecords(LayerDataObject... records);

  void clearSelectedRecords();

  LayerDataObject createRecord();

  Component createTablePanel();

  void deleteRecords(Collection<? extends LayerDataObject> records);

  void deleteRecords(LayerDataObject... record);

  int getChangeCount();

  List<LayerDataObject> getChanges();

  List<String> getColumnNames();

  List<LayerDataObject> getDataObjects(BoundingBox boundingBox);

  DataObjectStore getDataStore();

  DataType getGeometryType();

  List<LayerDataObject> getMergeableSelectedRecords();

  DataObjectMetaData getMetaData();

  int getNewObjectCount();

  List<LayerDataObject> getNewRecords();

  Query getQuery();

  LayerDataObject getRecord(int row);

  LayerDataObject getRecordById(Object id);

  List<LayerDataObject> getRecords();

  int getRowCount();

  int getRowCount(Query query);

  List<LayerDataObject> getSelectedRecords();

  List<LayerDataObject> getSelectedRecords(BoundingBox boundingBox);

  int getSelectionCount();

  boolean isCanAddRecords();

  boolean isCanDeleteRecords();

  boolean isCanEditRecords();

  boolean isDeleted(LayerDataObject record);

  boolean isEventsEnabled();

  @Override
  boolean isHasChanges();

  boolean isHidden(LayerDataObject record);

  boolean isModified(LayerDataObject record);

  boolean isNew(LayerDataObject record);

  boolean isSelected(LayerDataObject record);

  boolean isVisible(LayerDataObject record);

  List<LayerDataObject> query(Geometry geometry, double distance);

  List<LayerDataObject> query(Query query);

  void revertChanges(LayerDataObject record);

  boolean saveChanges(LayerDataObject record);

  void setQuery(Query query);

  void setSelectedRecords(BoundingBox boundingBox);

  void setSelectedRecords(Collection<LayerDataObject> records);

  void setSelectedRecords(LayerDataObject... records);

  void setSelectedRecordsById(Object id);

  int setSelectedWithinDistance(boolean selected, Geometry geometry,
    int distance);

  LayerDataObject showAddForm(Map<String, Object> parameters);

  <V extends JComponent> V showForm(final LayerDataObject record);

  void showRecordsTable();

  void unselectRecords(Collection<? extends LayerDataObject> records);

  void unselectRecords(LayerDataObject... records);
}
