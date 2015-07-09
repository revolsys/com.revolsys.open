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

  public StaticMethodFilter(final Class<?> methodClass, final String methodName,
    final Object... args) {
    this.methodClass = methodClass;
    this.methodName = methodName;
    this.args = args;
    initialize();
  }

  @Override
  public boolean accept(final T object) {
    try {
      if (this.args.length == 0) {
        return (Boolean)this.method.invoke(null, object);
      } else {
        final Object[] newArgs = new Object[this.args.length + 1];
        System.arraycopy(this.args, 0, newArgs, 0, this.args.length);
        newArgs[this.args.length] = object;
        return (Boolean)this.method.invoke(null, newArgs);
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
    return this.methodClass;
  }

  public String getMethodName() {
    return this.methodName;
  }

  @PostConstruct
  public void initialize() {
    final Method[] methods = this.methodClass.getMethods();
    for (final Method method : methods) {
      if (method.getName().equals(this.methodName)
        && method.getParameterTypes().length == 1 + this.args.length) {
        if (this.method != null) {
          throw new IllegalArgumentException(
            "Multiple method match for " + this.methodClass + "." + this.methodName);
        }
        this.method = method;
      }
    }
    if (this.method == null) {
      throw new IllegalArgumentException(
        "Method could not be found " + this.methodClass + "." + this.methodName);
    }
  }

  public void setMethodClass(final Class<?> methodClass) {
    this.methodClass = methodClass;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }

  @Override
  public String toString() {
    return this.methodClass.getName() + "." + this.methodName;
  }
}
