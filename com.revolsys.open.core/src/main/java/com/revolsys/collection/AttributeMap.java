package com.revolsys.collection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class AttributeMap extends LinkedHashMap<String, Object> {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(AttributeMap.class);

  public AttributeMap() {
  }

  public AttributeMap(final int initialCapacity, final float loadFactor,
    final boolean accessOrder) {
    super(initialCapacity, loadFactor, accessOrder);
  }

  public AttributeMap(final int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public AttributeMap(final int initialCapacity) {
    super(initialCapacity);
  }

  public AttributeMap(final Map<? extends String, ? extends Object> m) {
    super(m);
  }

  public Map<String, Object> getAttributes() {
    return this;
  }

  public void setAttributes(final Map<String, Object> attributes) {
    clear();
    putAll(attributes);
  }

  public void setProperties(Resource resource) {
    try {
      Properties properties = new Properties();
      properties.load(resource.getInputStream());
      for (Entry<Object, Object> entry : properties.entrySet()) {
        String key = (String)entry.getKey();
        Object value = entry.getValue();
        put(key, value);
      }
    } catch (Throwable e) {
      LOG.error("Cannot load properties from " + resource, e);
    }
  }

}
