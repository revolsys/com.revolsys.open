package com.revolsys.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

public class ThreadSharedAttributes {
  private static ThreadLocal<Map<Object, Object>> threadAttributes = new ThreadLocal<Map<Object, Object>>();

  private static Map<ThreadGroup, Map<Object, Object>> threadGroupAttributes = new WeakHashMap<ThreadGroup, Map<Object, Object>>();

  private static Map<Object, Object> defaultAttributes = new WeakHashMap<Object, Object>();

  public static void clearAttributes() {
    final Map<Object, Object> attributes = getLocalAttributes();
    synchronized (attributes) {
      attributes.clear();
    }
  }

  public static void clearThreadGroup(final ThreadGroup threadGroup) {
    synchronized (threadGroupAttributes) {
      threadGroupAttributes.remove(threadGroup);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getDefaultAttribute(final Object name) {
    synchronized (defaultAttributes) {
      final T value = (T)defaultAttributes.get(name);
      return value;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAttribute(final Object name) {
    final Map<Object, Object> attributes = getLocalAttributes();
    synchronized (attributes) {
      return (T)attributes.get(name);
    }
  }

  public static Map<String, Object> getAttributes() {
    final Map<Object, Object> attributes = getLocalAttributes();
    synchronized (attributes) {
      final HashMap<String, Object> map = new HashMap<String, Object>();
      for (final Entry<Object, Object> entry : attributes.entrySet()) {
        final Object key = entry.getKey();
        if (key instanceof String) {
          final String name = (String)key;
          final Object value = entry.getValue();
          map.put(name, value);
        }
      }
      return map;
    }
  }

  private static Map<Object, Object> getLocalAttributes() {
    Map<Object, Object> attributes = threadAttributes.get();
    if (attributes == null) {
      attributes = getThreadGroupAttributes();
      threadAttributes.set(attributes);
    }
    return attributes;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getThreadGroupAttribute(final Object name) {
    final Map<Object, Object> attributes = getThreadGroupAttributes();
    synchronized (attributes) {
      final T value = (T)attributes.get(name);
      if (value == null) {
        return (T)getDefaultAttribute(name);
      }
      return value;
    }
  }

  public static Map<Object, Object> getThreadGroupAttributes() {
    synchronized (threadGroupAttributes) {
      Map<Object, Object> attributes = null;
      final Thread thread = Thread.currentThread();
      final ThreadGroup threadGroup = thread.getThreadGroup();
      if (threadGroup != null) {
        attributes = threadGroupAttributes.get(threadGroup);
      }
      if (attributes == null) {
        attributes = new HashMap<Object, Object>(defaultAttributes);
      }
      return attributes;
    }
  }

  public static void initialiseThreadGroup(final ThreadGroup threadGroup) {
    final Map<Object, Object> attributes = getLocalAttributes();
    synchronized (threadGroupAttributes) {
      threadGroupAttributes.put(threadGroup, attributes);
    }
  }

  public static void setAttribute(final Object name, final Object value) {
    final Map<Object, Object> attributes = getLocalAttributes();
    synchronized (attributes) {
      attributes.put(name, value);
    }
  }

  public static void setAttributes(final Map<? extends Object, Object> values) {
    final Map<Object, Object> attributes = getLocalAttributes();
    synchronized (attributes) {
      attributes.putAll(values);
    }
  }

  public static void setDefaultAttribute(final Object name, final Object value) {
    synchronized (defaultAttributes) {
      defaultAttributes.put(name, value);
    }
  }

  public static void setDefaultAttributes(final Map<? extends Object, Object> values) {
    synchronized (defaultAttributes) {
      defaultAttributes.putAll(values);
    }
  }
}
