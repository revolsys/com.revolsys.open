package com.revolsys.record;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public abstract class AbstractRecord implements Record, Cloneable {
  @Override
  public Record clone() {
    try {
      final AbstractRecord record = (AbstractRecord)super.clone();
      record.setState(RecordState.NEW);
      return record;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone", e);
    }
  }

  @Override
  public boolean equals(final Object o) {
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  protected void initValues(final Map<String, ? extends Object> record) {
    if (record != null) {
      final List<FieldDefinition> fields = getFieldDefinitions();
      for (final FieldDefinition fieldDefintion : fields) {
        final String name = fieldDefintion.getName();
        final Object value = record.get(name);
        fieldDefintion.setValue(this, value);
      }
    }
  }

  protected abstract boolean setValue(FieldDefinition fieldDefinition, Object value);

  @Override
  public final boolean setValue(final int fieldIndex, final Object value) {
    final FieldDefinition fieldDefinition = getFieldDefinition(fieldIndex);
    if (fieldDefinition == null) {
      return false;
    } else {
      return setValue(fieldDefinition, value);
    }
  }

  @Override
  public void setValues(final Iterable<? extends Object> values) {
    if (values != null) {
      final Iterator<? extends Object> iterator = values.iterator();
      final RecordDefinition recordDefinition = getRecordDefinition();
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        if (iterator.hasNext()) {
          final Object value = iterator.next();
          setValue(fieldDefinition, value);
        } else {
          return;
        }
      }
    }
  }

  @Override
  public void setValues(final Object... values) {
    if (values != null) {
      int i = 0;
      final RecordDefinition recordDefinition = getRecordDefinition();
      for (final FieldDefinition fieldDefinition : recordDefinition.getFields()) {
        if (i < values.length) {
          final Object value = values[i];
          setValue(fieldDefinition, value);
          i++;
        } else {
          return;
        }
      }
    }
  }

  /**
   * Return a String representation of the record. There is no guarantee as to
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
      case PERSISTED:
        setState(RecordState.MODIFIED);
      break;
      case DELETED:
        throw new IllegalStateException("Cannot modify an object which has been deleted");
    }
  }

}
