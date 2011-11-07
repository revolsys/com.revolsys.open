package com.revolsys.gis.data.model.comparator;

import java.util.Comparator;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObjectMetaData;

public class DataObjectMetaDataNameComparator implements
  Comparator<DataObjectMetaData> {
  public static int compareTypeNames(
    final DataObjectMetaData metaData1,
    final DataObjectMetaData metaData2) {
    final QName typeName1 = metaData1.getName();
    final String name1 = typeName1.toString();

    final QName typeName2 = metaData2.getName();
    final String name2 = typeName2.toString();

    return name1.compareTo(name2);
  }

  public int compare(
    final DataObjectMetaData metaData1,
    final DataObjectMetaData metaData2) {
    return compareTypeNames(metaData1, metaData2);
  }
}
