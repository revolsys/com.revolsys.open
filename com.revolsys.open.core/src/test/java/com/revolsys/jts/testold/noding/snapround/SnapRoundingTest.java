package com.revolsys.jts.testold.noding.snapround;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDouble;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.noding.snapround.GeometryNoder;

/**
 * Test Snap Rounding
 *
 * @version 1.7
 */
public class SnapRoundingTest extends TestCase {

  static final double SNAP_TOLERANCE = 1.0;

  public static void main(final String args[]) {
    TestRunner.run(SnapRoundingTest.class);
  }

  WKTReader rdr = new WKTReader();

  public SnapRoundingTest(final String name) {
    super(name);
  }

  List fromWKT(final String[] wkts) {
    final List geomList = new ArrayList();
    for (final String wkt : wkts) {
      try {
        geomList.add(this.rdr.read(wkt));
      } catch (final Exception ex) {
        ex.printStackTrace();
      }
    }
    return geomList;
  }

  private boolean isSnapped(final Point v, final Point p0,
    final Point p1) {
    if (v.equals(2,p0)) {
      return true;
    }
    if (v.equals(2,p1)) {
      return true;
    }
    final LineSegment seg = new LineSegmentDouble(p0, p1);
    final double dist = seg.distance(v);
    if (dist < SNAP_TOLERANCE / 2.05) {
      return false;
    }
    return true;
  }

  private boolean isSnapped(final Point v, final List lines) {
    for (int i = 0; i < lines.size(); i++) {
      final LineString line = (LineString)lines.get(i);
      for (int j = 0; j < line.getVertexCount() - 1; j++) {
        final Point p0 = line.getPoint(j);
        final Point p1 = line.getPoint(j + 1);
        if (!isSnapped(v, p0, p1)) {
          return false;
        }
      }
    }
    return true;
  }

  boolean isSnapped(final List lines, final double tol) {
    for (int i = 0; i < lines.size(); i++) {
      final LineString line = (LineString)lines.get(i);
      for (int j = 0; j < line.getVertexCount(); j++) {
        final Point v = line.getPoint(j);
        if (!isSnapped(v, lines)) {
          return false;
        }

      }
    }
    return true;
  }

  void runRounding(final String[] wkt) {
    final List geoms = fromWKT(wkt);
    final GeometryNoder noder = new GeometryNoder(SNAP_TOLERANCE);
    noder.setValidate(true);
    final List nodedLines = noder.node(geoms);
    /*
     * for (Iterator it = nodedLines.iterator(); it.hasNext(); ) {
     * System.out.println(it.next()); }
     */
    assertTrue(isSnapped(nodedLines, SNAP_TOLERANCE));
  }

  public void testBadLines1() {
    final String[] badLines1 = {
      "LINESTRING ( 171 157, 175 154, 170 154, 170 155, 170 156, 170 157, 171 158, 171 159, 172 160, 176 156, 171 156, 171 159, 176 159, 172 155, 170 157, 174 161, 174 156, 173 156, 172 156 )"
    };
    runRounding(badLines1);
  }

  public void testBadLines2() {
    final String[] badLines2 = {
      "LINESTRING ( 175 222, 176 222, 176 219, 174 221, 175 222, 177 220, 174 220, 174 222, 177 222, 175 220, 174 221 )"
    };
    runRounding(badLines2);
  }

  public void testBadNoding1() {
    final String[] badNoding1 = {
      "LINESTRING ( 76 47, 81 52, 81 53, 85 57, 88 62, 89 64, 57 80, 82 55, 101 74, 76 99, 92 67, 94 68, 99 71, 103 75, 139 111 )"
    };
    runRounding(badNoding1);
  }

  public void testBadNoding1Extract() {
    final String[] badNoding1Extract = {
      "LINESTRING ( 82 55, 101 74 )", "LINESTRING ( 94 68, 99 71 )",
      "LINESTRING ( 85 57, 88 62 )"
    };
    runRounding(badNoding1Extract);
  }

  public void testBadNoding1ExtractShift() {
    final String[] badNoding1ExtractShift = {
      "LINESTRING ( 0 0, 19 19 )", "LINESTRING ( 12 13, 17 16 )",
      "LINESTRING ( 3 2, 6 7 )"
    };
    runRounding(badNoding1ExtractShift);
  }

  public void testCollapse1() {
    final String[] collapse1 = {
      "LINESTRING ( 362 177, 375 164, 374 164, 372 161, 373 163, 372 165, 373 164, 442 58 )"
    };
    runRounding(collapse1);
  }

  public void testCollapse2() {
    final String[] collapse2 = {
      "LINESTRING ( 393 175, 391 173, 390 175, 391 174, 391 173 )"
    };
    runRounding(collapse2);
  }

  public void testLineStringLongShort() {
    final String[] geoms = {
      "LINESTRING (0 0, 2 0)", "LINESTRING (0 0, 10 -1)"
    };
    runRounding(geoms);
  }

  public void testPolyWithCloseNode() {
    final String[] polyWithCloseNode = {
      "POLYGON ((20 0, 20 160, 140 1, 160 160, 160 1, 20 0))"
    };
    runRounding(polyWithCloseNode);
  }

}
