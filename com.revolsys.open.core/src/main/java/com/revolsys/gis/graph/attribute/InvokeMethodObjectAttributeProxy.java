package com.revolsys.gis.graph.attribute;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;

public class InvokeMethodObjectAttributeProxy<T, O> extends
  AbstractObjectAttributeProxy<T, O> implements Externalizable {

  private static final long serialVersionUID = 1L;

  private Class<?> clazz;

  /** The method to invoke. */
  private Method method;

  private String methodName;

  private Class<?> parameterClass;

  private Object[] parameters;

  public InvokeMethodObjectAttributeProxy() {
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
    this.clazz = clazz;
    this.methodName = methodName;
    this.parameterClass = parameterClass;
    this.parameters = parameters;
    init();

  }

  @SuppressWarnings("unchecked")
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

  public void init() {
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
    } catch (final Throwable e) {
      throw new RuntimeException(e);
    }
    if (method == null) {
      throw new IllegalArgumentException("Method could not be found " + clazz
        + "." + methodName);
    }
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException,
    ClassNotFoundException {
    final String className = (String)in.readObject();
    clazz = Class.forName(className);

    methodName = (String)in.readObject();

    final String parameterClassName = (String)in.readObject();
    parameterClass = Class.forName(parameterClassName);

    parameters = (Object[])in.readObject();

    init();
  }

  @Override
  public String toString() {
    return method.toString();
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeObject(clazz.getName());
    out.writeObject(methodName);
    out.writeObject(parameterClass.getName());
    out.writeObject(parameters);
  }

}
