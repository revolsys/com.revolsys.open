package com.revolsys.datatype;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import com.revolsys.util.function.Function2;
import com.revolsys.util.function.Function3;

public class FunctionDataType extends AbstractDataType {
  public static FunctionDataType newToObjectEquals(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction,
    final Function2<? extends Object, ? extends Object, Boolean> equalsFunction) {
    return new FunctionDataType(name, javaClass, true, toObjectFunction, null, equalsFunction,
      null);
  }

  private final Function<Object, ?> toObjectFunction;

  private final Function<Object, String> toStringFunction;

  private final Function2<Object, Object, Boolean> equalsFunction;

  private final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> equalsExcludesFunction;

  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requiresQuotes,
    final Function<Object, ?> function) {
    this(name, javaClass, requiresQuotes, function, null, null, null);
  }

  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requiresQuotes,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction) {
    this(name, javaClass, requiresQuotes, toObjectFunction, toStringFunction, null, null);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requiresQuotes,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction,
    final Function2<?, ?, Boolean> equalsFunction,
    final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> equalsExcludesFunction) {
    super(name, javaClass, requiresQuotes);
    this.toObjectFunction = toObjectFunction;
    if (toStringFunction == null) {
      this.toStringFunction = Object::toString;
    } else {
      this.toStringFunction = toStringFunction;
    }
    if (equalsFunction == null) {
      if (equalsExcludesFunction == null) {
        this.equalsFunction = Object::equals;
      } else {
        this.equalsFunction = (value1, value2) -> {
          return equalsNotNull(value1, value2, Collections.emptySet());
        };
      }
    } else {
      this.equalsFunction = (Function2)equalsFunction;
    }
    if (equalsExcludesFunction == null) {
      if (equalsFunction == null) {
        this.equalsExcludesFunction = (value1, value2, excludeFieldNames) -> {
          return value1.equals(value2);
        };
      } else {
        this.equalsExcludesFunction = (value1, value2, excludeFieldNames) -> {
          return this.equalsFunction.apply(value1, value2);
        };
      }
    } else {
      this.equalsExcludesFunction = equalsExcludesFunction;
    }
  }

  public FunctionDataType(final String name, final Class<?> javaClass, final boolean requireQuotes,
    final Function<Object, ?> toObjectFunction,
    final Function2<? extends Object, ? extends Object, Boolean> equalsFunction) {
    this(name, javaClass, requireQuotes, toObjectFunction, null, equalsFunction, null);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> function) {
    this(name, javaClass, true, function);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction) {
    this(name, javaClass, true, toObjectFunction, toStringFunction);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction, final Function<Object, String> toStringFunction,
    final Function2<?, ?, Boolean> equalsFunction) {
    this(name, javaClass, true, toObjectFunction, toStringFunction, equalsFunction, null);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction,
    final Function2<? extends Object, ? extends Object, Boolean> equalsFunction,
    final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> equalsExcludesFunction) {
    this(name, javaClass, true, toObjectFunction, null, equalsFunction, equalsExcludesFunction);
  }

  public FunctionDataType(final String name, final Class<?> javaClass,
    final Function<Object, ?> toObjectFunction,
    final Function3<Object, Object, Collection<? extends CharSequence>, Boolean> equalsExcludesFunction) {
    this(name, javaClass, true, toObjectFunction, null, null, equalsExcludesFunction);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return this.equalsFunction.apply(value1, value2);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    return this.equalsExcludesFunction.apply(value1, value2, excludeFieldNames);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    return this.toObjectFunction.apply(value);
  }

  @Override
  public String toStringDo(final Object value) {
    return this.toStringFunction.apply(value);
  }
}
