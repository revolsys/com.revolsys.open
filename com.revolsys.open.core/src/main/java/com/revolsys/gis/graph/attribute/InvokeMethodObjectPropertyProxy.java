package com.revolsys.gis.graph.attribute;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;

import com.revolsys.properties.AbstractObjectPropertyProxy;

public class InvokeMethodObjectPropertyProxy<T, O> extends AbstractObjectPropertyProxy<T, O>
  implements Externalizable {

  private static final long serialVersionUID = 1L;

  private Class<?> clazz;

  /** The method to invoke. */
  private Method method;

  private String methodName;

  private Class<?> parameterClass;

  private Object[] parameters;

  private Object object;

  public InvokeMethodObjectPropertyProxy() {
  }

  public InvokeMethodObjectPropertyProxy(final Object object, final String methodName,
    final Class<?> parameterClass, final Object... parameters) {
    if (object instanceof Class) {
      this.clazz = (Class<?>)object;
    } else {
      this.object = object;
      this.clazz = object.getClass();
    }
    this.methodName = methodName;
    this.parameterClass = parameterClass;
    this.parameters = parameters;
    init();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T createValue(final O value) {
    try {
      if (this.parameters == null) {
        return (T)this.method.invoke(this.object, value);
      } else {
        final Object[] parameters = new Object[this.parameters.length + 1];
        parameters[0] = value;
        for (int i = 0; i < this.parameters.length; i++) {
          final Object parameter = this.parameters[i];
          parameters[i + 1] = parameter;
        }
        return (T)this.method.invoke(this.object, parameters);
      }
    } catch (final Throwable e) {
      throw new RuntimeException(e);
    }
  }

  public void init() {
    try {
      final Class<?>[] parameterClasses = new Class<?>[this.parameters.length + 1];
      parameterClasses[0] = this.parameterClass;
      for (int i = 0; i < this.parameters.length; i++) {
        final Object parameter = this.parameters[i];
        parameterClasses[i + 1] = parameter.getClass();
      }
      for (final Method method : this.clazz.getMethods()) {
        if (method.getName().equals(this.methodName)) {
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
    if (this.method == null) {
      throw new IllegalArgumentException(
        "Method could not be found " + this.clazz + "." + this.methodName);
    }
  }

  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    final String className = (String)in.readObject();
    this.clazz = Class.forName(className);

    this.methodName = (String)in.readObject();

    final String parameterClassName = (String)in.readObject();
    this.parameterClass = Class.forName(parameterClassName);

    this.parameters = (Object[])in.readObject();

    init();
  }

  @Override
  public String toString() {
    return this.method.toString();
  }

  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeObject(this.clazz.getName());
    out.writeObject(this.methodName);
    out.writeObject(this.parameterClass.getName());
    out.writeObject(this.parameters);
  }

}
