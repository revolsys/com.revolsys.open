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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;

import org.apache.commons.beanutils.MethodUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import com.revolsys.beans.NonWeakListener;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.beans.WeakPropertyChangeListener;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.io.ObjectWithProperties;

public final class Property {
  public static void addListener(final Object source, final Object listener) {
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

  public static void addListener(final Object source,
    final String propertyName, final Object listener) {
    final PropertyChangeListener propertyChangeListener = getPropertyChangeListener(listener);
    if (propertyChangeListener != null) {
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
      if (propertyChangeSupport == null) {
        if (source instanceof JComponent) {
          final JComponent component = (JComponent)source;
          component.addPropertyChangeListener(propertyName,
            propertyChangeListener);
        }
      } else {
        propertyChangeSupport.addPropertyChangeListener(propertyName,
          propertyChangeListener);
      }
    }
  }

  public static PropertyDescriptor descriptor(final Class<?> beanClass,
    final String name) {
    if (beanClass != null && StringUtils.hasText(name)) {
      try {
        final BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        final PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < props.length; i++) {
          final PropertyDescriptor property = props[i];
          if (property.getName().equals(name)) {
            return property;
          }
        }
      } catch (final IntrospectionException e) {
        ExceptionUtil.log(Property.class, e);
      }
    }
    return null;
  }

