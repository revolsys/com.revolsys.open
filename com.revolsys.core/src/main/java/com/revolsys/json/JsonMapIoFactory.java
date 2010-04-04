package com.revolsys.json;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapReader;
import com.revolsys.io.MapReaderFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;

public class JsonMapIoFactory extends AbstractIoFactory implements
  MapReaderFactory, MapWriterFactory {
  public JsonMapIoFactory() {
    super("JSON");
    addMediaTypeAndFileExtension("application/json", "json");
  }

  public MapReader getReader(
    final Reader in) {
    return new JsonMapReader(in);
  }

  public MapWriter getWriter(
    final Writer out) {
    return new JsonMapWriter(out);
  }

  public static String toString(
    final Map<String, ? extends Object> map) {
    final StringWriter writer = new StringWriter();
    final JsonMapWriter mapWriter = new JsonMapWriter(writer);
    mapWriter.write(map);
    mapWriter.close();
    return writer.toString();
  }

  public static Map<String, String> toMap(
    final String string) {
    final Map<String, Object> map = toObjectMap(string);
    if (map.isEmpty()) {
      return Collections.emptyMap();
    } else {
      Map<String, String> stringMap = new LinkedHashMap<String, String>();
      for (Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        if (value == null) {
          stringMap.put(key, null);
        } else {
          stringMap.put(key, value.toString());
        }
      }
      return stringMap;
    }
  }

  public static Map<String, Object> toObjectMap(
    final String string) {
    final StringReader reader = new StringReader(string);
    final MapReader mapReader = new JsonMapReader(reader);
    for (Map<String, Object> map : mapReader) {
      return map;
    }
    return Collections.emptyMap();
  }

}
