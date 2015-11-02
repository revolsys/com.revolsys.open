package com.revolsys.util;

import java.awt.Component;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.swing.JComponent;

import org.apache.commons.beanutils.MethodUtils;
import org.springframework.core.annotation.AnnotationUtils;

import com.revolsys.beans.NonWeakListener;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.beans.WeakPropertyChangeListener;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.equals.Equals;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.util.function.Function2;

public interface Property {
  static void addListener(final Object source, final Object listener) {
    if (source != null) {
      final PropertyChangeListener propertyChangeListener = getPropertyChangeListener(listener);
      if (propertyChangeListener != null) {
        final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
        if (propertyChangeSupport == null) {
          if (source instanceof JComponent) {
            final JComponent component = (JComponent)source;
            component.addPropertyChangeListener(propertyChangeListener);
          }
        } else {
          propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
        }
      }
    }
  }

  static void addListener(final Object source, final PropertyChangeListener listener) {
    addListener(source, (Object)listener);
  }

  static void addListener(final Object source, final String propertyName, final Object listener) {
    final PropertyChangeListener propertyChangeListener = getPropertyChangeListener(listener);
    if (propertyChangeListener != null) {
      if (source != null) {
        final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
        if (propertyChangeSupport == null) {
          if (source instanceof JComponent) {
            final JComponent component = (JComponent)source;
            component.addPropertyChangeListener(propertyName, propertyChangeListener);
          }
        } else {
          propertyChangeSupport.addPropertyChangeListener(propertyName, propertyChangeListener);
        }
      }
    }
  }

  static void addListener(final Object source, final String propertyName,
    final PropertyChangeListener listener) {
    addListener(source, propertyName, (Object)listener);
  }

  static PropertyDescriptor descriptor(final Class<?> beanClass, final String name) {
    if (beanClass != null && Property.hasValue(name)) {
      try {
        final BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        final PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
        for (final PropertyDescriptor property : props) {
          if (property.getName().equals(name)) {
            return property;
          }
        }
      } catch (final IntrospectionException e) {
        Exceptions.log(Property.class, e);
      }
    }
    return null;
  }

  static boolean equals(final Object object1, final Object object2, final String propertyName) {
    if (object1 == object2) {
      return true;
    } else if (object1 != null && object2 != null) {
      final Object value1 = getSimple(object1, propertyName);
      final Object value2 = getSimple(object2, propertyName);
      return Equals.equal(value1, value2);
    }
    return false;
  }

