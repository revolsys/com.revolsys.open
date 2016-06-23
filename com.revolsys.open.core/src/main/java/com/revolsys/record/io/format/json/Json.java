package com.revolsys.record.io.format.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;

public class Json extends AbstractIoFactory implements MapReaderFactory, MapWriterFactory {
  public static Map<String, Object> getMap(final Map<String, Object> record,
    final String fieldName) {
    final String value = (String)record.get(fieldName);
    return toObjectMap(value);
  }

  public static MapEx toMap(final File directory, final String path) {
    if (directory == null || path == null) {
      return new LinkedHashMapEx();
    } else {
      final File file = FileUtil.getFile(directory, path);
      if (file.exists() && !file.isDirectory()) {
        final FileSystemResource resource = new FileSystemResource(file);
        return toMap(resource);
      } else {
        return new LinkedHashMapEx();
      }
    }
  }

  public static MapEx toMap(final Object source) {
    final Resource resource = Resource.getResource(source);
    return toMap(resource);
  }

  public static MapEx toMap(final Path directory, final String path) {
    if (directory == null || path == null) {
      return new LinkedHashMapEx();
    } else {
      final Path file = directory.resolve(path);
      if (Paths.exists(file) && !Files.isDirectory(file)) {
        final PathResource resource = new PathResource(file);
        return toMap(resource);
      } else {
        return new LinkedHashMapEx();
      }
    }
  }

  public static final MapEx toMap(final Resource resource) {
    if (resource != null && (!(resource instanceof FileSystemResource) || resource.exists())) {
      try {
        final java.io.Reader reader = resource.newBufferedReader();
        try (
          final JsonMapIterator iterator = new JsonMapIterator(reader, true)) {
          if (iterator.hasNext()) {
            return iterator.next();
          } else {
            return null;
          }
        }
      } catch (final IOException e) {
        throw new RuntimeException("Unable to read JSON map", e);
      }
    }
    return new LinkedHashMapEx();
  }

  public static Map<String, String> toMap(final String string) {
    final Map<String, Object> map = toObjectMap(string);
    if (map.isEmpty()) {
      return new LinkedHashMap<String, String>();
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

  public static final List<MapEx> toMapList(final Object source) {
    final Resource resource = Resource.getResource(source);
    if (resource != null && (!(resource instanceof FileSystemResource) || resource.exists())) {
      try (
        final BufferedReader reader = resource.newBufferedReader();
        final JsonMapReader jsonReader = new JsonMapReader(reader)) {
        return jsonReader.toList();
      } catch (final IOException e) {
        Exceptions.throwUncheckedException(e);
      }
    }
    return new ArrayList<>();
  }

  public static List<MapEx> toMapList(final String string) {
    final StringReader in = new StringReader(string);
    try (
      final JsonMapReader reader = new JsonMapReader(in)) {
      return reader.toList();
    }
  }

  public static MapEx toObjectMap(final String string) {
    if (Property.hasValue(string)) {
      final StringReader reader = new StringReader(string);
      try (
        final MapReader mapReader = new JsonMapReader(reader, true)) {
        for (final MapEx map : mapReader) {
          return map;
        }
      }
    }
    return new LinkedHashMapEx();
  }

  public static String toString(final List<? extends Map<String, Object>> list) {
    final StringWriter writer = new StringWriter();
    final JsonMapWriter mapWriter = new JsonMapWriter(writer, false);
    for (final Map<String, Object> map : list) {
      mapWriter.write(map);
    }
    mapWriter.close();
    return writer.toString();
  }

  public static String toString(final Map<String, ? extends Object> values) {
    final StringWriter writer = new StringWriter();
    try (
      final JsonWriter jsonWriter = new JsonWriter(writer, false)) {
      jsonWriter.write(values);
    }
    return writer.toString();
  }

  public static String toString(final Map<String, ? extends Object> values, final boolean indent) {
    final StringWriter writer = new StringWriter();
    try (
      final JsonWriter jsonWriter = new JsonWriter(writer, indent)) {
      jsonWriter.write(values);
    }
    return writer.toString();
  }

  public static void writeMap(final Map<String, ? extends Object> object, final Object target) {
    writeMap(object, target, true);
  }

  public static void writeMap(final Map<String, ? extends Object> object, final Object target,
    final boolean indent) {
    final Resource resource = Resource.getResource(target);
    try (
      final Writer writer = resource.newWriter();
      final JsonMapWriter out = new JsonMapWriter(writer, indent);) {
      out.setSingleObject(true);
      out.write(object);
    } catch (final IOException e) {
    }
  }

  public Json() {
    super("JSON");
    addMediaTypeAndFileExtension("application/json", "json");
  }

  @Override
  public MapReader newMapreader(final Resource resource) {
    return new JsonMapReader(resource.getInputStream());
  }

  @Override
  public MapWriter newMapWriter(final OutputStream out, final Charset charset) {
    return newMapWriter(out);
  }

  @Override
  public MapWriter newMapWriter(final Writer out) {
    return new JsonMapWriter(out);
  }
}
