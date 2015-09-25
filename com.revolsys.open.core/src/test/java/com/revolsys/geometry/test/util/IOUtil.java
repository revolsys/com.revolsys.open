package com.revolsys.geometry.test.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.wkb.ParseException;
import com.revolsys.geometry.wkb.WKBHexFileReader;
import com.revolsys.geometry.wkb.WKBReader;
import com.revolsys.geometry.wkb.WKTFileReader;
import com.revolsys.geometry.wkb.WKTReader;
import com.revolsys.spring.resource.FileSystemResource;

public class IOUtil {
  public static Geometry readGeometriesFromFile(final String filename,
    final GeometryFactory geomFact) throws Exception, IOException {
    final String ext = FileUtil.extension(filename);
    if (ext.equalsIgnoreCase(".shp")) {
      try (
        GeometryReader reader = GeometryReader.create(new FileSystemResource(filename))) {
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
