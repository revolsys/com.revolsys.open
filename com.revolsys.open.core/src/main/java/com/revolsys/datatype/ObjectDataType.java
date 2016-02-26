package com.revolsys.datatype;

import java.util.Map;

import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.util.Property;

public class ObjectDataType extends AbstractDataType {
  public ObjectDataType() {
    super("object", Object.class, true);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toObject(final Object value) {
    if (value instanceof Map<?, ?>) {
      final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
      final String type = (String)map.get("type");
      if (Property.hasValue(type)) {
        final Object object = MapObjectFactory.toObject(map);
        if (object != null) {
          return (V)object;
        }
      }
    }
    return (V)value;
  }
}
