package com.revolsys.datatype;

import java.util.Collection;
import java.util.function.Function;

import com.revolsys.geometry.model.Geometry;

public class GeometryDataType<G extends Geometry> extends AbstractDataType {

  private final Function<Object, ? extends Geometry> toObjectFunction;

  public GeometryDataType(final Class<G> javaClass,
    final Function<Object, ? extends Geometry> toObjectFunction) {
    super(javaClass.getSimpleName(), javaClass, true);
    this.toObjectFunction = toObjectFunction;
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return ((Geometry)value1).equalsExact((Geometry)value2);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    return ((Geometry)value1).equalsExact((Geometry)value2);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    return this.toObjectFunction.apply(value);
  }

}
