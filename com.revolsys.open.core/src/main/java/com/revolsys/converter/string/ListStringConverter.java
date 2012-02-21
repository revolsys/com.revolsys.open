package com.revolsys.converter.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.io.json.JsonParserUtil;
import com.revolsys.io.wkt.WktWriter;
import com.vividsolutions.jts.geom.Geometry;

public class ListStringConverter implements StringConverter<List<String>> {
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public Class<List<String>> getConvertedClass() {
    Class clazz = List.class;
    return clazz;
  }

  public boolean requiresQuotes() {
    return false;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public List<String> toObject(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Collection) {
      Collection<Object> collection = (Collection)value;
      List<String> list = new ArrayList<String>();
      for (Object object : collection) {
        String stringValue = StringConverterRegistry.toString(object);
        list.add(stringValue);
      }
      return list;
    } else {
      return toObject(value.toString());
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> toObject(final String string) {
    Object value = JsonParserUtil.read(string);
    if (value instanceof List) {
      return (List<String>)value;
    } else {
      throw new IllegalArgumentException("Value must be a JSON list " + string);
    }
  }

  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof List) {
      List<?> list = (List<?>)value;
      StringBuffer string = new StringBuffer("[");
      for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
        Object object = iterator.next();
        String stringValue = StringConverterRegistry.toString(object);
        string.append(stringValue);
        if (iterator.hasNext()) {
          string.append(", ");
        }
      }
      string.append("]");
      return string.toString();
    } else {
      return value.toString();
    }

  }
}
