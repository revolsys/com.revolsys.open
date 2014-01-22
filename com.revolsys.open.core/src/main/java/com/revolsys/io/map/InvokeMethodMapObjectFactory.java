package com.revolsys.io.map;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodMapObjectFactory extends AbstractMapObjectFactory {

  private final Reference<Object> object;

  private final String methodName;

  public InvokeMethodMapObjectFactory(final String typeName,
    final String description, final Object object, final String methodName) {
    super(typeName, description);
    this.object = new WeakReference<Object>(object);
    this.methodName = methodName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toObject(final Map<String, ? extends Object> properties) {
    try {
      final Object object = this.object.get();
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        return (V)MethodUtils.invokeStaticMethod(clazz, this.methodName,
          properties);
      } else if (object != null) {
        return (V)MethodUtils.invokeMethod(object, this.methodName, properties);
      } else {
        return null;
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
