package com.revolsys.data.record;

import java.util.Arrays;
import java.util.List;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;

public class FixedValueRecord extends BaseRecord {
  private static final long serialVersionUID = 1L;

  private static final RecordDefinition RECORD_DEFINITION = new RecordDefinitionImpl();

  private final Object value;

  public FixedValueRecord(final Object value) {
    this(RECORD_DEFINITION, value);
  }

  public FixedValueRecord(final RecordDefinition recordDefinition,
    final Object value) {
    super(recordDefinition);
    this.value = value;
  }

  @Override
  public FixedValueRecord clone() {
    final FixedValueRecord clone = (FixedValueRecord)super.clone();
    return clone;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getValue(final CharSequence name) {
    return (T)value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(final int index) {
    if (index < 0) {
      return null;
    } else {
      return (T)value;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getValueByPath(final CharSequence path) {
    return (T)value;
  }

  @Override
  public List<Object> getValues() {
    return Arrays.asList(value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    return false;
  }
}
