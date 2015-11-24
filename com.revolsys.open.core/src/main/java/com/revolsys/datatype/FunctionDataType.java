package com.revolsys.datatype;

import java.util.function.Function;

public class FunctionDataType extends AbstractDataType {
  private final Function<Object, ?> toObjectFunction;

  private final Function<Object, String> toStringFunction;

  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requiresQuotes,
    final Function<Object, ?> function) {
    this(name, javaClass, requiresQuotes, function, null);
  }

  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requiresQuotes,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction) {
    super(name, javaClass, requiresQuotes);
    this.toObjectFunction = toObjectFunction;
    if (toStringFunction == null) {
      this.toStringFunction = Object::toString;
    } else {
      this.toStringFunction = toStringFunction;
    }
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> function) {
    this(name, javaClass, true, function);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction) {
    this(name, javaClass, true, toObjectFunction, toStringFunction);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    return this.toObjectFunction.apply(value);
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return (String)value;
    } else {
      return this.toStringFunction.apply(value);
    }
  }
}
