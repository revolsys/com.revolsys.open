package com.revolsys.swing.map.layer.dataobject;

import java.awt.Component;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.map.layer.Layer;
import com.vividsolutions.jts.geom.Geometry;

public interface DataObjectLayer extends Layer {
  void addNewRecord();

  void addSelectedObjects(Collection<? extends DataObject> objects);

  void addSelectedObjects(DataObject... objects);

  void clearEditingObjects();

  void clearSelectedObjects();

  DataObject createObject();

  Component createTablePanel();

  void deleteObjects(Collection<? extends DataObject> objects);

  void deleteObjects(DataObject... object);

  int getChangeCount();

  List<DataObject> getChanges();

  List<DataObject> getDataObjects(BoundingBox boundingBox);

  DataObjectStore getDataStore();

  Set<DataObject> getEditingObjects();

  DataObjectMetaData getMetaData();

  int getNewObjectCount();

  List<DataObject> getNewObjects();

  DataObject getObject(int row);

  DataObject getObjectById(Object id);

  List<DataObject> getObjects();

  List<DataObject> getObjects(Geometry geometry, double distance);

  Query getQuery();

  int getRowCount();

  int getRowCount(Query query);

  List<DataObject> getSelectedObjects();

  int getSelectionCount();

  boolean isCanAddObjects();

  boolean isCanDeleteObjects();

  boolean isCanEditObjects();

  boolean isDeleted(DataObject object);

  boolean isEditing(DataObject object);

  @Override
  boolean isHasChanges();

  boolean isHidden(DataObject object);

  boolean isModified(DataObject object);

  boolean isNew(DataObject object);

  boolean isSelected(DataObject object);

  boolean isVisible(DataObject object);

  List<DataObject> query(Query query);

  void revertChanges(DataObject object);

  boolean saveChanges(DataObject object);

  void setEditingObjects(Collection<? extends DataObject> objects);

  void setQuery(Query query);

  void setSelectedObjects(BoundingBox boundingBox);

  void setSelectedObjects(Collection<DataObject> objects);

  void setSelectedObjects(DataObject... selectedObjects);

  void setSelectedObjectsById(Object sessionId);

  int setSelectedWithinDistance(boolean selected, Geometry geometry,
    int distance);

  <V extends JComponent> V showForm(final DataObject object);

  void unselectObjects(Collection<? extends DataObject> objects);

  void unselectObjects(DataObject... objects);
}
