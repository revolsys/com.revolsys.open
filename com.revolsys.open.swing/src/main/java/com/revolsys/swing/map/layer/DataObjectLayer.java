package com.revolsys.swing.map.layer;

import java.util.List;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public interface DataObjectLayer extends Layer {
  List<DataObject> getObjects();

  List<DataObject> getSelectedObjects();

  DataObjectMetaData getMetaData();
  
  int getRowCount();

  DataObject getObject(int row);
}
