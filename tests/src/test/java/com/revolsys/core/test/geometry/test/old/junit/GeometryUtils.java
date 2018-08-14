package com.revolsys.core.test.geometry.test.old.junit;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.wkb.ParseException;
import com.revolsys.io.IoFactory;

public class GeometryUtils {
  static GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  public static boolean isEqual(final Geometry a, final Geometry b) {
    final Geometry a2 = normalize(a);
    final Geometry b2 = normalize(b);
    return a2.equals(2, b2);
  }

  public static Geometry normalize(final Geometry g) {
    final Geometry g2 = g.normalize();
    return g2;
  }

  public static Geometry readWKT(final String inputWKT) throws ParseException {
    return geometryFactory.geometry(inputWKT);
  }

  public static List<Geometry> readWKT(final String[] inputWKT) throws ParseException {
    final List<Geometry> geometries = new ArrayList<>();
    for (final String element : inputWKT) {
      geometries.add(geometryFactory.geometry(element));
    }
    return geometries;
  }

  public static List<Geometry> readWKTFile(final GeometryFactory geometryFactory,
    final Reader reader) throws IOException, ParseException {
    final GeometryReaderFactory readerFactory = IoFactory
      .factoryByFileExtension(GeometryReaderFactory.class, "wkt");
    try (
      GeometryReader geometryReader = readerFactory.newGeometryReader(reader)) {
      geometryReader.setProperty("geometryFactory", geometryFactory);
      return geometryReader.toList();
    }
  }

  public static List<Geometry> readWKTFile(final GeometryFactory geometryFactory,
    final String filename) throws IOException, ParseException {
    try (
      FileReader reader = new FileReader(filename)) {
      return readWKTFile(geometryFactory, reader);
    }
  }

  public static List<Geometry> readWKTFile(final Reader reader) throws IOException, ParseException {
    final GeometryReaderFactory readerFactory = IoFactory
      .factoryByFileExtension(GeometryReaderFactory.class, "wkt");
    try (
      GeometryReader geometryReader = readerFactory.newGeometryReader(reader)) {
      return geometryReader.toList();
    }
  }

  public static List<Geometry> readWKTFile(final String filename)
    throws IOException, ParseException {
    try (
      FileReader reader = new FileReader(filename)) {
      return readWKTFile(reader);
    }
  }
}
