package com.revolsys.core.test.geometry.test.old.io;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.wkb.WKBReader;
import com.revolsys.geometry.wkb.WKBWriter;

import junit.framework.TestCase;

public class WKBWriterTest extends TestCase {

  public WKBWriterTest(final String name) {
    super(name);
  }

  public void testSRID() throws Exception {
    final GeometryFactory gf = GeometryFactory.DEFAULT_3D;
    final Point p1 = gf.point(1.0, 2.0);
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

    assertTrue(p1.equals(2, p2));
    assertEquals(0, p2.getHorizontalCoordinateSystemId());

    // not write out with srid set
    w = new WKBWriter(2, true);
    wkb = w.write(p1);

    // check the 3rd bit of the second byte, should be set
    b = (byte)(wkb[1] & 0x20);
    assertEquals(0x20, b);

    final int srid = (wkb[5] & 0xff) << 24 | (wkb[6] & 0xff) << 16 | (wkb[7] & 0xff) << 8
      | wkb[8] & 0xff;

    assertEquals(1234, srid);

    r = new WKBReader(gf);
    p2 = (Point)r.read(wkb);

    // read the geometry back in
    assertTrue(p1.equals(2, p2));
    assertEquals(1234, p2.getHorizontalCoordinateSystemId());
  }
}
