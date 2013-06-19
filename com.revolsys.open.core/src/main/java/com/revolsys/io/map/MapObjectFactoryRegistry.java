package com.revolsys.io.map;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.util.CollectionUtil;
import com.revolsys.util.JavaBeanUtil;

public class MapObjectFactoryRegistry {

  private static Map<String, MapObjectFactory> typeToFactory = new HashMap<String, MapObjectFactory>();

  @SuppressWarnings("unchecked")
  public <V> V toObject(final Map<String, ? extends Object> map) {
    final String typeClass = CollectionUtil.getString(map, "typeClass");
    if (StringUtils.hasText(typeClass)) {
      // TODO factory methods and constructor arguments
      final V object = (V)JavaBeanUtil.createInstance(typeClass);

      return object;
    } else {
      final String type = CollectionUtil.getString(map, "type");
      final MapObjectFactory objectFactory = typeToFactory.get(type);
      if (objectFactory == null) {
        return (V)map;
      } else {
        return (V)objectFactory.toObject(map);
      }
    }
  }
}
