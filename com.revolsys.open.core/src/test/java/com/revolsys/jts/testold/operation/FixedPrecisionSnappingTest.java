package com.revolsys.jts.testold.operation;

import junit.framework.TestCase;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.io.ParseException;

public class FixedPrecisionSnappingTest extends TestCase {
  private final GeometryFactory factory = GeometryFactory.getFactory(0, 1.0);

  public FixedPrecisionSnappingTest(final String name) {
    super(name);
  }

  public void testTriangles() throws ParseException {
    final Polygon a = factory.geometry("POLYGON((545 317, 617 379, 581 321, 545 317))");
    final Polygon b = factory.geometry("POLYGON((484 290, 558 359, 543 309, 484 290))");
    a.intersection(b);
  }
}
