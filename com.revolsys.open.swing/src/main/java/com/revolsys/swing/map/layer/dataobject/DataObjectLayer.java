package com.revolsys.swing.map.layer.dataobject;

import java.awt.Component;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.map.layer.Layer;
import com.vividsolutions.jts.geom.Geometry;

public interface DataObjectLayer extends Layer {
  void addNewObject();

  void addSelectedObjects(Collection<? extends LayerDataObject> objects);

  void addSelectedObjects(LayerDataObject... objects);

  void clearEditingObjects();

  void clearSelectedObjects();

  LayerDataObject createObject();

  Component createTablePanel();

  void deleteObjects(Collection<? extends LayerDataObject> objects);

  void deleteObjects(LayerDataObject... object);

  int getChangeCount();

  List<LayerDataObject> getChanges();

  List<String> getColumnNames();

  List<LayerDataObject> getDataObjects(BoundingBox boundingBox);

  DataObjectStore getDataStore();

  Set<LayerDataObject> getEditingObjects();

  DataType getGeometryType();

  List<LayerDataObject> getMergeableSelectedObjects();

  DataObjectMetaData getMetaData();

  int getNewObjectCount();

  List<LayerDataObject> getNewObjects();

  LayerDataObject getObject(int row);

  LayerDataObject getObjectById(Object id);

  List<LayerDataObject> getObjects();

  Query getQuery();

  int getRowCount();

  int getRowCount(Query query);

  List<LayerDataObject> getSelectedObjects();

  int getSelectionCount();

  boolean isCanAddObjects();

  boolean isCanDeleteObjects();

  boolean isCanEditObjects();

  boolean isDeleted(LayerDataObject object);

  boolean isEditing(LayerDataObject object);

  @Override
  boolean isHasChanges();

  boolean isHidden(LayerDataObject object);

  boolean isModified(LayerDataObject object);

  boolean isNew(LayerDataObject object);

  boolean isSelected(LayerDataObject object);

  boolean isVisible(LayerDataObject object);

  List<LayerDataObject> query(Geometry geometry, double distance);

  List<LayerDataObject> query(Query query);

  void revertChanges(LayerDataObject object);

  boolean saveChanges(LayerDataObject object);

  void setEditingObjects(Collection<? extends LayerDataObject> objects);

  void setQuery(Query query);

  void setSelectedObjects(BoundingBox boundingBox);

  void setSelectedObjects(Collection<LayerDataObject> objects);

  void setSelectedObjects(LayerDataObject... selectedObjects);

  void setSelectedObjectsById(Object sessionId);

  int setSelectedWithinDistance(boolean selected, Geometry geometry,
    int distance);

  LayerDataObject showAddForm(Map<String, Object> parameters);

  <V extends JComponent> V showForm(final LayerDataObject object);

  void showViewAttributes();

  void unselectObjects(Collection<? extends LayerDataObject> objects);

  void unselectObjects(LayerDataObject... objects);
}
