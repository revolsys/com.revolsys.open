package com.revolsys.gis.converter;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;

public class GetDataObjectValue implements Converter<DataObject, Object> {
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

  public Object convert(final DataObject source) {
    Object value = DataObjectUtil.getAttributeByPath(source, attributePath);
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
