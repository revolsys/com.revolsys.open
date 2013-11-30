package com.revolsys.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
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

import org.apache.commons.beanutils.MethodUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.gis.data.model.DataObject;

public final class Property {
  public static void addListener(final Object object,
    final PropertyChangeListener listener) {
    if (listener != null) {
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(object);
      if (propertyChangeSupport != null) {
        propertyChangeSupport.addPropertyChangeListener(listener);
      }
    }
  }

  public static void addListener(final Object object,
    final String propertyName, final PropertyChangeListener listener) {
    if (listener != null) {
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(object);
      if (propertyChangeSupport != null) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
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

  @SuppressWarnings("unchecked")
  public static <T> T get(final Object object, final String key) {
    if (object == null) {
      return null;
    } else {
      if (object instanceof DataObject) {
        final DataObject dataObject = (DataObject)object;
        return dataObject.getValueByPath(key);
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
    if (object instanceof PropertyChangeSupportProxy) {
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

  public static void removeListener(final Object object,
    final PropertyChangeListener listener) {
    if (listener != null) {
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(object);
      if (propertyChangeSupport != null) {
        propertyChangeSupport.removePropertyChangeListener(listener);
      }
    }
  }

  public static void removeListener(final Object object,
    final String propertyName, final PropertyChangeListener listener) {
    if (listener != null) {
      final PropertyChangeSupport propertyChangeSupport = propertyChangeSupport(object);
      if (propertyChangeSupport != null) {
        propertyChangeSupport.removePropertyChangeListener(propertyName,
          listener);
      }
    }
  }

  public static void set(final Object object,
    final Map<String, ? extends Object> properties) {
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

  public static void set(final Object object, final String propertyName,
    final Object value) {
    if (object != null) {
      if (object instanceof DataObject) {
        final DataObject dataObject = (DataObject)object;
        dataObject.setValueByPath(propertyName, value);
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

  public static void removeAllListeners(
    final PropertyChangeSupport propertyChangeSupport) {
    for (final PropertyChangeListener listener : propertyChangeSupport.getPropertyChangeListeners()) {
      if (listener instanceof PropertyChangeListenerProxy) {
        final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)listener;
        propertyChangeSupport.removePropertyChangeListener(
          proxy.getPropertyName(), listener);
      }
      propertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

}
