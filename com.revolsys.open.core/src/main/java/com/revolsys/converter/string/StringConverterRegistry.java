package com.revolsys.converter.string;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.revolsys.data.types.DataType;
import com.revolsys.gis.converter.string.BoundingBoxStringConverter;
import com.revolsys.gis.converter.string.GeometryStringConverter;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.util.CollectionUtil;

public class StringConverterRegistry {

  public static void clearInstance() {
    instance = null;
  }

  public synchronized static StringConverterRegistry getInstance() {
    if (instance == null) {
      instance = new StringConverterRegistry();
    }
    return instance;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public static <V> V toObject(final Class valueClass, final Object value) {
    if (value == null) {
      return null;
    } else {
      final StringConverter<Object> converter = StringConverterRegistry.getInstance()
          .getConverter(valueClass);
      if (converter == null) {
        return (V)value;
      } else {
        return (V)converter.toObject(value);
      }
    }
  }

  @SuppressWarnings({
    "unchecked"
  })
  public static <V> V toObject(final DataType dataType, final Object value) {
    final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
    return (V)toObject(dataTypeClass, value);
  }

  public static Object toObject(final Object value) {
    if (value == null) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      final Class<Object> valueClass = (Class<Object>)value.getClass();
      return toObject(valueClass, value);
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public static String toString(final Class valueClass, final Object value) {
    if (value == null) {
      return null;
    } else {
      final StringConverterRegistry registry = StringConverterRegistry.getInstance();
      final StringConverter<Object> converter = registry.getConverter(valueClass);
      if (converter == null) {
        return value.toString();
      } else {
        return converter.toString(value);
      }
    }
  }

  public static String toString(final DataType dataType, final Object value) {
    @SuppressWarnings("unchecked")
    final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
    return toString(dataTypeClass, value);
  }

  @SuppressWarnings("unchecked")
  public static String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Class<Object> valueClass = (Class<Object>)value.getClass();
      return toString(valueClass, value);
    }
  }

  public static StringConverterRegistry instance = new StringConverterRegistry();

  private final Map<Class<?>, StringConverter<?>> classConverterMap = new HashMap<Class<?>, StringConverter<?>>();

  public StringConverterRegistry() {
    addConverter(new BigDecimalStringConverter());
    addConverter(new BigIntegerStringConverter());
    addConverter(new BooleanStringConverter());
    addConverter(new ByteStringConverter());
    addConverter(new DoubleStringConverter());
    addConverter(new FloatStringConverter());
    addConverter(new DateStringConverter());
    addConverter(new TimestampStringConverter());
    addConverter(new DateTimeStringConverter());
    addConverter(new IntegerStringConverter());
    addConverter(new LongStringConverter());
    addConverter(new ShortStringConverter());
    addConverter(new StringStringConverter());
    addConverter(new UrlStringConverter());
    final GeometryStringConverter geometryConverter = new GeometryStringConverter();
    addConverter(geometryConverter);
    addConverter(Point.class, geometryConverter);
    addConverter(LineString.class, geometryConverter);
    addConverter(Polygon.class, geometryConverter);
    addConverter(MultiPoint.class, geometryConverter);
    addConverter(MultiLineString.class, geometryConverter);
    addConverter(MultiPolygon.class, geometryConverter);
    addConverter(new ListStringConverter());
    addConverter(new ColorStringConverter());
    addConverter(new MeasureStringConverter());
    addConverter(new BoundingBoxStringConverter());
  }

  public void addConverter(final Class<?> clazz,
    final StringConverter<?> converter) {
    this.classConverterMap.put(clazz, converter);
  }

  public void addConverter(final StringConverter<?> converter) {
    addConverter(converter.getConvertedClass(), converter);
  }

  @SuppressWarnings({
    "rawtypes"
  })
  private StringConverter get(final Set<Class<?>> interfaces) {
    StringConverter converter = null;
    for (int i = 0; i < interfaces.size(); i++) {
      final Class<?> interfaceClass = CollectionUtil.get(interfaces, i);
      converter = get(interfaces, interfaceClass);
      if (converter != null) {
        this.classConverterMap.put(interfaceClass, converter);
        return converter;
      }
    }
    return converter;
  }

  @SuppressWarnings("rawtypes")
  private StringConverter get(final Set<Class<?>> interfaces,
    final Class<?> clazz) {
    StringConverter converter = null;
    if (clazz != null) {
      converter = this.classConverterMap.get(clazz);
      if (converter == null) {
        for (final Class<?> interfaceClass : clazz.getInterfaces()) {
          interfaces.add(interfaceClass);
        }
        final Class<?> superClass = clazz.getSuperclass();
        converter = get(interfaces, superClass);
      }
    }
    if (converter != null) {
      this.classConverterMap.put(clazz, converter);
    }
    return converter;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <T> StringConverter<T> getConverter(final Class<T> clazz) {

    StringConverter converter = null;
    if (clazz != null) {
      converter = this.classConverterMap.get(clazz);
      if (converter == null) {
        final Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        converter = get(interfaces, clazz);
        if (converter == null) {
          converter = get(interfaces);
        }
      }
    }
    return converter;
  }

  @SuppressWarnings("unchecked")
  public <T> StringConverter<T> getConverter(final Object object) {
    if (object == null) {
      return new NullStringConverter<T>();
    } else {
      final Class<T> clazz = (Class<T>)object.getClass();
      return getConverter(clazz);
    }
  }
}
