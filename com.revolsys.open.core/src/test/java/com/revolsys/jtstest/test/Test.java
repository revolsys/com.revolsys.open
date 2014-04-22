package com.revolsys.jtstest.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;

public class Test implements Runnable, MapSerializer {

  private final String description;

  private final List<Test> tests = new ArrayList<>();

  private Map<String, Object> properties = new LinkedHashMap<>();

  private Test parent;

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "test", "Test", Test.class, "create");

  public static Test create(final Map<String, Object> map) {
    return new Test(map);
  }

  private List<Object> arguments = new ArrayList<Object>();

  private final String propertyName;

  private final String methodName;

  @SuppressWarnings("unchecked")
  public Test(final Map<String, Object> map) {
    this.description = (String)map.get("description");

    final Map<String, Object> properties = (Map<String, Object>)map.get("properties");
    if (properties != null) {
      this.properties = properties;
    }
    this.propertyName = (String)map.get("propertyName");
    this.methodName = (String)map.get("methodName");
    final List<Object> arguments = (List<Object>)map.get("arguments");
    if (arguments != null) {
      this.arguments = arguments;
    }

    final List<Map<String, Object>> tests = (List<Map<String, Object>>)map.get("tests");
    if (tests != null) {
      for (final Map<String, Object> testObject : tests) {
        final Test test = MapObjectFactoryRegistry.toObject(testObject);
        test.setParent(this);
        this.tests.add(test);
      }
    }
  }

  public Map<String, Object> getAllProperties() {
    Map<String, Object> allProperties;
    if (parent == null) {
      allProperties = new LinkedHashMap<>();
    } else {
      allProperties = parent.getAllProperties();
    }
    allProperties.putAll(getProperties());
    return allProperties;
  }

  public Test getParent() {
    return parent;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public <V> V getPropertyValue(final String name) {
    Object value = properties.get(name);
    if (value instanceof Map) {
      final Map<String, Object> valueMap = (Map<String, Object>)value;
      value = MapObjectFactoryRegistry.toObject(valueMap);
    } else if (value instanceof List) {
      final List<Object> list = (List<Object>)value;
      for (int i = 0; i < list.size(); i++) {
        Object listValue = list.get(i);
        if (listValue instanceof Map) {
          final Map<String, Object> valueMap = (Map<String, Object>)listValue;
          listValue = MapObjectFactoryRegistry.toObject(valueMap);
          list.set(i, listValue);
        }
      }
    }
    return (V)value;
  }

  @Override
  public void run() {
    for (final Test test : tests) {
      test.run();
    }
  }

  public void setParent(final Test parent) {
    this.parent = parent;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "test");
    MapSerializerUtil.add(map, "description", description);
    MapSerializerUtil.add(map, "propertyName", propertyName);
    MapSerializerUtil.add(map, "methodName", methodName);
    MapSerializerUtil.add(map, "arguments", arguments, Collections.emptyList());
    MapSerializerUtil.addAll(map, properties);
    MapSerializerUtil.add(map, "tests", tests);
    return map;
  }

  @Override
  public String toString() {
    return toMap().toString();
  }
}
