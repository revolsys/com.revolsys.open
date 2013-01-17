package com.revolsys.io.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.Reader;
import com.revolsys.spring.SpringUtil;

public class JsonMapIoFactory extends AbstractMapReaderFactory implements
  MapWriterFactory {
  public static Map<String, Object> toMap(final File file) {
    final FileSystemResource resource = new FileSystemResource(file);
    return toMap(resource);
  }

  public static Map<String, Object> toMap(final InputStream in) {

    try {
      try {
        final java.io.Reader reader = new InputStreamReader(in);
        final JsonMapIterator iterator = new JsonMapIterator(reader, true);
        try {
          if (iterator.hasNext()) {
            return iterator.next();
          } else {
            return null;
          }
        } finally {
          iterator.close();
        }
      } finally {
        FileUtil.closeSilent(in);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read JSON map", e);
    }
  }

  public static final Map<String, Object> toMap(final Resource resource) {
    try {
      final InputStream in = resource.getInputStream();
      return toMap(in);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open stream for " + resource, e);
    }
  }

  public static Map<String, String> toMap(final String string) {
    final Map<String, Object> map = toObjectMap(string);
    if (map.isEmpty()) {
      return Collections.emptyMap();
    } else {
      final Map<String, String> stringMap = new LinkedHashMap<String, String>();
      for (final Entry<String, Object> entry : map.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value == null) {
          stringMap.put(key, null);
        } else {
          stringMap.put(key, value.toString());
        }
      }
      return stringMap;
    }
  }

  public static List<Map<String, Object>> toMapList(final String string) {
    final StringReader in = new StringReader(string);
    final JsonMapReader reader = new JsonMapReader(in);
    try {
      return reader.read();
    } finally {
      reader.close();
    }
  }

  public static Map<String, Object> toObjectMap(final String string) {
    if (StringUtils.hasText(string)) {
      final StringReader reader = new StringReader(string);
      final Reader<Map<String, Object>> mapReader = new JsonMapReader(reader,
        true);
      for (final Map<String, Object> map : mapReader) {
        return map;
      }
    }
    return Collections.emptyMap();
  }

  public static String toString(final List<? extends Map<String, Object>> list) {
    final StringWriter writer = new StringWriter();
    final JsonMapWriter dataObjectWriter = new JsonMapWriter(writer);
    for (final Map<String, Object> map : list) {
      dataObjectWriter.write(map);
    }
    dataObjectWriter.close();
    return writer.toString();
  }

  public static String toString(final Map<String, ? extends Object> values) {
    final StringWriter writer = new StringWriter();
    final JsonWriter jsonWriter = new JsonWriter(writer, false);
    jsonWriter.write(values);
    jsonWriter.close();
    return writer.toString();
  }

  public JsonMapIoFactory() {
    super("JSON");
    addMediaTypeAndFileExtension("application/json", "json");
  }

  @Override
  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    try {
      return new JsonMapReader(resource.getInputStream());
    } catch (final IOException e) {
      throw new RuntimeException("Unable to open " + resource, e);
    }
  }

  @Override
  public MapWriter getWriter(final OutputStream out) {
    final Writer writer = new OutputStreamWriter(out);
    return getWriter(writer);
  }

  @Override
  public MapWriter getWriter(final Resource resource) {
    final Writer writer = SpringUtil.getWriter(resource);
    return getWriter(writer);
  }

  @Override
  public MapWriter getWriter(final Writer out) {
    return new JsonMapWriter(out);
  }

  @Override
  public boolean isCustomAttributionSupported() {
    return true;
  }

  @Override
  public boolean isGeometrySupported() {
    return true;
  }

}
