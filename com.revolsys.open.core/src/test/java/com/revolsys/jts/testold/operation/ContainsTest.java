package com.revolsys.jts.testold.operation;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.WKTReader;

/**
 * Tests {@link Geometry#relate}.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ContainsTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(ContainsTest.class);
  }

  private final GeometryFactory fact = GeometryFactory.getFactory();

  private final WKTReader rdr = new WKTReader(this.fact);

  public ContainsTest(final String name) {
    super(name);
  }

  /**
   * From GEOS #572.
   * A case where B is contained in A, but 
   * the JTS relate algorithm fails to compute this correctly.
   * 
   * The cause is that the long segment in A nodes the single-segment line in B.
   * The node location cannot be computed precisely.
   * The node then tests as not lying precisely on the original long segment in A.
   * 
   * The solution is to change the relate algorithm so that it never computes
   * new intersection points, only ones which occur at existing vertices.
   * (The topology of the implicit intersections can still be computed
   * to contribute to the intersection matrix result).
   * This will require a complete reworking of the relate algorithm. 
   * 
   * @throws Exception
   */
  public void testContainsIncorrect() throws Exception {
    final String a = "LINESTRING (1 0, 0 2, 0 0, 2 2)";
    final String b = "LINESTRING (0 0, 2 2)";

    // for now assert this as false, although it should be true
    assertTrue(!a.contains(b));
  }
}
