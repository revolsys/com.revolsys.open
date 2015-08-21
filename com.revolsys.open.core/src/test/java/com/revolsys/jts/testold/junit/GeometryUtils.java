package com.revolsys.jts.testold.junit;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.io.ParseException;
import com.revolsys.geometry.io.WKTFileReader;
import com.revolsys.geometry.io.WKTReader;
import com.revolsys.geometry.model.Geometry;

public class GeometryUtils {
  // TODO: allow specifying GeometryFactoryI

  public static WKTReader reader = new WKTReader();

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
    return reader.read(inputWKT);
  }

  public static List readWKT(final String[] inputWKT) throws ParseException {
    final ArrayList geometries = new ArrayList();
    for (final String element : inputWKT) {
      geometries.add(reader.read(element));
    }
    return geometries;
  }

  public static Collection readWKTFile(final Reader rdr) throws IOException, ParseException {
    final WKTFileReader fileRdr = new WKTFileReader(rdr, reader);
    final List geoms = fileRdr.read();
    return geoms;
  }

  public static Collection readWKTFile(final String filename) throws IOException, ParseException {
    final WKTFileReader fileRdr = new WKTFileReader(filename, reader);
    final List geoms = fileRdr.read();
    return geoms;
  }
}
