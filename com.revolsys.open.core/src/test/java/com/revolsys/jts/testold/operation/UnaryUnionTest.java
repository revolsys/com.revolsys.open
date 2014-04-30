package com.revolsys.jts.testold.operation;

import java.util.Collection;

import junit.framework.TestCase;

import org.junit.Assert;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.operation.union.UnaryUnionOp;
import com.revolsys.jts.testold.junit.GeometryUtils;

public class UnaryUnionTest extends TestCase {

  public UnaryUnionTest(final String name) {
    super(name);
  }

  private void doTest(final String expectedWKT, final String... inputWKT)
    throws ParseException {
    Geometry result;
    final Collection<Geometry> geoms = GeometryUtils.readWKT(inputWKT);
    if (geoms.size() == 0) {
      final GeometryFactory geometryFactory = GeometryFactory.getFactory();
      result = UnaryUnionOp.union(geoms, geometryFactory);
    } else {
      result = UnaryUnionOp.union(geoms);
    }

    Assert.assertEquals(GeometryUtils.readWKT(expectedWKT), result);
  }

  public void testAll() throws Exception {
    doTest(
      "GEOMETRYCOLLECTION (POINT (60 140),   LINESTRING (40 90, 40 140), LINESTRING (160 90, 160 140), POLYGON ((0 0, 0 90, 40 90, 90 90, 90 0, 0 0)), POLYGON ((120 0, 120 90, 160 90, 210 90, 210 0, 120 0)))",
      "GEOMETRYCOLLECTION (POLYGON ((0 0, 0 90, 90 90, 90 0, 0 0)),   POLYGON ((120 0, 120 90, 210 90, 210 0, 120 0)),  LINESTRING (40 50, 40 140),  LINESTRING (160 50, 160 140),  POINT (60 50),  POINT (60 140),  POINT (40 140))");
  }

  public void testEmptyCollection() throws Exception {
    doTest("GEOMETRYCOLLECTION EMPTY");
  }

  public void testLineNoding() throws Exception {
    doTest("MULTILINESTRING ((0 0, 5 0), (5 0, 10 0, 5 -5, 5 0), (5 0, 5 5))",
      "LINESTRING (0 0, 10 0, 5 -5, 5 5)");
  }

  public void testPoints() throws Exception {
    doTest("MULTIPOINT ((1 1), (2 2))", "POINT (1 1)", "POINT (2 2)");
  }

}