  static void firePropertyChange(final Object source, final PropertyChangeEvent event) {
    final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(event);
    }
  }

  static void firePropertyChange(final Object source, final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
    if (propertyChangeSupport != null) {
      propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
  }

  static void firePropertyChange(final Object source, final String propertyName,
    final Object oldValue, final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  @SuppressWarnings("unchecked")
  static <T> T get(final Object object, final String key) {
    if (object == null) {
      return null;
    } else {
      if (object instanceof Record) {
        final Record record = (Record)object;
        return record.getValueByPath(key);
      } else if (object instanceof Map) {
        final Map<String, ?> map = (Map<String, ?>)object;
        return (T)map.get(key);
      } else if (object instanceof Annotation) {
        final Annotation annotation = (Annotation)object;
        return (T)AnnotationUtils.getValue(annotation, key);
      } else {
        final String firstName = JavaBeanUtil.getFirstName(key);
        final String subName = JavaBeanUtil.getSubName(key);
        final Object value = JavaBeanUtil.getProperty(object, firstName);
        if (value == null || !Property.hasValue(subName)) {
          return (T)value;
        } else {
          return (T)get(value, subName);
        }
      }
    }
  }

  static boolean getBoolean(final Map<String, Object> map, final String key) {
    if (map == null) {
      return false;
    } else {
      final Object value = map.get(key);
      if (value == null) {
        return false;
      } else if (value instanceof Boolean) {
        final Boolean booleanValue = (Boolean)value;
        return booleanValue;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("Y") || stringValue.equals("1")
          || Boolean.parseBoolean(stringValue)) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  static boolean getBoolean(final ObjectWithProperties object, final String key) {
    if (object == null) {
      return false;
    } else {
      final Object value = object.getProperty(key);
      if (value == null) {
        return false;
      } else if (value instanceof Boolean) {
        final Boolean booleanValue = (Boolean)value;
        return booleanValue;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("Y") || stringValue.equals("1")
          || Boolean.parseBoolean(stringValue)) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  static Class<?> getClass(final Class<?> beanClass, final String name) {
    final PropertyDescriptor propertyDescriptor = descriptor(beanClass, name);
    if (propertyDescriptor == null) {
      return null;
    } else {
      return propertyDescriptor.getPropertyType();
    }
  }

  static Class<?> getClass(final Object object, final String fieldName) {
    if (object == null) {
      return null;
    } else {
      final Class<?> objectClass = object.getClass();
      final Class<?> fieldClass = getClass(objectClass, fieldName);
      return fieldClass;
    }
  }

  static Double getDouble(final ObjectWithProperties object, final String key) {
    if (object == null) {
      return null;
    } else {
      final Object value = object.getProperty(key);
      return StringConverterRegistry.toObject(Double.class, value);
    }
  }

  static double getDouble(final ObjectWithProperties object, final String key,
    final double defaultValue) {
    if (object == null) {
      return defaultValue;
    } else {
      final Object value = object.getProperty(key);
      if (value == null) {
        return defaultValue;
      } else {
        return StringConverterRegistry.toObject(Double.class, value);
      }
    }
  }

  static Integer getInteger(final ObjectWithProperties object, final String key) {
    if (object == null) {
      return null;
    } else {
      final Object value = object.getProperty(key);
      return StringConverterRegistry.toObject(Integer.class, value);
    }
  }

  static int getInteger(final ObjectWithProperties object, final String key,
    final int defaultValue) {
    if (object == null) {
      return defaultValue;
    } else {
      final Object value = object.getProperty(key);
      if (value == null) {
        return defaultValue;
      } else {
        return StringConverterRegistry.toObject(Integer.class, value);
      }
    }
  }

  @SuppressWarnings("unchecked")
  static PropertyChangeListener getPropertyChangeListener(final Object listener) {
    if (listener instanceof PropertyChangeListener) {
      final PropertyChangeListener propertyChangeListener = (PropertyChangeListener)listener;
      if (propertyChangeListener instanceof NonWeakListener) {
        return propertyChangeListener;
      } else {
        final WeakPropertyChangeListener weakListener = new WeakPropertyChangeListener(
          propertyChangeListener);
        return weakListener;
      }
    } else if (listener instanceof Consumer) {
      final Consumer<Object> consumer = (Consumer<Object>)listener;
      return (e) -> {
        final Object object = e.getNewValue();
        consumer.accept(object);
      };
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  static <T> T getSimple(final Object object, final String key) {
    if (object == null) {
      return null;
    } else {
      if (object instanceof Record) {
        final Record record = (Record)object;
        return record.getValue(key);
      } else if (object instanceof Map) {
        final Map<String, ?> map = (Map<String, ?>)object;
        return (T)map.get(key);
      } else if (object instanceof Annotation) {
        final Annotation annotation = (Annotation)object;
        return (T)AnnotationUtils.getValue(annotation, key);
      } else {
        final Object value = JavaBeanUtil.getProperty(object, key);
        return (T)value;
      }
    }
  }

  static String getString(final ObjectWithProperties object, final String key) {
    if (object == null) {
      return null;
    } else {
      final Object value = object.getProperty(key);
      return StringConverterRegistry.toObject(String.class, value);
    }
  }

  static String getString(final ObjectWithProperties object, final String key,
    final String defaultValue) {
    if (object == null) {
      return defaultValue;
    } else {
      final Object value = object.getProperty(key);
      if (value == null) {
        return defaultValue;
      } else {
        return StringConverterRegistry.toObject(String.class, value);
      }
    }
  }

  static boolean hasText(final CharSequence string) {
    final int length = string.length();
    for (int i = 0; i < length; i++) {
      final char character = string.charAt(i);
      if (!Character.isWhitespace(character)) {
        return true;
      }
    }
    return false;
  }

  static boolean hasValue(final CharSequence string) {
    if (string == null) {
      return false;
    } else {
      return hasText(string);
    }
  }

  static boolean hasValue(final Collection<?> collection) {
    if (collection == null || collection.isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  static boolean hasValue(final Emptyable value) {
    if (value == null) {
      return false;
    } else {
      return !value.isEmpty();
    }
  }

  static boolean hasValue(final Object value) {
    if (value == null) {
      return false;
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      return hasText(string);
    } else if (value instanceof Collection<?>) {
      final Collection<?> collection = (Collection<?>)value;
      return !collection.isEmpty();
    } else if (value instanceof Map<?, ?>) {
      final Map<?, ?> map = (Map<?, ?>)value;
      return !map.isEmpty();
    } else if (value instanceof Emptyable) {
      final Emptyable emptyable = (Emptyable)value;
      return !emptyable.isEmpty();
    } else {
      return true;
    }
  }

  static boolean hasValue(final Object[] array) {
    if (array == null || array.length > 1) {
      return false;
    } else {
      return true;
    }
  }

  static boolean hasValuesAll(final Object... values) {
    if (values == null || values.length == 0) {
      return false;
    } else {
      for (final Object value : values) {
        if (!hasValue(value)) {
          return false;
        }
      }
      return true;
    }
  }

  static boolean hasValuesAny(final Object... values) {
    if (values == null || values.length == 0) {
      return false;
    } else {
      for (final Object value : values) {
        if (hasValue(value)) {
          return true;
        }
      }
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  static <V> V invoke(final Object object, final String methodName,
    final Object... parameterArray) {
    try {
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        return (V)MethodUtils.invokeStaticMethod(clazz, methodName, parameterArray);
      } else {
        return (V)MethodUtils.invokeMethod(object, methodName, parameterArray);
      }
    } catch (final InvocationTargetException e) {
      return (V)Exceptions.throwCauseException(e);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to invoke " + toString(object, methodName, parameterArray),
        e);
    }
  }

  static boolean isChanged(final Object oldValue, final Object newValue) {
    final boolean oldHasValue = Property.hasValue(oldValue);
    final boolean newHasValue = Property.hasValue(newValue);
    if (oldHasValue) {
      if (newHasValue) {
        if (Equals.equal(oldValue, newValue)) {
          return false;
        } else {
          return true;
        }
      } else {
        return true;
      }
    } else {
      if (newHasValue) {
        return true;
      } else {
        return false;
      }
    }
  }

  static boolean isEmpty(final Emptyable value) {
    if (value == null) {
      return true;
    } else {
      return value.isEmpty();
    }
  }

  static boolean isEmpty(final Object value) {
    if (value == null) {
      return true;
    } else if (value instanceof CharSequence) {
      final CharSequence string = (CharSequence)value;
      return !hasText(string);
    } else if (value instanceof Collection<?>) {
      final Collection<?> collection = (Collection<?>)value;
      return collection.isEmpty();
    } else if (value instanceof Map<?, ?>) {
      final Map<?, ?> map = (Map<?, ?>)value;
      return map.isEmpty();
    } else if (value instanceof Emptyable) {
      final Emptyable emptyable = (Emptyable)value;
      return emptyable.isEmpty();
    } else {
      return false;
    }
  }

  static boolean isEmpty(final Object[] value) {
    if (value == null || value.length == 0) {
      return true;
    } else {
      return false;
    }
  }

  static <V> PropertyChangeListener newListener(final Consumer<V> consumer) {
    return (event) -> {
      @SuppressWarnings("unchecked")
      final V value = (V)event.getNewValue();
      consumer.accept(value);
    };
  }

  static <V> PropertyChangeListener newListener(final Function2<String, V, ?> function) {
    return (event) -> {
      final String propertyName = event.getPropertyName();
      @SuppressWarnings("unchecked")
      final V value = (V)event.getNewValue();
      function.apply(propertyName, value);
    };
  }

  static PropertyChangeSupport propertyChangeSupport(final Object object) {
    if (object instanceof PropertyChangeSupport) {
      return (PropertyChangeSupport)object;
    } else if (object instanceof PropertyChangeSupportProxy) {
      final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)object;
      return proxy.getPropertyChangeSupport();
    } else {
      return null;
    }
  }

  static Method readMethod(final Class<?> beanClass, final String name) {
    final PropertyDescriptor descriptor = descriptor(beanClass, name);
    if (descriptor != null) {
      return descriptor.getReadMethod();
    } else {
      return null;
    }
  }

  static Method readMethod(final Object object, final String name) {
    return readMethod(object.getClass(), name);
  }

  static void removeAllListeners(final Component component) {
    for (final PropertyChangeListener listener : component.getPropertyChangeListeners()) {
      if (listener instanceof PropertyChangeListenerProxy) {
        final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
        final String propertyName = proxy.getPropertyName();
        component.removePropertyChangeListener(propertyName, listener);
      }
      component.removePropertyChangeListener(listener);
    }
  }

  static void removeAllListeners(final Object object) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      removeAllListeners(component);
    }
    if (object instanceof PropertyChangeSupportProxy) {
      final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)object;

      final PropertyChangeSupport propertyChangeSupport = proxy.getPropertyChangeSupport();
      for (final PropertyChangeListener listener : propertyChangeSupport
        .getPropertyChangeListeners()) {
        if (listener instanceof PropertyChangeListenerProxy) {
          final PropertyChangeListenerProxy listenerProxy = (PropertyChangeListenerProxy)listener;
          final String propertyName = listenerProxy.getPropertyName();
          propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }
        propertyChangeSupport.removePropertyChangeListener(listener);
      }
    }

  }

  static void removeAllListeners(final PropertyChangeSupport propertyChangeSupport) {
    for (final PropertyChangeListener listener : propertyChangeSupport
      .getPropertyChangeListeners()) {
      if (listener instanceof PropertyChangeListenerProxy) {
        final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
        final String propertyName = proxy.getPropertyName();
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
      }
      propertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

  static void removeListener(final Object source, final Object listener) {
    if (source != null && listener instanceof PropertyChangeListener) {
      final PropertyChangeListener propertyChangeListener = (PropertyChangeListener)listener;
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
      if (propertyChangeSupport != null) {
        for (final PropertyChangeListener otherListener : propertyChangeSupport
          .getPropertyChangeListeners()) {
          if (otherListener == propertyChangeListener) {
            propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
          } else if (otherListener instanceof WeakPropertyChangeListener) {
            final WeakPropertyChangeListener weakListener = (WeakPropertyChangeListener)otherListener;
            final PropertyChangeListener listenerReference = weakListener.getListener();
            if (listenerReference == null || listenerReference == propertyChangeListener) {
              propertyChangeSupport.removePropertyChangeListener(weakListener);
            }
          }
        }
      }
      if (source instanceof Component) {
        final Component component = (Component)source;
        for (final PropertyChangeListener otherListener : component.getPropertyChangeListeners()) {
          if (otherListener == propertyChangeListener) {
            component.removePropertyChangeListener(propertyChangeListener);
          } else if (otherListener instanceof WeakPropertyChangeListener) {
            final WeakPropertyChangeListener weakListener = (WeakPropertyChangeListener)otherListener;
            final PropertyChangeListener listenerReference = weakListener.getListener();
            if (listenerReference == null || listenerReference == propertyChangeListener) {
              component.removePropertyChangeListener(weakListener);
            }
          }
        }
      }
    }
  }

  static void removeListener(final Object source, final String propertyName,
    final Object listener) {
    if (listener instanceof PropertyChangeListener) {
      final PropertyChangeListener propertyChangeListener = (PropertyChangeListener)listener;
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
      if (propertyChangeSupport != null) {
        for (final PropertyChangeListener otherListener : propertyChangeSupport
          .getPropertyChangeListeners()) {
          if (otherListener instanceof PropertyChangeListenerProxy) {
            final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)otherListener;
            final PropertyChangeListener proxyListener = proxy.getListener();

            final String proxyPropertyName = proxy.getPropertyName();
            if (proxyListener instanceof WeakPropertyChangeListener) {
              final WeakPropertyChangeListener weakListener = (WeakPropertyChangeListener)proxyListener;
              final PropertyChangeListener listenerReference = weakListener.getListener();
              if (listenerReference == null) {
                propertyChangeSupport.removePropertyChangeListener(proxyPropertyName, weakListener);
              } else if (proxyPropertyName.equals(propertyName)) {
                if (listenerReference == propertyChangeListener) {
                  propertyChangeSupport.removePropertyChangeListener(propertyName, weakListener);
                }
              }
            } else if (propertyChangeListener == proxyListener) {
              if (proxyPropertyName.equals(propertyName)) {
                propertyChangeSupport.removePropertyChangeListener(propertyName,
                  propertyChangeListener);
              }
            }
          } else if (otherListener instanceof WeakPropertyChangeListener) {
            final WeakPropertyChangeListener weakListener = (WeakPropertyChangeListener)otherListener;
            final PropertyChangeListener listenerReference = weakListener.getListener();
            if (listenerReference == null) {
              propertyChangeSupport.removePropertyChangeListener(weakListener);
            }
          }
        }
      }
      if (source instanceof Component) {
        final Component component = (Component)source;
        for (final PropertyChangeListener otherListener : component.getPropertyChangeListeners()) {
          if (otherListener == propertyChangeListener) {
            component.removePropertyChangeListener(propertyName, propertyChangeListener);
          } else if (otherListener instanceof WeakPropertyChangeListener) {
            final WeakPropertyChangeListener weakListener = (WeakPropertyChangeListener)otherListener;
            final PropertyChangeListener listenerReference = weakListener.getListener();
            if (listenerReference == null || listenerReference == propertyChangeListener) {
              component.removePropertyChangeListener(propertyName, propertyChangeListener);
            }
          }
        }
      }
    }
  }

  static void set(final Object object, final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> property : properties.entrySet()) {
        final String propertyName = property.getKey();
        final Object value = property.getValue();
        try {
          set(object, propertyName, value);
        } catch (final Throwable e) {
          Exceptions.log(Property.class, "Unable to set property " + propertyName, e);
        }
      }
    }
  }

  static void set(final Object object, final String propertyName, final Object value) {
    if (object != null) {
      if (object instanceof Record) {
        final Record record = (Record)object;
        record.setValueByPath(propertyName, value);
      } else if (object instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>)object;
        map.put(propertyName, value);
      } else {
        JavaBeanUtil.setProperty(object, propertyName, value);
      }
    }
  }

  static String toString(final Object object, final String methodName,
    final List<Object> parameters) {
    final StringBuilder string = new StringBuilder();

    if (object == null) {
    } else if (object instanceof Class<?>) {
      string.append(object);
      string.append('.');
    } else {
      string.append(object.getClass());
      string.append('.');
    }
    string.append(methodName);
    string.append('(');
    for (int i = 0; i < parameters.size(); i++) {
      if (i > 0) {
        string.append(',');
      }
      final Object parameter = parameters.get(i);
      if (parameter == null) {
        string.append("null");
      } else {
        string.append(parameter.getClass());
      }
    }
    string.append(')');
    string.append('\n');
    string.append(parameters);

    return string.toString();
  }

  static String toString(final Object object, final String methodName, final Object... parameters) {
    return toString(object, methodName, Arrays.asList(parameters));
  }
}
