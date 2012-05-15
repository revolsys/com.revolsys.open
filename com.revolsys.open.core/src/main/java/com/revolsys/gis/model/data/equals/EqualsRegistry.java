package com.revolsys.gis.model.data.equals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.gis.data.model.DataObject;
import com.vividsolutions.jts.geom.Geometry;

public class EqualsRegistry implements Equals<Object> {

  private static final ObjectEquals DEFAULT_EQUALS = new ObjectEquals();

  public static final EqualsRegistry INSTANCE = new EqualsRegistry();

  private final Map<Class<?>, Equals<?>> classEqualsMap = new HashMap<Class<?>, Equals<?>>();

  public EqualsRegistry() {
    register(Object.class, DEFAULT_EQUALS);
    register(String.class, DEFAULT_EQUALS);
    register(Boolean.class, new BooleanEquals());
    register(Number.class, new NumberEquals());
    register(BigDecimal.class, new NumberEquals());
    register(BigInteger.class, new NumberEquals());
    register(Long.class, new NumberEquals());
    register(Byte.class, new NumberEquals());
    register(Integer.class, new NumberEquals());
    register(Short.class, new NumberEquals());
    register(Geometry.class, new Geometry3DExactEquals());
    register(Date.class, new DateEquals());
    register(java.sql.Date.class, new DateEquals());
    register(Timestamp.class, new DateEquals());
    register(Map.class, new MapEquals());
    register(List.class, new ListEquals());
    register(DataObject.class, new DataObjectEquals());
  }

  public boolean equals(final Object object1, final Object object2) {
    final Set<String> exclude = Collections.emptySet();
    return equals(object1, object2, exclude);
  }

  public boolean equals(
    final Object object1,
    final Object object2,
    final Collection<String> exclude) {
    if (object1 == null) {
      if (object2 == null) {
        return true;
      } else {
        return false;
      }
    } else if (object2 == null) {
      return false;
    } else {
      try {
        final Equals<Object> equals = getEquals(object1.getClass());
        return equals.equals(object1, object2, exclude);
      } catch (final ClassCastException e) {
        return false;
      }
    }
  }

  public Equals<Object> getEquals(final Class<?> clazz) {
    if (clazz == null) {
      return DEFAULT_EQUALS;
    } else {
      @SuppressWarnings("unchecked")
      Equals<Object> equals = (Equals<Object>)classEqualsMap.get(clazz);
      if (equals == null) {
        final Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null) {
          for (final Class<?> inter : interfaces) {
            equals = getEquals(inter);
            if (equals != null && equals != DEFAULT_EQUALS) {
              return equals;
            }
          }
        }
        return getEquals(clazz.getSuperclass());
      } else {
        return equals;
      }
    }
  }

  public void register(final Class<?> clazz, final Equals<?> equals) {
    classEqualsMap.put(clazz, equals);
    equals.setEqualsRegistry(this);
  }

  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
  }
}
