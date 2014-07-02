package com.revolsys.gis.data.model;

import java.util.Arrays;
import java.util.List;

public class FixedValueRecord extends BaseRecord {
  private static final long serialVersionUID = 1L;

  private static final DataObjectMetaData META_DATA = new DataObjectMetaDataImpl();

  private final Object value;

  public FixedValueRecord(final DataObjectMetaData metaData,
    final Object value) {
    super(metaData);
    this.value = value;
  }

  public FixedValueRecord(final Object value) {
    this(META_DATA, value);
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
  public void setValue(final int index, final Object value) {

  }
}
