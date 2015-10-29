package com.revolsys.record;

import com.revolsys.record.schema.FieldDefinition;

public abstract class AbstractRecord implements Record, Cloneable {
  /**
   * Construct a new clone of the object.
   *
   * @return The cloned object.
   */
  @Override
  public AbstractRecord clone() {
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
