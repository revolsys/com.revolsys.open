package com.revolsys.gis.data.model.comparator;

import java.util.Comparator;

import com.revolsys.gis.data.model.DataObjectMetaData;

public class DataObjectMetaDataNameComparator implements
  Comparator<DataObjectMetaData> {
  public static int compareTypeNames(final DataObjectMetaData metaData1,
    final DataObjectMetaData metaData2) {
    final String typePath1 = metaData1.getPath();
    final String name1 = typePath1.toString();

    final String typePath2 = metaData2.getPath();
    final String name2 = typePath2.toString();

    return name1.compareTo(name2);
  }

  @Override
  public int compare(final DataObjectMetaData metaData1,
    final DataObjectMetaData metaData2) {
    return compareTypeNames(metaData1, metaData2);
  }
}
