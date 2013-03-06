package com.revolsys.swing.map.layer;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodLayerFactory<T extends Layer> implements
  LayerFactory<T> {

  private String description;

  private String typeName;

  private Object object;

  private String methodName;

  public String getDescription() {
    return description;
  }

  public String getTypeName() {
    return typeName;
  }

  public InvokeMethodLayerFactory(String typeName, String description,
    Object object, String methodName) {
    this.typeName = typeName;
    this.description = description;
    this.object = object;
    this.methodName = methodName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T createLayer(Map<String, Object> properties) {
    try {
      if (object instanceof Class<?>) {
        Class<?> clazz = (Class<?>)object;
        return (T)MethodUtils.invokeStaticMethod(clazz, methodName,
          properties);
      } else {
        return (T)MethodUtils.invokeMethod(object, methodName, properties);
      }
    } catch (NoSuchMethodException e) {
      return ExceptionUtil.throwUncheckedException(e);
    } catch (IllegalAccessException e) {
      return ExceptionUtil.throwUncheckedException(e);
    } catch (InvocationTargetException e) {
      return ExceptionUtil.throwCauseException(e);
    }
  }

  @Override
  public String toString() {
    return description;
  }
}
