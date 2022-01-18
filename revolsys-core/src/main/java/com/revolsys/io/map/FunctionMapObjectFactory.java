package com.revolsys.io.map;

import java.util.function.Function;

import com.revolsys.collection.map.MapEx;

public class FunctionMapObjectFactory extends AbstractMapObjectFactory {
  private final Function<MapEx, Object> function;

  public FunctionMapObjectFactory(final String typeName, final String description,
    final Function<MapEx, Object> function) {
    super(typeName, description);
    this.function = function;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V mapToObject(final MapEx properties) {
    return (V)this.function.apply(properties);
  }
}
