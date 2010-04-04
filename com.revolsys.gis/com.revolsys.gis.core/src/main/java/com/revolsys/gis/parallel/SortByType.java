package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.comparator.DataObjectMetaDataNameComparator;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class SortByType extends BaseInOutProcess<DataObject> {

  private final Map<DataObjectMetaData, List<DataObject>> objectsByType = new TreeMap<DataObjectMetaData, List<DataObject>>(
    new DataObjectMetaDataNameComparator());

  @Override
  protected void postRun(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    for (final List<DataObject> objects : objectsByType.values()) {
      for (final DataObject object : objects) {
        out.write(object);
      }
    }
  }

  @Override
  protected void process(
    final Channel<DataObject> in,
    final Channel<DataObject> out,
    final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    List<DataObject> objects = objectsByType.get(metaData);
    if (objects == null) {
      objects = new ArrayList<DataObject>();
      objectsByType.put(metaData, objects);
    }
    objects.add(object);
  }
}
