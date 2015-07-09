package com.revolsys.jts.testold.operation;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.io.ParseException;

import junit.framework.TestCase;

public class FixedPrecisionSnappingTest extends TestCase {
  private final GeometryFactory factory = GeometryFactory.fixed(0, 1.0);

  public FixedPrecisionSnappingTest(final String name) {
    super(name);
  }

  public void testTriangles() throws ParseException {
    final Polygon a = this.factory.geometry("POLYGON((545 317, 617 379, 581 321, 545 317))");
    final Polygon b = this.factory.geometry("POLYGON((484 290, 558 359, 543 309, 484 290))");
    a.intersection(b);
  }
}
