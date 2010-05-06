package com.revolsys.gis;


public class TinTest{

//  private final GeometryFactory factory = new GeometryFactory(
//    new PrecisionModel(PrecisionModel.FIXED));
//
//  private final Coordinate t0 = new Coordinate(0, 0, 0);
//
//  private final Coordinate t1 = new Coordinate(0, 7, 0);
//
//  private final Coordinate t2 = new Coordinate(10, 0, 0);
//
//  private TriangulatedIrregularNetwork tin;
//
//  private final WKTWriter writer = new WKTWriter(3);
//
//  public boolean matchesTriangles(
//    final Triangle expectedTriangle,
//    final Collection<Triangle> triangles) {
//    for (final Triangle triangle : triangles) {
//      if (expectedTriangle.equals(triangle)) {
//        return true;
//      }
//    }
//    return false;
//  }
//
//  public boolean matchesTriangles(
//    final Triangle[] expectedTriangles,
//    final Collection<Triangle> triangles) {
//    assertEquals(expectedTriangles.length, triangles.size());
//    int i = 0;
//    for (final Triangle triangle : expectedTriangles) {
//      if (!matchesTriangles(triangle, triangles)) {
//        fail("Expecting Triangle[" + i + "]: " + toString(triangle));
//        return false;
//      }
//      i++;
//    }
//    return true;
//  }
//
//  //
//  // public void test000BreaklineStartCornerEndCorner() throws Exception {
//  // Coordinate l0 = new Coordinate(0, 7, 1);
//  // Coordinate l1 = new Coordinate(10, 0, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(l0, l1, t2)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test001BreaklineStartCornerEndEdgeNext() throws Exception {
//  // Coordinate l0 = new Coordinate(0, 7, 1);
//  // Coordinate l1 = new Coordinate(4, 4, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(l0, l1, t0), new Triangle(l1, t2, t0)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test002BreaklineStartCornerEndEdgePrevious() throws Exception {
//  // Coordinate l0 = new Coordinate(0, 7, 1);
//  // Coordinate l1 = new Coordinate(0, 5, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(l0, l1, t2), new Triangle(l1, t0, t2)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test003BreaklineStartCornerEndEdgeOpposite() throws Exception {
//  // Coordinate l0 = new Coordinate(0, 7, 1);
//  // Coordinate l1 = new Coordinate(3, 0, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(l0, l1, t2), new Triangle(l0, l1, t0)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test004BreaklineStartCornerEndInside() throws Exception {
//  // Coordinate l0 = new Coordinate(0, 7, 1);
//  // Coordinate l1 = new Coordinate(3, 3, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(l0, t2, l1), new Triangle(l1, t2, t0),
//  // new Triangle(l0, l1, t0)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test005BreaklineStartEdgeEndSameEdge() throws Exception {
//  // Coordinate l0 = new Coordinate(2, 0, 1);
//  // Coordinate l1 = new Coordinate(4, 0, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, t1, l0), new Triangle(t1, l0, l1),
//  // new Triangle(t1, t2, l1)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test006BreaklineStartEdgeEndNextEdge() throws Exception {
//  // Coordinate l0 = new Coordinate(1, 0, 1);
//  // Coordinate l1 = new Coordinate(0, 2, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t2, l0, t1),
//  // new Triangle(l0, l1, t1),
//  // new Triangle(l0, t0, l1)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test007BreaklineStartEdgeEndPreviousEdge() throws Exception {
//  // Coordinate l0 = new Coordinate(0, 1, 1);
//  // Coordinate l1 = new Coordinate(2, 0, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, l0, l1),
//  // new Triangle(t1, l0, l1),
//  // new Triangle(t1,t2, l1)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test008BreaklineStartEdgeEndInside() throws Exception {
//  // Coordinate l0 = new Coordinate(0, 2, 1);
//  // Coordinate l1 = new Coordinate(2, 2, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, l0, l1),
//  // new Triangle(t0, l1, t2),
//  // new Triangle(t1,l0,l1),
//  // new Triangle(t1, t2, l1)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test009BreaklineStartInsideEndInsideCounterClockwise()
//  // throws Exception {
//  // Coordinate l0 = new Coordinate(1, 4, 1);
//  // Coordinate l1 = new Coordinate(2, 1, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, l0, l1), new Triangle(t0, t1, l0),
//  // new Triangle(t1, t2, l0), new Triangle(l0, t2, l1),
//  // new Triangle(l1, t2, t0)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test010BreaklineStartInsideEndInsideClockwise() throws
//  // Exception {
//  // Coordinate l0 = new Coordinate(1, 2, 1);
//  // Coordinate l1 = new Coordinate(3, 1, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, t1, l0), new Triangle(t1, l0, t2),
//  // new Triangle(l0, t2, l1), new Triangle(t0, l1, t2),
//  // new Triangle(t0, l0, l1)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test011BreaklineStartInsideEndInsideColinear() throws Exception
//  // {
//  // Coordinate l0 = new Coordinate(1, 1, 1);
//  // Coordinate l1 = new Coordinate(2, 2, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, t1, l0), new Triangle(t1, l1, l0),
//  // new Triangle(t1, t2, l1), new Triangle(l1, t2, l0),
//  // new Triangle(l0, t2, t0)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test012BreaklineStartInsideEndInsideMiddleClockwise()
//  // throws Exception {
//  // Coordinate l0 = new Coordinate(1, 1, 1);
//  // Coordinate l1 = new Coordinate(3, 2, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, t1, l0), new Triangle(t1, l1, l0),
//  // new Triangle(t1, t2, l1), new Triangle(l1, t2, l0),
//  // new Triangle(l0, t2, t0)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test013BreaklineStartInsideEndInsideMiddleCounterClockwise()
//  // throws Exception {
//  // Coordinate l0 = new Coordinate(1, 1, 1);
//  // Coordinate l1 = new Coordinate(2, 3, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, t1, l0), new Triangle(t1, l1, l0),
//  // new Triangle(t1, t2, l1), new Triangle(l1, t2, l0),
//  // new Triangle(l0, t2, t0)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // public void test014BreaklineStartEdgeEndInsideColinear() throws Exception {
//  // Coordinate l0 = new Coordinate(0, 2, 1);
//  // Coordinate l1 = new Coordinate(5, 1, 2);
//  // Triangle[] triangles = new Triangle[] {
//  // new Triangle(t0, l0, l1),
//  // new Triangle(t0, l1, t2),
//  // new Triangle(t1,l0,l1),
//  // new Triangle(t1, t2, l1)
//  // };
//  // checkBreakline(l0, l1, triangles);
//  // }
//  //
//  // private void checkBreakline(Coordinate l0, Coordinate l1,
//  // Triangle[] expectedTriangles) throws Exception {
//  // log.info(getName());
//  // LineSegment3D lineSegment = new LineSegment3D(l0, l1);
//  // checkSegment(lineSegment, expectedTriangles);
//  //
//  // setUp();
//  // LineSegment3D reversedSegment = new LineSegment3D(l1, l0);
//  // checkSegment(reversedSegment, expectedTriangles);
//  // }
//
//  @Override
//  protected void setUp()
//    throws Exception {
//
//    factory.createPolygon(factory.createLinearRing(new Coordinate[] {
//      t0, t1, t2, t0
//    }), null);
//    tin = new TriangulatedIrregularNetwork(factory,
//      new Envelope(-1, 11, -1, 11));
//    tin.insertNode(t0);
//    tin.insertNode(t1);
//    tin.insertNode(t2);
//    System.out.println(tin.getTriangles());
//    System.out.println();
//  }
//
//  @Override
//  protected void tearDown()
//    throws Exception {
//    tin = null;
//  }
//
//  public void testQuick()
//    throws Exception {
//  }
//
//  private String toString(
//    final Triangle triangle) {
//    final Polygon polygon = factory.createPolygon(
//      factory.createLinearRing(new Coordinate[] {
//        triangle.p0, triangle.p1, triangle.p2, triangle.p0
//      }), null);
//
//    return writer.write(polygon);
//  }
}
