package com.revolsys.data.comparator;

import java.util.Comparator;

import com.revolsys.data.record.schema.RecordDefinition;

public class RecordDefinitionNameComparator implements
  Comparator<RecordDefinition> {
  public static int compareTypeNames(final RecordDefinition metaData1,
    final RecordDefinition metaData2) {
    final String typePath1 = metaData1.getPath();
    final String name1 = typePath1.toString();

    final String typePath2 = metaData2.getPath();
    final String name2 = typePath2.toString();

    return name1.compareTo(name2);
  }

  @Override
  public int compare(final RecordDefinition metaData1,
    final RecordDefinition metaData2) {
    return compareTypeNames(metaData1, metaData2);
  }
}
