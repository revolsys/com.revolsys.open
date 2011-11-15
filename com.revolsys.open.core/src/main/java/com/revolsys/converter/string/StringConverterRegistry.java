package com.revolsys.converter.string;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.converter.string.GeometryStringConverter;
import com.revolsys.gis.data.model.types.DataType;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class StringConverterRegistry {

  public static String toString(DataType dataType, Object value) {
    if (value == null) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
      final StringConverter<Object> converter = StringConverterRegistry.INSTANCE.getConverter(dataTypeClass);
      if (converter == null) {
        return value.toString();
      } else {
        return converter.toString(value);
      }
    }
  }

  public static Object toObject(DataType dataType, Object value) {
    if (value == null) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
      final StringConverter<Object> converter = StringConverterRegistry.INSTANCE.getConverter(dataTypeClass);
      if (converter == null) {
        return value;
      } else {
        return converter.toObject(value);
      }
    }
  }

  public static final StringConverterRegistry INSTANCE = new StringConverterRegistry();

  private Map<Class<?>, StringConverter<?>> classConverterMap = new HashMap<Class<?>, StringConverter<?>>();

  @SuppressWarnings("unchecked")
  public <T> StringConverter<T> getConverter(Class<T> clazz) {
    return (StringConverter<T>)classConverterMap.get(clazz);
  }

  @SuppressWarnings("unchecked")
  public <T> StringConverter<T> getConverter(Object object) {
    if (object == null) {
      return new NullStringConverter<T>();
    } else {
      Class<T> clazz = (Class<T>)object.getClass();
      return getConverter(clazz);
    }
  }

  public StringConverterRegistry() {
    addConverter(new BigDecimalStringConverter());
    addConverter(new BigIntegerStringConverter());
    addConverter(new BooleanStringConverter());
    addConverter(new ByteStringConverter());
    addConverter(new DoubleStringConverter());
    addConverter(new FloatStringConverter());
    addConverter(new IntegerStringConverter());
    addConverter(new LongStringConverter());
    addConverter(new ShortStringConverter());
    addConverter(new StringStringConverter());
    final GeometryStringConverter geometryConverter = new GeometryStringConverter();
    addConverter(geometryConverter);
    addConverter(Point.class, geometryConverter);
    addConverter(LineString.class, geometryConverter);
    addConverter(Polygon.class, geometryConverter);
    addConverter(MultiPoint.class, geometryConverter);
    addConverter(MultiLineString.class, geometryConverter);
    addConverter(MultiPolygon.class, geometryConverter);
  }

  public void addConverter(StringConverter<?> converter) {
    addConverter(converter.getConvertedClass(), converter);
  }

  public void addConverter(Class<?> clazz, StringConverter<?> converter) {
    classConverterMap.put(clazz, converter);
  }
}
