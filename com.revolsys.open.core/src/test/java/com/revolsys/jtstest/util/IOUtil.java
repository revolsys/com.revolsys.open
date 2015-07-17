package com.revolsys.jtstest.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.springframework.core.io.FileSystemResource;

import com.revolsys.gis.geometry.io.GeometryReader;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKBHexFileReader;
import com.revolsys.jts.io.WKBReader;
import com.revolsys.jts.io.WKTFileReader;
import com.revolsys.jts.io.WKTReader;

public class IOUtil {
  public static Geometry readGeometriesFromFile(final String filename,
    final GeometryFactory geomFact) throws Exception, IOException {
    final String ext = FileUtil.extension(filename);
    if (ext.equalsIgnoreCase(".shp")) {
      try (
        GeometryReader reader = GeometryReader
          .create(new FileSystemResource(filename))) {
        final List<Geometry> geometries = reader.read();
        if (geometries.isEmpty()) {
          return geomFact.geometryCollection();
        } else {
          final GeometryFactory geometryFactory = geometries.get(0).getGeometryFactory();
          return geometryFactory.geometryCollection(geometries);
        }
      }
    }
    if (ext.equalsIgnoreCase(".wkb")) {
      return readGeometryFromWKBHexFile(filename, geomFact);
    }
    return readGeometriesFromWKTFile(filename, geomFact);
  }

  public static Geometry readGeometriesFromWKBHexString(final String wkb,
    final GeometryFactory geomFact) throws ParseException, IOException {
    final WKBReader reader = new WKBReader(geomFact);
    final WKBHexFileReader fileReader = new WKBHexFileReader(new StringReader(wkb), reader);
    final List geomList = fileReader.read();

    if (geomList.size() == 1) {
      return (Geometry)geomList.get(0);
    }

    return geomFact.geometryCollection(geomList);
  }

  private static Geometry readGeometriesFromWKTFile(final String filename,
    final GeometryFactory geomFact) throws ParseException, IOException {
    return readGeometriesFromWKTString(FileUtil.readText(filename), geomFact);
  }

  /**
   * Reads one or more WKT geometries from a string.
   *
   * @param wkt
   * @param geomFact
   * @return the geometry read
   * @throws ParseException
   * @throws IOException
   */
  public static Geometry readGeometriesFromWKTString(final String wkt,
    final GeometryFactory geomFact) throws ParseException, IOException {
    final WKTReader reader = new WKTReader(geomFact);
    final WKTFileReader fileReader = new WKTFileReader(new StringReader(wkt), reader);
    final List geomList = fileReader.read();

    if (geomList.size() == 1) {
      return (Geometry)geomList.get(0);
    }

    return geomFact.geometryCollection(geomList);
  }

  private static Geometry readGeometryFromWKBHexFile(final String filename,
    final GeometryFactory geomFact) throws ParseException, IOException {
    return readGeometriesFromWKBHexString(FileUtil.readText(filename), geomFact);
  }

}
