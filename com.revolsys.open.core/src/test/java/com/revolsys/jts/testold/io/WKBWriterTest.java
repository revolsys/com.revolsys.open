package com.revolsys.jts.testold.io;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.io.WKBReader;
import com.revolsys.jts.io.WKBWriter;

public class WKBWriterTest extends TestCase {

  public WKBWriterTest(final String name) {
    super(name);
  }

  public void testSRID() throws Exception {
    final GeometryFactory gf = GeometryFactory.getFactory();
    final Point p1 = gf.point(new Coordinate((double)1, 2, Coordinates.NULL_ORDINATE));
    // p1.setSRID(1234);

    // first write out without srid set
    WKBWriter w = new WKBWriter();
    byte[] wkb = w.write(p1);

    // check the 3rd bit of the second byte, should be unset
    byte b = (byte)(wkb[1] & 0x20);
    assertEquals(0, b);

    // read geometry back in
    WKBReader r = new WKBReader(gf);
    Point p2 = (Point)r.read(wkb);

    assertTrue(p1.equalsExact2d(p2));
    assertEquals(0, p2.getSrid());

    // not write out with srid set
    w = new WKBWriter(2, true);
    wkb = w.write(p1);

    // check the 3rd bit of the second byte, should be set
    b = (byte)(wkb[1] & 0x20);
    assertEquals(0x20, b);

    final int srid = (wkb[5] & 0xff) << 24 | (wkb[6] & 0xff) << 16
      | (wkb[7] & 0xff) << 8 | wkb[8] & 0xff;

    assertEquals(1234, srid);

    r = new WKBReader(gf);
    p2 = (Point)r.read(wkb);

    // read the geometry back in
    assertTrue(p1.equalsExact2d(p2));
    assertEquals(1234, p2.getSrid());
  }
}
