package com.revolsys.data.comparator;

import java.util.Comparator;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;

public class RecordTypeNameComparator implements Comparator<Record> {
  public static int compareTypeNames(final Record object1,
    final Record object2) {
    final RecordDefinition metaData1 = object1.getMetaData();
    final String typePath1 = metaData1.getPath();
    final String name1 = typePath1.toString();

    final RecordDefinition metaData2 = object2.getMetaData();
    final String typePath2 = metaData2.getPath();
    final String name2 = typePath2.toString();

    return name1.compareTo(name2);
  }

  @Override
  public int compare(final Record object1, final Record object2) {
    return compareTypeNames(object1, object2);
  }
}
