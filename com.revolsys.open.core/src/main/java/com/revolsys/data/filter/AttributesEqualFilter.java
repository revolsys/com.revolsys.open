package com.revolsys.data.filter;

import java.util.Arrays;
import java.util.Collection;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.filter.Filter;

public class AttributesEqualFilter implements Filter<Record> {
  public static boolean accept(final Record object1, final Record object2,
    final boolean nullEqualsEmptyString, final Collection<String> fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object value1 = RecordUtil.getFieldByPath(object1, fieldName);
      final Object value2 = RecordUtil.getFieldByPath(object2, fieldName);
      if (nullEqualsEmptyString) {
        if (value1 == null) {
          if (value2 != null && !"".equals(value2)) {
            return false;
          }
        } else if (value2 == null) {
          if (value1 != null && !"".equals(value1)) {
            return false;
          }
        } else if (!EqualsInstance.INSTANCE.equals(value1, value2)) {
          return false;
        }
      } else {
        if (!EqualsInstance.INSTANCE.equals(value1, value2)) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean accept(final Record object1, final Record object2,
    final boolean nullEqualsEmptyString, final String... fieldNames) {
    return accept(object1, object2, nullEqualsEmptyString,
      Arrays.asList(fieldNames));
  }

  public static boolean accept(final Record object1, final Record object2,
    final String... fieldNames) {
    return accept(object1, object2, false, Arrays.asList(fieldNames));
  }

  private final Collection<String> fieldNames;

  private final Record object;

  private boolean nullEqualsEmptyString;

  public AttributesEqualFilter(final Record object,
    final Collection<String> fieldNames) {
    this.fieldNames = fieldNames;
    this.object = object;
  }

  public AttributesEqualFilter(final Record object, final String... fieldNames) {
    this(object, Arrays.asList(fieldNames));
  }

  @Override
  public boolean accept(final Record object) {
    return accept(this.object, object, this.nullEqualsEmptyString,
      this.fieldNames);
  }

  public boolean isNullEqualsEmptyString() {
    return this.nullEqualsEmptyString;
  }

  public void setNullEqualsEmptyString(final boolean nullEqualsEmptyString) {
    this.nullEqualsEmptyString = nullEqualsEmptyString;
  }

  @Override
  public String toString() {
    return "AttributeEquals" + this.fieldNames;
  }

}
