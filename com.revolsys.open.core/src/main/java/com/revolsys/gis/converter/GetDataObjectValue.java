package com.revolsys.gis.converter;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordUtil;

public class GetDataObjectValue implements Converter<Record, Object> {
  private String attributePath;

  private Map<? extends Object, ? extends Object> valueMap;

  public GetDataObjectValue() {
  }

  public GetDataObjectValue(final String attributePath) {
    this.attributePath = attributePath;
  }

  public GetDataObjectValue(final String attributePath,
    final Map<? extends Object, ? extends Object> valueMap) {
    this.attributePath = attributePath;
    this.valueMap = valueMap;
  }

  @Override
  public Object convert(final Record source) {
    Object value = RecordUtil.getAttributeByPath(source, attributePath);
    if (!valueMap.isEmpty()) {
      if (valueMap.containsKey(value)) {
        value = valueMap.get(value);
      }
    }
    return value;
  }

  @Override
  public String toString() {
    return attributePath;
  }
}
