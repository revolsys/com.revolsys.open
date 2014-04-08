package com.revolsys.jts.operation.relate;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.algorithm.BoundaryNodeRule;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * Tests {@link Geometry#relate} with different {@link BoundaryNodeRule}s.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class RelateBoundaryNodeRuleTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(RelateBoundaryNodeRuleTest.class);
  }

  private final GeometryFactory fact = new GeometryFactory();

  private final WKTReader rdr = new WKTReader(this.fact);

  public RelateBoundaryNodeRuleTest(final String name) {
    super(name);
  }

  void runRelateTest(final String wkt1, final String wkt2,
    final BoundaryNodeRule bnRule, final String expectedIM)
    throws ParseException {
    final Geometry g1 = this.rdr.read(wkt1);
    final Geometry g2 = this.rdr.read(wkt2);
    final IntersectionMatrix im = RelateOp.relate(g1, g2, bnRule);
    final String imStr = im.toString();
    System.out.println(imStr);
    assertTrue(im.matches(expectedIM));
  }

  public void testLineRingTouchAtEndpointAndInterior() throws Exception {
    final String a = "LINESTRING (20 100, 20 220, 120 100, 20 100)";
    final String b = "LINESTRING (20 20, 40 100)";

    // this is the same result as for the above test
    runRelateTest(a, b, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE, "F01FFF102");
    // this result is different - the A node is now on the boundary, so
    // A.bdy/B.ext = 0
    runRelateTest(a, b, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE, "F01FF0102");
  }

  public void testLineRingTouchAtEndpoints() throws Exception {
    final String a = "LINESTRING (20 100, 20 220, 120 100, 20 100)";
    final String b = "LINESTRING (20 20, 20 100)";

    // under Mod2, A has no boundary - A.int / B.bdy = 0
    // runRelateTest(a, b, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE, "F01FFF102"
    // );
    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
    // runRelateTest(a, b, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE, "FF1F0F102"
    // );
    // under MultiValent, A has a boundary node but B does not - A.bdy / B.bdy =
    // F and A.int
    runRelateTest(a, b, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,
      "0F1FFF1F2");
  }

  public void testLineStringSelfIntTouchAtEndpoint() throws Exception {
    final String a = "LINESTRING (20 20, 100 100, 100 20, 20 100)";
    final String b = "LINESTRING (60 60, 20 60)";

    // results for both rules are the same
    runRelateTest(a, b, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE, "F01FF0102");
    runRelateTest(a, b, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE, "F01FF0102");
  }

  public void testMultiLineStringSelfIntTouchAtEndpoint() throws Exception {
    final String a = "MULTILINESTRING ((20 20, 100 100, 100 20, 20 100), (60 60, 60 140))";
    final String b = "LINESTRING (60 60, 20 60)";

    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
    runRelateTest(a, b, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE, "FF1F00102");
  }

  public void testMultiLineStringTouchAtEndpoint() throws Exception {
    final String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20))";
    final String b = "LINESTRING (10 10, 20 0)";

    // under Mod2, A has no boundary - A.int / B.bdy = 0
    // runRelateTest(a, b, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE, "F01FFF102"
    // );
    // under EndPoint, A has a boundary node - A.bdy / B.bdy = 0
    runRelateTest(a, b, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE, "FF1F00102");
    // under MultiValent, A has a boundary node but B does not - A.bdy / B.bdy =
    // F and A.int
    // runRelateTest(a, b, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,
    // "0F1FFF1F2" );
  }
}
