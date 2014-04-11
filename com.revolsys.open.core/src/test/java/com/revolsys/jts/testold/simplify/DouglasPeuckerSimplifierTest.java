package com.revolsys.jts.testold.simplify;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.simplify.DouglasPeuckerSimplifier;

/**
 * @version 1.7
 */
public class DouglasPeuckerSimplifierTest extends TestCase {
  public static void main(final String[] args) {
    junit.textui.TestRunner.run(DouglasPeuckerSimplifierTest.class);
  }

  public DouglasPeuckerSimplifierTest(final String name) {
    super(name);
  }

  public void testEmptyPolygon() throws Exception {
    final String geomStr = "POLYGON(EMPTY)";
    new GeometryOperationValidator(DPSimplifierResult.getResult(geomStr, 1)).setExpectedResult(
      geomStr)
      .test();
  }

  public void testFlattishPolygon() throws Exception {
    new GeometryOperationValidator(DPSimplifierResult.getResult(
      "POLYGON ((0 0, 50 0, 53 0, 55 0, 100 0, 70 1,  60 1, 50 1, 40 1, 0 0))",
      10.0)).test();
  }

  public void testGeometryCollection() throws Exception {
    new GeometryOperationValidator(
      DPSimplifierResult.getResult(
        "GEOMETRYCOLLECTION ("
          + "MULTIPOINT (80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120),"
          + "POLYGON ((80 200, 240 200, 240 60, 80 60, 80 200)),"
          + "LINESTRING (80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120)"
          + ")", 10.0)).test();
  }

  public void testMultiLineString() throws Exception {
    new GeometryOperationValidator(
      DPSimplifierResult.getResult(
        "MULTILINESTRING( (0 0, 50 0, 70 0, 80 0, 100 0), (0 0, 50 1, 60 1, 100 0) )",
        10.0)).test();
  }

  public void testMultiLineStringWithEmpty() throws Exception {
    new GeometryOperationValidator(
      DPSimplifierResult.getResult(
        "MULTILINESTRING( EMPTY, (0 0, 50 0, 70 0, 80 0, 100 0), (0 0, 50 1, 60 1, 100 0) )",
        10.0)).test();
  }

  public void testMultiPoint() throws Exception {
    final String geomStr = "MULTIPOINT(80 200, 240 200, 240 60, 80 60, 80 200, 140 199, 120 120)";
    new GeometryOperationValidator(TPSimplifierResult.getResult(geomStr, 10.0)).setExpectedResult(
      geomStr)
      .test();
  }

  public void testMultiPolygonWithEmpty() throws Exception {
    new GeometryOperationValidator(
      DPSimplifierResult.getResult(
        "MULTIPOLYGON (EMPTY, ((-36 91.5, 4.5 91.5, 4.5 57.5, -36 57.5, -36 91.5)), ((25.5 57.5, 61.5 57.5, 61.5 23.5, 25.5 23.5, 25.5 57.5)))",
        10.0)).test();
  }

  public void testPoint() throws Exception {
    final String geomStr = "POINT (10 10)";
    new GeometryOperationValidator(DPSimplifierResult.getResult(geomStr, 1)).setExpectedResult(
      geomStr)
      .test();
  }

  public void testPolygonNoReduction() throws Exception {
    new GeometryOperationValidator(
      DPSimplifierResult.getResult(
        "POLYGON ((20 220, 40 220, 60 220, 80 220, 100 220, 120 220, 140 220, 140 180, 100 180, 60 180,     20 180, 20 220))",
        10.0)).test();
  }

  public void testPolygonReduction() throws Exception {
    new GeometryOperationValidator(
      DPSimplifierResult.getResult(
        "POLYGON ((120 120, 121 121, 122 122, 220 120, 180 199, 160 200, 140 199, 120 120))",
        10.0)).test();
  }

  public void testPolygonReductionWithSplit() throws Exception {
    new GeometryOperationValidator(DPSimplifierResult.getResult(
      "POLYGON ((40 240, 160 241, 280 240, 280 160, 160 240, 40 140, 40 240))",
      10.0)).test();
  }

  public void testPolygonWithTouchingHole() throws Exception {
    new GeometryOperationValidator(
      DPSimplifierResult.getResult(
        "POLYGON ((80 200, 240 200, 240 60, 80 60, 80 200), (120 120, 220 120, 180 199, 160 200, 140 199, 120 120))",
        10.0)).setExpectedResult(
      "POLYGON ((80 200, 160 200, 240 200, 240 60, 80 60, 80 200), (160 200, 140 199, 120 120, 220 120, 180 199, 160 200)))")
      .test();
  }

  public void testTinyHole() throws Exception {
    new GeometryOperationValidator(
      DPSimplifierResult.getResult(
        "POLYGON ((10 10, 10 310, 370 310, 370 10, 10 10), (160 190, 180 190, 180 170, 160 190))",
        30.0)).testEmpty(false);
  }

  public void testTinyLineString() throws Exception {
    new GeometryOperationValidator(DPSimplifierResult.getResult(
      "LINESTRING (0 5, 1 5, 2 5, 5 5)", 10.0)).test();
  }

  public void testTinySquare() throws Exception {
    new GeometryOperationValidator(DPSimplifierResult.getResult(
      "POLYGON ((0 5, 5 5, 5 0, 0 0, 0 1, 0 5))", 10.0)).test();
  }
}

class DPSimplifierResult {
  private static WKTReader rdr = new WKTReader();

  public static Geometry[] getResult(final String wkt, final double tolerance)
    throws ParseException {
    final Geometry[] ioGeom = new Geometry[2];
    ioGeom[0] = rdr.read(wkt);
    ioGeom[1] = DouglasPeuckerSimplifier.simplify(ioGeom[0], tolerance);
    System.out.println(ioGeom[1]);
    return ioGeom;
  }
}
