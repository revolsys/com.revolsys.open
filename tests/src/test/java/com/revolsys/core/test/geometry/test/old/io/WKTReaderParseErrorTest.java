package com.revolsys.core.test.geometry.test.old.io;

import java.io.IOException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.wkb.ParseException;
import com.revolsys.geometry.wkb.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests the {@link WKTReader} with various errors
 */
public class WKTReaderParseErrorTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(WKTReaderParseErrorTest.class);
  }

  private final GeometryFactory fact = GeometryFactory.DEFAULT_3D;

  private final WKTReader rdr = new WKTReader(this.fact);

  public WKTReaderParseErrorTest(final String name) {
    super(name);
  }

  private void readBad(final String wkt) throws IOException {
    boolean threwParseEx = false;
    try {
      final Geometry g = this.rdr.read(wkt);
    } catch (final ParseException ex) {
      // System.out.println(ex.getMessage());
      threwParseEx = true;
    }
    assertTrue(threwParseEx);
  }

  public void testBadChar() throws IOException, ParseException {
    readBad("POINT ( # 1e-04 1E-05)");
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

  public void testExtraLParen() throws IOException, ParseException {
    readBad("POINT (( 1e01 -1E02)");
  }

  public void testMissingOrdinate() throws IOException, ParseException {
    readBad("POINT ( 1e01 )");
  }
}
