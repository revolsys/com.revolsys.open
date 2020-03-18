package com.revolsys.record;

import java.util.AbstractMap;
import java.util.Set;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.JavaBeanUtil;

public class DelegatingRecord extends AbstractMap<String, Object> implements Record {
  private final Record record;

  public DelegatingRecord(final Record record) {
    this.record = record;
  }

  @Override
  public Record clone() {
    final Record clone = JavaBeanUtil.clone(this.record);
    return new DelegatingRecord(clone);
  }

  @Override
  public int compareTo(final Record o) {
    return this.record.compareTo(o);
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return this.record.entrySet();
  }

  public Record getRecord() {
    return this.record;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.record.getRecordDefinition();
  }

  @Override
  public RecordFactory getRecordFactory() {
    return this.record.getRecordFactory();
  }

  @Override
  public RecordState getState() {
    return this.record.getState();
  }

  @Override
  public <T extends Object> T getValue(final int index) {
    return this.record.getValue(index);
  }

  @Override
  public int hashCode() {
    return this.record.hashCode();
  }

  @Override
  public boolean isState(final RecordState state) {
    return this.record.isState(state);
  }

  @Override
  public RecordState setState(final RecordState state) {
    return this.record.setState(state);
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    return this.record.setValue(index, value);
  }

  @Override
  public void setValues(final Iterable<? extends Object> values) {
    this.record.setValues(values);
  }

  @Override
  public void setValues(final Object... values) {
    this.record.setValues(values);
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

}
