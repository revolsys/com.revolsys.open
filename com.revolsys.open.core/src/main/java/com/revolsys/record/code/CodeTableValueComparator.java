package com.revolsys.record.code;

import java.util.Comparator;

import com.revolsys.identifier.Identifier;
import com.revolsys.util.CompareUtil;

public class CodeTableValueComparator implements Comparator<Identifier> {
  private final CodeTable codeTable;

  public CodeTableValueComparator(final CodeTable codeTable) {
    this.codeTable = codeTable;
  }

  @Override
  public int compare(final Identifier identifier1, final Identifier identifier2) {
    if (identifier1 == null) {
      if (identifier2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (identifier2 == null) {
      return 1;
    } else if (identifier1.equals(identifier2)) {
      return 0;
    } else {
      final Object value1 = this.codeTable.getValue(identifier1);
      final Object value2 = this.codeTable.getValue(identifier2);
      int compare = CompareUtil.compare(value1, value2);
      if (compare == 0) {
        compare = identifier1.compareTo(identifier2);
      }
      return compare;
    }
  }

}
