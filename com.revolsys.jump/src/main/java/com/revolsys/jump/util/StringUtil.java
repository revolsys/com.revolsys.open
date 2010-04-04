package com.revolsys.jump.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public final class StringUtil {
  private StringUtil() {
  }

  @SuppressWarnings("unchecked")
  public static String toString(final Map map) {
    StringBuffer s = new StringBuffer();
    for (Iterator<Entry> entries = map.entrySet().iterator(); entries.hasNext();) {
      Entry entry = entries.next();
      s.append(entry.getKey());
      s.append('=');
      s.append(entry.getValue());
      if (entries.hasNext()) {
        s.append(',');
      }
    }
    return s.toString();
  }

  public static <T> String toString(final Collection<T> collection) {
    StringBuffer s = new StringBuffer();
    for (Iterator<T> values = collection.iterator(); values.hasNext();) {
      T value = values.next();
      s.append(value);
      if (values.hasNext()) {
        s.append(',');
      }
    }
    return s.toString();
  }

}
