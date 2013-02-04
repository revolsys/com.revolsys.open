package com.revolsys.swing.map.layer.dataobject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.Layer;
import com.vividsolutions.jts.geom.Geometry;

public interface DataObjectLayer extends Layer {
  void clearEditingObjects();

  void clearHiddenObjects();

  void clearSelection();

  void deleteObjects(DataObject... object);

  void deleteObjects(List<DataObject> objects);

  List<DataObject> getDataObjects(BoundingBox boundingBox);

  Set<DataObject> getEditingObjects();

  Set<DataObject> getHiddenObjects();

  DataObjectMetaData getMetaData();

  DataObject getObject(int row);

  List<DataObject> getObjects();

  List<DataObject> getObjects(Geometry geometry, double distance);

  int getRowCount();

  List<DataObject> getSelectedObjects();

  void selectObjects(DataObject... objects);

  void selectObjects(List<DataObject> objects);

  void setEditingObjects(Collection<? extends DataObject> objects);

  void setHiddenObjects(Collection<? extends DataObject> hiddenObjects);

  void setHiddenObjects(DataObject... hiddenObjects);

  int setSelectedWithinDistance(boolean selected, Geometry geometry,
    int distance);
}
