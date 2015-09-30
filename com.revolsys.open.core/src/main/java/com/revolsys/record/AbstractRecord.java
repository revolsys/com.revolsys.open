package com.revolsys.record;

import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractRecord extends AbstractMap<String, Object>
  implements Record, Cloneable {
  /**
   * Construct a new clone of the object.
   *
   * @return The cloned object.
   */
  @Override
  public AbstractRecord clone() {
    try {
      final AbstractRecord record = (AbstractRecord)super.clone();
      record.setState(RecordState.New);
      return record;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone", e);
    }
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return new RecordEntrySet(this);
  }

  @Override
  public boolean equals(final Object o) {
    return this == o;
  }

  @Override
  public Object get(final Object key) {
    // Don't remove this speeds up field access
    if (key instanceof CharSequence) {
      final CharSequence name = (String)key;
      return getValue(name);
    } else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public Set<String> keySet() {
    // Don't remove this speeds up field access
    return new LinkedHashSet<>(getRecordDefinition().getFieldNames());
  }

  @Override
  public Object put(final String key, final Object value) {
    // Don't remove this speeds up field access
    final Object oldValue = getValue(key);
    setValue(key, value);
    return oldValue;
  }

  @Override
  public void putAll(final Map<? extends String, ? extends Object> values) {
    // Don't remove this speeds up field access
    setValues(values);
  }

  @Override
  public Object remove(final Object key) {
    // Don't remove this speeds up field access
    if (key instanceof CharSequence) {
      final CharSequence name = (CharSequence)key;
      final Object value = getValue(name);
      setValue(name, null);
      return value;
    }
    return null;
  }

  @Override
  public int size() {
    // Don't remove this speeds up field access
    return getRecordDefinition().getFieldCount();
  }

  /**
   * Return a String representation of the Object. There is no guarantee as to
   * the format of this string.
   *
   * @return The string value.
   */
  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    s.append(this.getRecordDefinition().getPath()).append("(\n");
    for (int i = 0; i < this.getRecordDefinition().getFieldCount(); i++) {
      final Object value = getValue(i);
      if (value != null) {
        s.append(this.getRecordDefinition().getFieldName(i)).append('=').append(value).append('\n');
      }
    }
    s.append(')');
    return s.toString();
  }

  @SuppressWarnings("incomplete-switch")
  protected void updateState() {
    switch (getState()) {
      case Persisted:
        setState(RecordState.Modified);
      break;
      case Deleted:
        throw new IllegalStateException("Cannot modify an object which has been deleted");
    }
  }

}
