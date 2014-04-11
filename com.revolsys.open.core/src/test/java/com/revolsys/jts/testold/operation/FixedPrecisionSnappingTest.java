package com.revolsys.jts.testold.operation;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

public class FixedPrecisionSnappingTest extends TestCase {
  public static void main(final String[] args) {
    junit.textui.TestRunner.run(FixedPrecisionSnappingTest.class);
  }

  PrecisionModel pm = new PrecisionModel(1.0);

  GeometryFactory fact = new GeometryFactory(this.pm);

  WKTReader rdr = new WKTReader(this.fact);

  public FixedPrecisionSnappingTest(final String name) {
    super(name);
  }

  public void testTriangles() throws ParseException {
    final Geometry a = this.rdr.read("POLYGON ((545 317, 617 379, 581 321, 545 317))");
    final Geometry b = this.rdr.read("POLYGON ((484 290, 558 359, 543 309, 484 290))");
    a.intersection(b);
  }
}
