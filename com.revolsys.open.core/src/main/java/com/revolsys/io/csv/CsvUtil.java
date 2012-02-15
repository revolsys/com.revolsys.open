package com.revolsys.io.csv;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CsvUtil {
  /**
   * Convert a Map to a CSV string with a header row and a data row.
   * 
   * @param map The Map to convert to CSV
   * @return The CSV string.
   */
  public static String toCsv(final Map<String, ? extends Object> map) {
    final StringWriter csvString = new StringWriter();
    final CsvMapWriter csvMapWriter = new CsvMapWriter(csvString);
    csvMapWriter.write(map);
    return csvString.toString();
  }

  public static Map<String, String> toMap(
    final String businessApplicationParameters) {
    final HashMap<String, String> map = new LinkedHashMap<String, String>();
    final CsvIterator iterator = new CsvIterator(new StringReader(
      businessApplicationParameters));
    if (iterator.hasNext()) {
      final List<String> keys = iterator.next();
      if (iterator.hasNext()) {
        final List<String> values = iterator.next();
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
          map.put(keys.get(i), values.get(i));
        }
      }
    }
    return map;
  }

  public static Map<String, Object> toObjectMap(
    final String businessApplicationParameters) {
    final HashMap<String, Object> map = new LinkedHashMap<String, Object>();
    final CsvIterator iterator = new CsvIterator(new StringReader(
      businessApplicationParameters));
    if (iterator.hasNext()) {
      final List<String> keys = iterator.next();
      if (iterator.hasNext()) {
        final List<String> values = iterator.next();
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
          map.put(keys.get(i), values.get(i));
        }
      }
    }
    return map;
  }

  private CsvUtil() {
  }

}
