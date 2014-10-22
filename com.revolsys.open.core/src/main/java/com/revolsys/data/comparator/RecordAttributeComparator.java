package com.revolsys.data.comparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.util.CompareUtil;

public class RecordAttributeComparator implements Comparator<Record> {
  private List<String> fieldNames;

  private boolean invert;

  private boolean nullFirst;

  public RecordAttributeComparator() {
  }

  public RecordAttributeComparator(final boolean sortAsceding,
    final String... fieldNames) {
    this(Arrays.asList(fieldNames));
    this.invert = !sortAsceding;
  }

  public RecordAttributeComparator(final List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public RecordAttributeComparator(final String... fieldNames) {
    this(Arrays.asList(fieldNames));
  }

  @Override
  public int compare(final Record object1, final Record object2) {
    for (final String fieldName : this.fieldNames) {
      final int compare = compare(object1, object2, fieldName);
      if (compare != 0) {
        return compare;
      }
    }

    return 0;
  }

  public int compare(final Record object1, final Record object2,
    final String fieldName) {
    final Comparable<Object> value1 = RecordUtil.getFieldByPath(object1,
      fieldName);
    final Comparable<Object> value2 = RecordUtil.getFieldByPath(object2,
      fieldName);
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        if (this.nullFirst) {
          return -1;
        } else {
          return 1;
        }
      }
    } else if (value2 == null) {
      if (this.nullFirst) {
        return 1;
      } else {
        return -1;
      }
    } else {
      final int compare = CompareUtil.compare(value1, value2);
      if (this.invert) {
        return -compare;
      } else {
        return compare;
      }
    }
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public boolean isInvert() {
    return this.invert;
  }

  public boolean isNullFirst() {
    return this.nullFirst;
  }

  public void setFieldNames(final List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public void setInvert(final boolean invert) {
    this.invert = invert;
  }

  public void setNullFirst(final boolean nullFirst) {
    this.nullFirst = nullFirst;
  }

}
