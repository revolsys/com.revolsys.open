package com.revolsys.data.identifier;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.CompareUtil;

public abstract class AbstractIdentifier implements Identifier {

  public static void setIdentifier(final Map<String, Object> record,
    final List<String> idAttributeNames, final Identifier identifier) {
    if (identifier == null) {
      for (int i = 0; i < idAttributeNames.size(); i++) {
        final String attributeName = idAttributeNames.get(0);
        record.put(attributeName, null);
      }
    } else {
      identifier.setIdentifier(record, idAttributeNames);
    }
  }

  @Override
  public int compareTo(final Identifier other) {
    if (other == this) {
      return 0;
    } else if (other == null) {
      return -1;
    } else {
      final Iterator<Object> idIter = getValues().iterator();
      final Iterator<Object> otherIdIter = other.getValues().iterator();
      while (idIter.hasNext() && otherIdIter.hasNext()) {
        final Object value = idIter.next();
        final Object otherValue = otherIdIter.next();
        final int compare = CompareUtil.compare(value, otherValue);
        if (compare != 0) {
          return compare;
        }
      }
      if (idIter.hasNext()) {
        return 1;
      } else if (otherIdIter.hasNext()) {
        return -1;
      } else {
        return 0;
      }
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof Identifier) {
      final List<Object> values = getValues();
      final Identifier recordIdentifier = (Identifier)other;
      final List<Object> otherValues = recordIdentifier.getValues();
      if (EqualsRegistry.equal(values, otherValues)) {
        return true;
      } else {
        return false;
      }
    } else {
      final List<Object> values = getValues();
      if (values.size() == 1) {
        return values.get(0).equals(other);
      } else {
        return false;
      }
    }
  }

  @Override
  public Integer getInteger(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      return Integer.valueOf(value.toString());
    }
  }

  @Override
  public Long getLong(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      return Long.valueOf(value.toString());
    }
  }

  @Override
  public String getString(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else {
      return StringConverterRegistry.toString(value);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final int index) {
    return (V)getValues().get(index);
  }

  @Override
  public int hashCode() {
    int hashCode;
    final List<Object> values = getValues();
    if (values.size() == 1) {
      hashCode = values.get(0).hashCode();
    } else {
      hashCode = 1;
      for (final Object value : values) {
        hashCode *= 31;
        if (value != null) {
          hashCode += value.hashCode();
        }
      }
    }
    return hashCode;
  }

  @Override
  public void setIdentifier(final Map<String, Object> record,
    final List<String> attributeNames) {
    final List<Object> values = getValues();
    if (attributeNames.size() == values.size()) {
      for (int i = 0; i < attributeNames.size(); i++) {
        final String attributeName = attributeNames.get(i);
        final Object value = values.get(i);
        record.put(attributeName, value);
      }
    } else {
      throw new IllegalArgumentException("Attribute names count for "
          + attributeNames + " != count for values " + values);
    }
  }

  @Override
  public void setIdentifier(final Map<String, Object> record,
    final String... attributeNames) {
    setIdentifier(record, Arrays.asList(attributeNames));
  }

  @Override
  public void setIdentifier(final Record record) {
    setIdentifier(record, record.getRecordDefinition().getAttributeNames());
  }

  @Override
  public String toString() {
    final List<Object> values = getValues();
    return CollectionUtil.toString(":", values);
  }
}
