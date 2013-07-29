package com.revolsys.swing.map.layer;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.io.map.AbstractMapObjectFactory;
import com.revolsys.util.ExceptionUtil;

public class InvokeMethodMapObjectFactory extends AbstractMapObjectFactory {

  private final Object object;

  private final String methodName;

  public InvokeMethodMapObjectFactory(final String typeName,
    final String description, final Object object, final String methodName) {
    super(typeName, description);
    this.object = object;
    this.methodName = methodName;
  }

  @Override
  public <V> V toObject(final Map<String, ? extends Object> properties) {
    try {
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        return (V)MethodUtils.invokeStaticMethod(clazz, methodName, properties);
      } else {
        return (V)MethodUtils.invokeMethod(object, methodName, properties);
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
