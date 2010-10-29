package com.revolsys.filter;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;

public class StaticMethodFilter<T> implements Filter<T> {
  private Method method;

  private Class<?> methodClass;

  private String methodName;

  private Object[] args;

  public StaticMethodFilter() {
  }

  public StaticMethodFilter(
    final Class<?> methodClass,
    final String methodName,
    Object... args) {
    this.methodClass = methodClass;
    this.methodName = methodName;
    this.args = args;
    initialize();
  }

  public boolean accept(
    final T object) {
    try {
      if (args.length == 0) {
        return (Boolean)method.invoke(null, object);
      } else {
        final Object[] newArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = object;
        return (Boolean)method.invoke(null, newArgs);
      }
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Class<?> getMethodClass() {
    return methodClass;
  }

  public String getMethodName() {
    return methodName;
  }

  @PostConstruct
  public void initialize() {
    final Method[] methods = methodClass.getMethods();
    for (final Method method : methods) {
      if (method.getName().equals(methodName)
        && method.getParameterTypes().length == 1 + args.length) {
        if (this.method != null) {
          throw new IllegalArgumentException("Multiple method match for "
            + methodClass + "." + methodName);
        }
        this.method = method;
      }
    }
    if (method == null) {
      throw new IllegalArgumentException("Method could not be found "
        + methodClass + "." + methodName);
    }
  }

  public void setMethodClass(
    final Class<?> methodClass) {
    this.methodClass = methodClass;
  }

  public void setMethodName(
    final String methodName) {
    this.methodName = methodName;
  }

  @Override
  public String toString() {
    return methodClass.getName() + "." + methodName;
  }
}
