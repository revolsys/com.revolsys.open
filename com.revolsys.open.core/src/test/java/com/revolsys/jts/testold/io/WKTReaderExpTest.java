package com.revolsys.jts.testold.io;

import java.io.IOException;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * Tests the {@link WKTReader} with exponential notation.
 */
public class WKTReaderExpTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(WKTReaderExpTest.class);
  }

  private final GeometryFactory fact = GeometryFactory.getFactory();

  private final WKTReader rdr = new WKTReader(this.fact);

  public WKTReaderExpTest(final String name) {
    super(name);
  }

  private void readBad(final String wkt) throws IOException {
    boolean threwParseEx = false;
    try {
      final Geometry g = this.rdr.read(wkt);
    } catch (final ParseException ex) {
    //  System.out.println(ex.getMessage());
      threwParseEx = true;
    }
    assertTrue(threwParseEx);
  }

  private void readGoodCheckCoordinate(final String wkt, final double x,
    final double y) throws IOException, ParseException {
    final Geometry g = this.rdr.read(wkt);
    final Point pt = g.getCoordinate();
    assertEquals(pt.getX(), x, 0.0001);
    assertEquals(pt.getY(), y, 0.0001);
  }

  public void testBadExpFormat() throws IOException, ParseException {
    readBad("POINT (1e0a1 1X02)");
  }

  public void testBadExpPlusSign() throws IOException, ParseException {
    readBad("POINT (1e+01 1X02)");
  }

  public void testBadPlusSign() throws IOException, ParseException {
    readBad("POINT ( +1e+01 1X02)");
  }

  public void testGoodBasicExp() throws IOException, ParseException {
    readGoodCheckCoordinate("POINT ( 1e01 -1E02)", 1E01, -1E02);
  }

  public void testGoodWithExpSign() throws IOException, ParseException {
    readGoodCheckCoordinate("POINT ( 1e-04 1E-05)", 1e-04, 1e-05);
  }
}
