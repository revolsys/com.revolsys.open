package com.revolsys.jtstest.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKBHexFileReader;
import com.revolsys.jts.io.WKBReader;
import com.revolsys.jts.io.WKTFileReader;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jtstest.testbuilder.io.shapefile.Shapefile;

public class IOUtil {
  private static String cleanHex(final String hexStuff) {
    return hexStuff.replaceAll("[^0123456789ABCDEFabcdef]", "");
  }

  public static Geometry readGeometriesFromFile(final String filename,
    final GeometryFactory geomFact) throws Exception, IOException {
    final String ext = FileUtil.extension(filename);
    if (ext.equalsIgnoreCase(".shp")) {
      return readGeometriesFromShapefile(filename, geomFact);
    }
    if (ext.equalsIgnoreCase(".wkb")) {
      return readGeometryFromWKBHexFile(filename, geomFact);
    }
    return readGeometriesFromWKTFile(filename, geomFact);
  }

  private static Geometry readGeometriesFromShapefile(final String filename,
    final GeometryFactory geomFact) throws Exception {
    final Shapefile shpfile = new Shapefile(new FileInputStream(filename));
    shpfile.readStream(geomFact);
    final List geomList = new ArrayList();
    do {
      final Geometry geom = shpfile.next();
      if (geom == null) {
        break;
      }
      geomList.add(geom);
    } while (true);

    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }

  public static Geometry readGeometriesFromWKBHexString(final String wkb,
    final GeometryFactory geomFact) throws ParseException, IOException {
    final WKBReader reader = new WKBReader(geomFact);
    final WKBHexFileReader fileReader = new WKBHexFileReader(new StringReader(
      wkb), reader);
    final List geomList = fileReader.read();

    if (geomList.size() == 1) {
      return (Geometry)geomList.get(0);
    }

    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
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
    final WKTFileReader fileReader = new WKTFileReader(new StringReader(wkt),
      reader);
    final List geomList = fileReader.read();

    if (geomList.size() == 1) {
      return (Geometry)geomList.get(0);
    }

    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geomList));
  }

  private static Geometry readGeometryFromWKBHexFile(final String filename,
    final GeometryFactory geomFact) throws ParseException, IOException {
    return readGeometriesFromWKBHexString(FileUtil.readText(filename), geomFact);
  }

  private static Geometry readGeometryFromWKBHexString(final String wkbHexFile,
    final GeometryFactory geomFact) throws ParseException, IOException {
    final WKBReader reader = new WKBReader(geomFact);
    final String wkbHex = cleanHex(wkbHexFile);
    return reader.read(WKBReader.hexToBytes(wkbHex));
  }

}
