package com.revolsys.jts.testold.operation;

import com.revolsys.geometry.io.ParseException;
import com.revolsys.geometry.io.WKTReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.operation.relate.RelateOp;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests {@link Geometry#relate}.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class RelateTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(RelateTest.class);
  }

  private final GeometryFactory fact = GeometryFactory.floating3();

  private final WKTReader rdr = new WKTReader(this.fact);

  public RelateTest(final String name) {
    super(name);
  }

  void runRelateTest(final String wkt1, final String wkt2, final String expectedIM)
    throws ParseException {
    final Geometry g1 = this.rdr.read(wkt1);
    final Geometry g2 = this.rdr.read(wkt2);
    final IntersectionMatrix im = RelateOp.relate(g1, g2);
    final String imStr = im.toString();
    // System.out.println(imStr);
    assertTrue(im.matches(expectedIM));
  }

  /**
   * From GEOS #572
   *
   * The cause is that the longer line nodes the single-segment line.
   * The node then tests as not lying precisely on the original longer line.
   *
   * @throws Exception
   */
  public void testContainsIncorrectIMMatrix() throws Exception {
    final String a = "LINESTRING (1 0, 0 2, 0 0, 2 2)";
    final String b = "LINESTRING (0 0, 2 2)";

    // actual matrix is 001F001F2
    // true matrix should be 101F00FF2
    runRelateTest(a, b, "001F001F2");
  }
}
