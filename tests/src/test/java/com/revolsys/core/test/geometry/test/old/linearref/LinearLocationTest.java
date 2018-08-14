package com.revolsys.core.test.geometry.test.old.linearref;

import com.revolsys.geometry.linearref.LinearLocation;
import com.revolsys.geometry.linearref.LocationIndexedLine;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests methods involving only {@link LinearLocation}s
 *
 * @author Martin Davis
 *
 */
public class LinearLocationTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(LinearLocationTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.DEFAULT_2D;

  public LinearLocationTest(final String name) {
    super(name);
  }

  public void testGetSegmentMultiLineString() throws Exception {
    final Geometry line = this.geometryFactory
      .geometry("MULTILINESTRING ((0 0, 10 0, 20 0), (20 0, 30 0))");
    final LocationIndexedLine indexedLine = new LocationIndexedLine(line);

    final LinearLocation loc0 = indexedLine.indexOf(new PointDoubleXY(0, 0));
    final LinearLocation loc0_5 = indexedLine.indexOf(new PointDoubleXY(5, 0));
    final LinearLocation loc1 = indexedLine.indexOf(new PointDoubleXY(10, 0));
    final LinearLocation loc2 = indexedLine.indexOf(new PointDoubleXY(20, 0));
    final LinearLocation loc2B = new LinearLocation(1, 0, 0.0);

    final LinearLocation loc2_5 = indexedLine.indexOf(new PointDoubleXY(25, 0));
    final LinearLocation loc3 = indexedLine.indexOf(new PointDoubleXY(30, 0));

    final LineSegment seg0 = new LineSegmentDouble(new PointDoubleXY(0, 0),
      new PointDoubleXY(10, 0));
    final LineSegment seg1 = new LineSegmentDouble(new PointDoubleXY(10, 0),
      new PointDoubleXY(20, 0));
    final LineSegment seg2 = new LineSegmentDouble(new PointDoubleXY(20, 0),
      new PointDoubleXY(30, 0));

    assertTrue(loc0.getSegment(line).equals(seg0));
    assertTrue(loc0_5.getSegment(line).equals(seg0));

    assertTrue(loc1.getSegment(line).equals(seg1));
    assertTrue(loc2.getSegment(line).equals(seg1));

    assertTrue(loc2_5.getSegment(line).equals(seg2));
    assertTrue(loc3.getSegment(line).equals(seg2));
  }

  public void testRepeatedCoordsLineString() throws Exception {
    final Geometry line = this.geometryFactory.geometry("LINESTRING (10 0, 10 0, 20 0)");
    final LocationIndexedLine indexedLine = new LocationIndexedLine(line);
    final LinearLocation loc0 = indexedLine.indexOf(new PointDoubleXY(11, 0));
    assertTrue(loc0.compareTo(new LinearLocation(1, 0.1)) == 0);
  }

  public void testSameSegmentLineString() throws Exception {
    final Geometry line = this.geometryFactory.geometry("LINESTRING (0 0, 10 0, 20 0, 30 0)");
    final LocationIndexedLine indexedLine = new LocationIndexedLine(line);

    final LinearLocation loc0 = indexedLine.indexOf(new PointDoubleXY(0, 0));
    final LinearLocation loc0_5 = indexedLine.indexOf(new PointDoubleXY(5, 0));
    final LinearLocation loc1 = indexedLine.indexOf(new PointDoubleXY(10, 0));
    final LinearLocation loc2 = indexedLine.indexOf(new PointDoubleXY(20, 0));
    final LinearLocation loc2_5 = indexedLine.indexOf(new PointDoubleXY(25, 0));
    final LinearLocation loc3 = indexedLine.indexOf(new PointDoubleXY(30, 0));

    assertTrue(loc0.isOnSameSegment(loc0));
    assertTrue(loc0.isOnSameSegment(loc0_5));
    assertTrue(loc0.isOnSameSegment(loc1));
    assertTrue(!loc0.isOnSameSegment(loc2));
    assertTrue(!loc0.isOnSameSegment(loc2_5));
    assertTrue(!loc0.isOnSameSegment(loc3));

    assertTrue(loc0_5.isOnSameSegment(loc0));
    assertTrue(loc0_5.isOnSameSegment(loc1));
    assertTrue(!loc0_5.isOnSameSegment(loc2));
    assertTrue(!loc0_5.isOnSameSegment(loc3));

    assertTrue(!loc2.isOnSameSegment(loc0));
    assertTrue(loc2.isOnSameSegment(loc1));
    assertTrue(loc2.isOnSameSegment(loc2));
    assertTrue(loc2.isOnSameSegment(loc3));

    assertTrue(loc2_5.isOnSameSegment(loc3));

    assertTrue(!loc3.isOnSameSegment(loc0));
    assertTrue(loc3.isOnSameSegment(loc2));
    assertTrue(loc3.isOnSameSegment(loc2_5));
    assertTrue(loc3.isOnSameSegment(loc3));

  }

  public void testSameSegmentMultiLineString() throws Exception {
    final Geometry line = this.geometryFactory
      .geometry("MULTILINESTRING ((0 0, 10 0, 20 0), (20 0, 30 0))");
    final LocationIndexedLine indexedLine = new LocationIndexedLine(line);

    final LinearLocation loc0 = indexedLine.indexOf(new PointDoubleXY(0, 0));
    final LinearLocation loc0_5 = indexedLine.indexOf(new PointDoubleXY(5, 0));
    final LinearLocation loc1 = indexedLine.indexOf(new PointDoubleXY(10, 0));
    final LinearLocation loc2 = indexedLine.indexOf(new PointDoubleXY(20, 0));
    final LinearLocation loc2B = new LinearLocation(1, 0, 0.0);

    final LinearLocation loc2_5 = indexedLine.indexOf(new PointDoubleXY(25, 0));
    final LinearLocation loc3 = indexedLine.indexOf(new PointDoubleXY(30, 0));

    assertTrue(loc0.isOnSameSegment(loc0));
    assertTrue(loc0.isOnSameSegment(loc0_5));
    assertTrue(loc0.isOnSameSegment(loc1));
    assertTrue(!loc0.isOnSameSegment(loc2));
    assertTrue(!loc0.isOnSameSegment(loc2_5));
    assertTrue(!loc0.isOnSameSegment(loc3));

    assertTrue(loc0_5.isOnSameSegment(loc0));
    assertTrue(loc0_5.isOnSameSegment(loc1));
    assertTrue(!loc0_5.isOnSameSegment(loc2));
    assertTrue(!loc0_5.isOnSameSegment(loc3));

    assertTrue(!loc2.isOnSameSegment(loc0));
    assertTrue(loc2.isOnSameSegment(loc1));
    assertTrue(loc2.isOnSameSegment(loc2));
    assertTrue(!loc2.isOnSameSegment(loc3));
    assertTrue(loc2B.isOnSameSegment(loc3));

    assertTrue(loc2_5.isOnSameSegment(loc3));

    assertTrue(!loc3.isOnSameSegment(loc0));
    assertTrue(!loc3.isOnSameSegment(loc2));
    assertTrue(loc3.isOnSameSegment(loc2B));
    assertTrue(loc3.isOnSameSegment(loc2_5));
    assertTrue(loc3.isOnSameSegment(loc3));
  }

  public void testZeroLengthLineString() throws Exception {
    final Geometry line = this.geometryFactory.geometry("LINESTRING (10 0, 10 0)");
    final LocationIndexedLine indexedLine = new LocationIndexedLine(line);
    final LinearLocation loc0 = indexedLine.indexOf(new PointDoubleXY(11, 0));
    assertTrue(loc0.compareTo(new LinearLocation(1, 0.0)) == 0);
  }

}