  public static void firePropertyChange(final Object source,
    final PropertyChangeEvent event) {
    final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(event);
    }
  }

  public static void firePropertyChange(final Object source,
    final String propertyName, final int index, final Object oldValue,
    final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
    if (propertyChangeSupport != null) {
      propertyChangeSupport.fireIndexedPropertyChange(propertyName, index,
        oldValue, newValue);
    }
  }

  public static void firePropertyChange(final Object source,
    final String propertyName, final Object oldValue, final Object newValue) {
    final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
    if (propertyChangeSupport != null) {
      propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(final Object object, final String key) {
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
        if (value == null || !StringUtils.hasText(subName)) {
          return (T)value;
        } else {
          return (T)get(value, subName);
        }
      }
    }
  }

  public static Class<?> getClass(final Class<?> beanClass, final String name) {
    final PropertyDescriptor propertyDescriptor = descriptor(beanClass, name);
    if (propertyDescriptor == null) {
      return null;
    } else {
      return propertyDescriptor.getPropertyType();
    }
  }

  public static Class<?> getClass(final Object object, final String fieldName) {
    if (object == null) {
      return null;
    } else {
      final Class<?> objectClass = object.getClass();
      final Class<?> fieldClass = getClass(objectClass, fieldName);
      return fieldClass;
    }
  }

  public static Double getDouble(final ObjectWithProperties object,
    final String key) {
    if (object == null) {
      return null;
    } else {
      final Object value = object.getProperty(key);
      return StringConverterRegistry.toObject(Double.class, value);
    }
  }

  public static double getDouble(final ObjectWithProperties object,
    final String key, final double defaultValue) {
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

  public static Integer getInteger(final ObjectWithProperties object,
    final String key) {
    if (object == null) {
      return null;
    } else {
      final Object value = object.getProperty(key);
      return StringConverterRegistry.toObject(Integer.class, value);
    }
  }

  public static int getInteger(final ObjectWithProperties object,
    final String key, final int defaultValue) {
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

  public static PropertyChangeListener getPropertyChangeListener(
    final Object listener) {
    if (listener instanceof PropertyChangeListener) {
      final PropertyChangeListener propertyChangeListener = (PropertyChangeListener)listener;
      if (propertyChangeListener instanceof NonWeakListener) {
        return propertyChangeListener;
      } else {
        final WeakPropertyChangeListener weakListener = new WeakPropertyChangeListener(
          propertyChangeListener);
        return weakListener;
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getSimple(final Object object, final String key) {
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

  public static String getString(final ObjectWithProperties object,
    final String key) {
    if (object == null) {
      return null;
    } else {
      final Object value = object.getProperty(key);
      return StringConverterRegistry.toObject(String.class, value);
    }
  }

  public static String getString(final ObjectWithProperties object,
    final String key, final String defaultValue) {
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

  @SuppressWarnings("unchecked")
  public static <V> V invoke(final Object object, final String methodName,
    final Object... parameterArray) {
    try {
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        return (V)MethodUtils.invokeStaticMethod(clazz, methodName,
          parameterArray);
      } else {
        return (V)MethodUtils.invokeMethod(object, methodName, parameterArray);
      }
    } catch (final InvocationTargetException e) {
      return (V)ExceptionUtil.throwCauseException(e);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to invoke "
        + toString(object, methodName, parameterArray), e);
    }
  }

  public static boolean isEmpty(final Object value) {
    if (value == null) {
      return true;
    } else if (value instanceof CharSequence) {
      final CharSequence charSequence = (CharSequence)value;
      return !StringUtils.hasText(charSequence);
    } else {
      return false;
    }
  }

  public static PropertyChangeSupport propertyChangeSupport(final Object object) {
    if (object instanceof PropertyChangeSupport) {
      return (PropertyChangeSupport)object;
    } else if (object instanceof PropertyChangeSupportProxy) {
      final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)object;
      return proxy.getPropertyChangeSupport();
    } else {
      return null;
    }
  }

  public static Method readMethod(final Class<?> beanClass, final String name) {
    final PropertyDescriptor descriptor = descriptor(beanClass, name);
    if (descriptor != null) {
      return descriptor.getReadMethod();
    } else {
      return null;
    }
  }

  public static Method readMethod(final Object object, final String name) {
    return readMethod(object.getClass(), name);
  }

  public static void removeAllListeners(final Component component) {
    for (final PropertyChangeListener listener : component.getPropertyChangeListeners()) {
      if (listener instanceof PropertyChangeListenerProxy) {
        final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
        final String propertyName = proxy.getPropertyName();
        component.removePropertyChangeListener(propertyName, listener);
      }
      component.removePropertyChangeListener(listener);
    }
  }

  public static void removeAllListeners(final Object object) {
    if (object instanceof Component) {
      final Component component = (Component)object;
      removeAllListeners(component);
    }
    if (object instanceof PropertyChangeSupportProxy) {
      final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)object;

      final PropertyChangeSupport propertyChangeSupport = proxy.getPropertyChangeSupport();
      for (final PropertyChangeListener listener : propertyChangeSupport.getPropertyChangeListeners()) {
        if (listener instanceof PropertyChangeListenerProxy) {
          final PropertyChangeListenerProxy listenerProxy = (PropertyChangeListenerProxy)listener;
          final String propertyName = listenerProxy.getPropertyName();
          propertyChangeSupport.removePropertyChangeListener(propertyName,
            listener);
        }
        propertyChangeSupport.removePropertyChangeListener(listener);
      }
    }

  }

  public static void removeAllListeners(
    final PropertyChangeSupport propertyChangeSupport) {
    for (final PropertyChangeListener listener : propertyChangeSupport.getPropertyChangeListeners()) {
      if (listener instanceof PropertyChangeListenerProxy) {
        final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
        final String propertyName = proxy.getPropertyName();
        propertyChangeSupport.removePropertyChangeListener(propertyName,
          listener);
      }
      propertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

  public static void removeListener(final Object source, final Object listener) {
    if (listener instanceof PropertyChangeListener) {
      final PropertyChangeListener propertyChangeListener = (PropertyChangeListener)listener;
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
      if (propertyChangeSupport != null) {
        for (final PropertyChangeListener otherListener : propertyChangeSupport.getPropertyChangeListeners()) {
          if (otherListener == propertyChangeListener) {
            propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
          } else if (otherListener instanceof WeakPropertyChangeListener) {
            final WeakPropertyChangeListener weakListener = (WeakPropertyChangeListener)otherListener;
            final PropertyChangeListener listenerReference = weakListener.getListener();
            if (listenerReference == null
              || listenerReference == propertyChangeListener) {
              propertyChangeSupport.removePropertyChangeListener(propertyChangeListener);
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
            if (listenerReference == null
              || listenerReference == propertyChangeListener) {
              component.removePropertyChangeListener(propertyChangeListener);
            }
          }
        }
      }
    }
  }

  public static void removeListener(final Object source,
    final String propertyName, final Object listener) {
    if (listener instanceof PropertyChangeListener) {
      final PropertyChangeListener propertyChangeListener = (PropertyChangeListener)listener;
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(source);
      if (propertyChangeSupport != null) {
        for (final PropertyChangeListener otherListener : propertyChangeSupport.getPropertyChangeListeners()) {
          if (otherListener == propertyChangeListener) {
            propertyChangeSupport.removePropertyChangeListener(propertyName,
              propertyChangeListener);
          } else if (otherListener instanceof WeakPropertyChangeListener) {
            final WeakPropertyChangeListener weakListener = (WeakPropertyChangeListener)otherListener;
            final PropertyChangeListener listenerReference = weakListener.getListener();
            if (listenerReference == null
              || listenerReference == propertyChangeListener) {
              propertyChangeSupport.removePropertyChangeListener(propertyName,
                propertyChangeListener);
            }
          }
        }
      }
      if (source instanceof Component) {
        final Component component = (Component)source;
        for (final PropertyChangeListener otherListener : component.getPropertyChangeListeners()) {
          if (otherListener == propertyChangeListener) {
            component.removePropertyChangeListener(propertyName,
              propertyChangeListener);
          } else if (otherListener instanceof WeakPropertyChangeListener) {
            final WeakPropertyChangeListener weakListener = (WeakPropertyChangeListener)otherListener;
            final PropertyChangeListener listenerReference = weakListener.getListener();
            if (listenerReference == null
              || listenerReference == propertyChangeListener) {
              component.removePropertyChangeListener(propertyName,
                propertyChangeListener);
            }
          }
        }
      }
    }
  }

  public static void set(final Object object,
    final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> property : properties.entrySet()) {
        final String propertyName = property.getKey();
        final Object value = property.getValue();
        try {
          set(object, propertyName, value);
        } catch (final Throwable e) {
          ExceptionUtil.log(Property.class, "Unable to set property "
            + propertyName, e);
        }
      }
    }
  }

  public static void set(final Object object, final String propertyName,
    final Object value) {
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

  public static String toString(final Object object, final String methodName,
    final List<Object> parameters) {
    final StringBuffer string = new StringBuffer();

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

  public static String toString(final Object object, final String methodName,
    final Object... parameters) {
    return toString(object, methodName, Arrays.asList(parameters));
  }

  private Property() {
  }

}
