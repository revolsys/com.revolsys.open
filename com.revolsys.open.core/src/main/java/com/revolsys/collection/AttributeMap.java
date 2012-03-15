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

  public AttributeMap(final int initialCapacity) {
    super(initialCapacity);
  }

  public AttributeMap(final int initialCapacity, final float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public AttributeMap(final int initialCapacity, final float loadFactor,
    final boolean accessOrder) {
    super(initialCapacity, loadFactor, accessOrder);
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

  public void setProperties(final Resource resource) {
    final Properties properties = new Properties();
    try {
      properties.load(resource.getInputStream());
      setProperties(properties);
    } catch (final Throwable e) {
      LOG.error("Cannot load properties from " + resource, e);
    }
  }

  private void setProperties(final Properties properties) {
    for (final Entry<Object, Object> entry : properties.entrySet()) {
      final String key = (String)entry.getKey();
      final Object value = entry.getValue();
      put(key, value);
    }
  }

  public void setPropertyResources(Resource[] resources) {
    final Properties properties = new Properties();
    for (Resource resource : resources) {
      try {
        properties.load(resource.getInputStream());
      } catch (final Throwable e) {
        LOG.error("Cannot load properties from " + resource, e);
      }
    }
    setProperties(properties);
  }
}
