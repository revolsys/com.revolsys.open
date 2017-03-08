package com.revolsys.geometry.io;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;

public interface GeometryWriter extends Writer<Geometry> {
  static boolean isWritable(final Object source) {
    return IoFactory.isAvailable(GeometryWriterFactory.class, source);
  }

  static GeometryWriter newGeometryWriter(final Object source) {
    final GeometryWriterFactory factory = IoFactory.factory(GeometryWriterFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.newGeometryWriter(source);
    }
  }

  static GeometryWriter newGeometryWriter(final Object source,
    final GeometryFactory geometryFactory) {
    final GeometryWriter writer = newGeometryWriter(source);
    if (writer != null) {
      writer.setGeometryFactory(geometryFactory);
    }
    return writer;
  }

  static GeometryWriter newGeometryWriter(final Object source, final MapEx properties) {
    final GeometryWriter writer = newGeometryWriter(source);
    if (writer != null) {
      writer.setProperties(properties);
    }
    return writer;
  }

  static void writeAll(final Object source, final GeometryFactory geometryFactory,
    final Iterable<? extends Geometry> geometries) {
    try (
      GeometryWriter writer = newGeometryWriter(source, geometryFactory)) {
      if (writer == null) {
        throw new IllegalArgumentException("Cannot create writer for: " + source);
      } else {
        writer.writeAll(geometries);
      }
    }
  }

  static void writeAll(final Object source, final MapEx properties,
    final Iterable<? extends Geometry> geometries) {
    try (
      GeometryWriter writer = newGeometryWriter(source, properties)) {
      if (writer == null) {
        throw new IllegalArgumentException("Cannot create writer for: " + source);
      } else {
        writer.writeAll(geometries);
      }
    }
  }

  void setGeometryFactory(GeometryFactory geometryFactory);

  default void writeAll(final Iterable<? extends Geometry> geometries) {
    if (geometries != null) {
      for (final Geometry geometry : geometries) {
        if (geometry != null) {
          write(geometry);
        }
      }
    }
  }
}
