package com.revolsys.filter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

public class StaticMethodValuesFilter<T> implements Filter<T> {
  private Method method;

  private Class<?> methodClass;

  private String methodName;

  private Collection<? extends Object> values;

  public StaticMethodValuesFilter() {
  }

  public StaticMethodValuesFilter(final Class<?> methodClass,
    final String methodName, final Collection<? extends Object> values) {
    this.methodClass = methodClass;
    this.methodName = methodName;
    this.values = values;
    initialize();
  }

  public StaticMethodValuesFilter(final Class<?> methodClass,
    final String methodName, final Object... values) {
    this(methodClass, methodName, Arrays.asList(values));
  }

  public boolean accept(final T object) {
    final Object value = getValue(object);
    return values.contains(value);
  }

  public Class<?> getMethodClass() {
    return methodClass;
  }

  public String getMethodName() {
    return methodName;
  }

  public Object getValue(final Object object) {
    try {
      return method.invoke(null, object);
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Collection<? extends Object> getValues() {
    return values;
  }

  @PostConstruct
  public void initialize() {
    final Method[] methods = methodClass.getMethods();
    for (final Method method : methods) {
      final String name = method.getName();
      if (name.equals(methodName) && method.getParameterTypes().length == 1) {
        if (this.method != null) {
          throw new IllegalArgumentException("Multiple method match for "
            + methodClass + "." + methodName);
        }
        this.method = method;
      }
    }
    if (this.method == null) {
      throw new IllegalArgumentException("No method match for " + methodClass
        + "." + methodName);
    }
  }

  public void setMethodClass(final Class<?> methodClass) {
    this.methodClass = methodClass;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }

  public void setValues(final Collection<? extends Object> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    if (values.size() == 1) {
      return methodClass.getName() + "." + methodName + "(object)="
        + values.iterator().next();
    } else {
      return methodClass.getName() + "." + methodName + "(object) in " + values;
    }
  }
}
