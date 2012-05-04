package com.revolsys.gis.data.model.comparator;

import java.util.Comparator;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class DataObjectTypeNameComparator implements Comparator<DataObject> {
  public static int compareTypeNames(
    final DataObject object1,
    final DataObject object2) {
    final DataObjectMetaData metaData1 = object1.getMetaData();
    final String typePath1 = metaData1.getPath();
    final String name1 = typePath1.toString();

    final DataObjectMetaData metaData2 = object2.getMetaData();
    final String typePath2 = metaData2.getPath();
    final String name2 = typePath2.toString();

    return name1.compareTo(name2);
  }

  public int compare(final DataObject object1, final DataObject object2) {
    return compareTypeNames(object1, object2);
  }
}
