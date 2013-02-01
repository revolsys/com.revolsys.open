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
  void clearSelection();

  DataObjectMetaData getMetaData();

  DataObject getObject(int row);

  List<DataObject> getObjects();

  int getRowCount();

  List<DataObject> getSelectedObjects();

  void selectObjects(List<DataObject> objects);

  void selectObjects(DataObject... objects);

  void deleteObjects(DataObject... object);

  void deleteObjects(List<DataObject> objects);

  List<DataObject> getObjects(Geometry geometry, double distance);

  int setSelectedWithinDistance(boolean selected, Geometry geometry,
    int distance);

  void setEditingObjects(Collection<? extends DataObject> objects);

  void clearEditingObjects();

  Set<DataObject> getEditingObjects();

  void setHiddenObjects(Collection<? extends DataObject> hiddenObjects);

  void clearHiddenObjects();

  Set<DataObject> getHiddenObjects();

  void setHiddenObjects(DataObject... hiddenObjects);

  List<DataObject> getDataObjects(BoundingBox boundingBox);
}
