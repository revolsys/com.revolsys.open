package com.revolsys.swing.map.layer;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodLayerFactory<T extends Layer> extends
  AbstractLayerFactory<T> {

  private final Object object;

  private final String methodName;

  public InvokeMethodLayerFactory(final String typeName,
    final String description, final Object object, final String methodName) {
   super(typeName,description);
    this.object = object;
    this.methodName = methodName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T createLayer(final Map<String, Object> properties) {
    try {
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        return (T)MethodUtils.invokeStaticMethod(clazz, methodName, properties);
      } else {
        return (T)MethodUtils.invokeMethod(object, methodName, properties);
      }
    } catch (final NoSuchMethodException e) {
      return ExceptionUtil.throwUncheckedException(e);
    } catch (final IllegalAccessException e) {
      return ExceptionUtil.throwUncheckedException(e);
    } catch (final InvocationTargetException e) {
      return ExceptionUtil.throwCauseException(e);
    }
  }
}
