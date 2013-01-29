package com.revolsys.swing.map.layer.dataobject;

import java.util.List;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.symbolizer.Symbolizer;

public interface DataObjectLayer extends Layer {
  void clearSelection();

  DataObjectMetaData getMetaData();

  DataObject getObject(int row);

  List<DataObject> getObjects();

  List<DataObject> getObjects(double x, double y, int buffer);

  int getRowCount();

  List<DataObject> getSelectedObjects();

  void selectObjects(List<DataObject> objects);

  void selectObjects(DataObject... objects);

  int setSelectedAtPoint(boolean selected, double x, double y, int tolerance);

  void setSymbolizers(Symbolizer... symbolizers);

  void deleteObjects(DataObject... object);

  void deleteObjects(List<DataObject> objects);
}
