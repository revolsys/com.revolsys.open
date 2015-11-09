package com.revolsys.converter.string;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.gis.converter.string.BoundingBoxStringConverter;
import com.revolsys.gis.converter.string.GeometryStringConverter;

public class StringConverterRegistry {
  public static StringConverterRegistry instance = new StringConverterRegistry();

  public static void clearInstance() {
    instance = null;
  }

  public synchronized static StringConverterRegistry getInstance() {
    if (instance == null) {
      instance = new StringConverterRegistry();
    }
    return instance;
  }

  private final Map<Class<?>, StringConverter<?>> classConverterMap = new HashMap<Class<?>, StringConverter<?>>();

  public StringConverterRegistry() {
    addConverter(new IdentifierStringConverter());
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
    addConverter(new UriStringConverter());
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

  public void addConverter(final Class<?> clazz, final StringConverter<?> converter) {
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
  private StringConverter get(final Set<Class<?>> interfaces, final Class<?> clazz) {
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
