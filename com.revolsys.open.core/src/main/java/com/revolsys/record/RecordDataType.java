package com.revolsys.record;

import java.util.Collection;
import java.util.Map;

import com.revolsys.datatype.AbstractDataType;
import com.revolsys.datatype.DataTypes;

public class RecordDataType extends AbstractDataType {
  public RecordDataType() {
    super("record", Record.class, true);
  }

  @Override
  public boolean equals(final Object value1, final Object value2) {
    final Record record = toObject(value1);
    final Map<String, Object> map = DataTypes.MAP.toObject(value2);
    return record.equalValuesAll(map);
  }

  @Override
  public boolean equals(final Object value1, final Object value2,
    final Collection<String> excludeFieldNames) {
    final Record record = toObject(value1);
    final Map<String, Object> map = DataTypes.MAP.toObject(value2);
    return record.equalValuesAll(map, excludeFieldNames);
  }
}
