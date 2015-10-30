package com.revolsys.record.io.format.json;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Paths;
import com.revolsys.io.Reader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class Json extends AbstractIoFactory implements MapReaderFactory, MapWriterFactory {
  public static Map<String, Object> toMap(final File file) {
    if (file == null) {
      return new LinkedHashMap<>();
    } else {
      final FileSystemResource resource = new FileSystemResource(file);
      return toMap(resource);
    }
  }

  public static Map<String, Object> toMap(final File directory, final String path) {
    if (directory == null || path == null) {
      return new LinkedHashMap<>();
    } else {
      final File file = FileUtil.getFile(directory, path);
      if (file.exists() && !file.isDirectory()) {
        final FileSystemResource resource = new FileSystemResource(file);
        return toMap(resource);
      } else {
        return new LinkedHashMap<>();
      }
    }
  }

  public static Map<String, Object> toMap(final InputStream in) {
    if (in == null) {
      return new LinkedHashMap<>();
    } else {
      try {
        final java.io.Reader reader = FileUtil.newUtf8Reader(in);
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
  }

  public static Map<String, Object> toMap(final Path directory, final String path) {
    if (directory == null || path == null) {
      return new LinkedHashMap<>();
    } else {
      final Path file = directory.resolve(path);
      if (Paths.exists(file) && !Files.isDirectory(file)) {
        final PathResource resource = new PathResource(file);
        return toMap(resource);
      } else {
        return new LinkedHashMap<>();
      }
    }
  }

  public static final Map<String, Object> toMap(final Resource resource) {
    if (resource != null && (!(resource instanceof FileSystemResource) || resource.exists())) {
      final InputStream in = resource.getInputStream();
      return toMap(in);
    } else {
      return new LinkedHashMap<>();
    }
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

  public static List<Map<String, Object>> toMapList(final String string) {
    final StringReader in = new StringReader(string);
    final JsonMapReader reader = new JsonMapReader(in);
    try {
      return reader.toList();
    } finally {
      reader.close();
    }
  }

  public static Map<String, Object> toObjectMap(final String string) {
    if (Property.hasValue(string)) {
      final StringReader reader = new StringReader(string);
      try (
        final Reader<Map<String, Object>> mapReader = new JsonMapReader(reader, true)) {
        for (final Map<String, Object> map : mapReader) {
          return map;
        }
      }
    }
    return new LinkedHashMap<>();
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

  public static void write(final Map<String, ? extends Object> object, final File file) {
    final FileSystemResource resource = new FileSystemResource(file);
    write(object, resource);
  }

  public static void write(final Map<String, ? extends Object> object, final File file,
    final boolean indent) {
    final FileSystemResource resource = new FileSystemResource(file);
    write(object, resource, indent);
  }

  public static void write(final Map<String, ? extends Object> object, final Path path,
    final boolean indent) {
    final PathResource resource = new PathResource(path);
    write(object, resource, indent);
  }

  public static void write(final Map<String, ? extends Object> object, final Resource resource) {
    try (
      final Writer writer = resource.newWriter();
      final JsonMapWriter out = new JsonMapWriter(writer);) {
      out.setSingleObject(true);
      out.write(object);
    } catch (final IOException e) {
    }
  }

  public static void write(final Map<String, ? extends Object> object, final Resource resource,
    final boolean indent) {
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
