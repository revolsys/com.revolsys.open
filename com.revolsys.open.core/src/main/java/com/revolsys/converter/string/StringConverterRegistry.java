package com.revolsys.converter.string;

import java.util.HashMap;
import java.util.Map;

public class StringConverterRegistry {

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
  }

  public void addConverter(StringConverter<?> converter) {
    addConverter(converter.getConvertedClass(), converter);
  }

  public void addConverter(Class<?> clazz, StringConverter<?> converter) {
    classConverterMap.put(clazz, converter);
  }
}
