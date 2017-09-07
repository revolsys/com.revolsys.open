package com.revolsys.geometry.test.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.test.old.junit.GeometryUtils;
import com.revolsys.geometry.wkb.ParseException;
import com.revolsys.geometry.wkb.WKBHexFileReader;
import com.revolsys.geometry.wkb.WKBReader;
import com.revolsys.spring.resource.PathResource;

public class IOUtil {
  public static Geometry readGeometriesFromFile(final String filename,
    final GeometryFactory geomFact) throws Exception, IOException {
    final String ext = TestFileUtil.getFileNameExtension(filename);
    if (ext.equalsIgnoreCase(".shp")) {
      try (
        GeometryReader reader = GeometryReader.newGeometryReader(new PathResource(filename))) {
        final List<Geometry> geometries = reader.toList();
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
    return readGeometriesFromWKTString(TestFileUtil.readText(filename), geomFact);
  }

  /**
   * Reads one or more WKT geometries from a string.
   *
   * @param wkt
   * @param geomFactory
   * @return the geometry read
   * @throws ParseException
   * @throws IOException
   */
  public static Geometry readGeometriesFromWKTString(final String wkt,
    final GeometryFactory geomFactory) throws ParseException, IOException {
    final List<Geometry> geomList = GeometryUtils.readWKTFile(geomFactory, new StringReader(wkt));

    if (geomList.size() == 1) {
      return geomList.get(0);
    }

    return geomFactory.geometryCollection(geomList);
  }

  private static Geometry readGeometryFromWKBHexFile(final String filename,
    final GeometryFactory geomFact) throws ParseException, IOException {
    return readGeometriesFromWKBHexString(TestFileUtil.readText(filename), geomFact);
  }

}
