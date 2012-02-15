package com.revolsys.gis.graph.attribute;

import java.lang.reflect.Method;

public class InvokeMethodObjectAttributeProxy<T, O> extends
  AbstractObjectAttributeProxy<T, O> {
  /** The method to invoke. */
  private Method method;

  /** The object to invoke the method on. */
  private Object object;

  private Object[] parameters;

  /**
   * Construct a new InvokeMethodRunnable.
   * 
   * @param methodName The name of the method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public InvokeMethodObjectAttributeProxy(final Class<?> clazz,
    final String methodName, final Class<?> parameterClass) {
    try {
      method = clazz.getMethod(methodName, parameterClass);
    } catch (final Throwable e) {
      throw new RuntimeException(e);
    }
    if (method == null) {
      throw new IllegalArgumentException("Method could not be found " + clazz
        + "." + methodName);
    }
  }

  /**
   * Construct a new InvokeMethodRunnable.
   * 
   * @param methodName The name of the method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public InvokeMethodObjectAttributeProxy(final Class<?> clazz,
    final String methodName, final Class<?> parameterClass,
    final Object... parameters) {
    try {
      final Class<?>[] parameterClasses = new Class<?>[parameters.length + 1];
      parameterClasses[0] = parameterClass;
      for (int i = 0; i < parameters.length; i++) {
        final Object parameter = parameters[i];
        parameterClasses[i + 1] = parameter.getClass();
      }
      for (final Method method : clazz.getMethods()) {
        if (method.getName().equals(methodName)) {
          final Class<?>[] parameterTypes = method.getParameterTypes();
          if (parameterTypes.length == parameterClasses.length) {
            boolean matched = true;
            for (int i = 0; i < parameterTypes.length; i++) {
              final Class<?> methodParamClass = parameterTypes[i];
              final Class<?> parameterClsss = parameterClasses[i];
              if (parameterClsss != null) {
                // TODO handle primitive types
                if (!methodParamClass.isAssignableFrom(parameterClsss)) {
                  matched = false;
                }
              }
            }
            if (matched) {
              this.method = method;
            }
          }
        }
      }
      this.parameters = parameters;
    } catch (final Throwable e) {
      throw new RuntimeException(e);
    }
    if (method == null) {
      throw new IllegalArgumentException("Method could not be found " + clazz
        + "." + methodName);
    }
  }

  public InvokeMethodObjectAttributeProxy(final Object object,
    final String methodName, final Class<?> parameterClass) {
    this(object.getClass(), methodName, parameterClass);
    this.object = object;
  }

  @Override
  public T createValue(final O object) {
    try {
      if (parameters == null) {
        return (T)method.invoke(object, object);
      } else {
        final Object[] parameters = new Object[this.parameters.length + 1];
        parameters[0] = object;
        for (int i = 0; i < this.parameters.length; i++) {
          final Object parameter = this.parameters[i];
          parameters[i + 1] = parameter;
        }
        return (T)method.invoke(object, parameters);
      }
    } catch (final Throwable e) {
      throw new RuntimeException(e);
    }
  }

}
