package com.revolsys.gis.data.model.codes;

import java.util.Comparator;

import com.revolsys.util.CompareUtil;

public class CodeTableCoparator implements Comparator<Object> {

  private final CodeTable codeTable;

  public CodeTableCoparator(final CodeTable codeTable) {
    this.codeTable = codeTable;
  }

  @Override
  public int compare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (value2 == null) {
      return -1;
    } else {
      final Object codeValue1 = codeTable.getValue(value1);
      final Object codeValue2 = codeTable.getValue(value2);
      return CompareUtil.compare(codeValue1, codeValue2);
    }
  }

  @Override
  public String toString() {
    return codeTable.toString();
  }
}
