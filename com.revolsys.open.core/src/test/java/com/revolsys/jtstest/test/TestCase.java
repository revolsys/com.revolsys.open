/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.test;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.util.Assert;

/**
 * @version 1.7
 */
public class TestCase implements Testable {
  private GeometryFactory geometryFactory = GeometryFactory.getFactory(0, 2);

  protected String name, description, expectedIM;

  protected boolean isRun = false;

  protected boolean failed = false;

  // protected boolean passed = false;
  protected String failedMsg = "";

  private Geometry expectedConvexHull = null;

  private Geometry expectedBoundary = null;

  private Geometry expectedIntersection = null;

  private Geometry expectedUnion = null;

  private Geometry expectedDifference = null;

  private Geometry expectedSymDifference = null;

  private Geometry expectedCentroid = null;

  private IntersectionMatrix im;

  private final Geometry[] geom = new Geometry[2];

  private String wkta;

  private String wktb;

  public TestCase() {
    this(null, null, null, null, null, null, null, null, null, null);
  }

  public TestCase(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public TestCase(final String name) {
    this(name, null, null, null, null, null, null, null, null, null);
  }

  public TestCase(final String name, final String description,
    final String wkta, final String wktb, final String expectedIM) {
    this(name, description, wkta, wktb, expectedIM, null, null, null, null,
      null);
  }

  public TestCase(final String name, final String description,
    final String wkta, final String wktb, final String expectedIM,
    final String expectedConvexHull, final String expectedIntersection,
    final String expectedUnion, final String expectedDifference,
    final String expectedSymDifference) {
    this(name, description, wkta, wktb, expectedIM, expectedConvexHull,
      expectedIntersection, expectedUnion, expectedDifference,
      expectedSymDifference, null);
  }

  public TestCase(final String name, final String description,
    final String wkta, final String wktb, final String expectedIM,
    final String expectedConvexHull, final String expectedIntersection,
    final String expectedUnion, final String expectedDifference,
    final String expectedSymDifference, final String expectedBoundary) {
    try {
      init(name, description, wkta, wktb, expectedIM,
        toNullOrGeometry(expectedConvexHull),
        toNullOrGeometry(expectedIntersection),
        toNullOrGeometry(expectedUnion), toNullOrGeometry(expectedDifference),
        toNullOrGeometry(expectedSymDifference),
        toNullOrGeometry(expectedBoundary));
    } catch (final ParseException e) {
      Assert.shouldNeverReachHere();
    }
  }

  public TestCase(final TestCase tc) {
    init(tc.name, tc.description, tc.getWellKnownText(0),
      tc.getWellKnownText(1), tc.expectedIM, tc.getExpectedConvexHull(),
      tc.getExpectedIntersection(), tc.getExpectedUnion(),
      tc.getExpectedDifference(), tc.getExpectedSymDifference(),
      tc.getExpectedBoundary());
  }

  void assertEquals(final Object o1, final Object o2, final String msg) {
    assertTrue(o1.equals(o2), msg);
  }

  void assertEqualsExact(final Geometry g1, final Geometry g2, final String msg) {
    final Geometry g1Clone = g1.normalize();
    final Geometry g2Clone = g2.normalize();
    assertTrue(g1Clone.equalsExact2d(g2Clone), msg);
  }

  void assertTrue(final boolean val, final String msg) {
    if (!val) {
      failed = true;
      failedMsg = msg;
    }
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Geometry getExpectedBoundary() {
    return expectedBoundary;
  }

  @Override
  public Geometry getExpectedConvexHull() {
    return expectedConvexHull;
  }

  @Override
  public Geometry getExpectedDifference() {
    return expectedDifference;
  }

  @Override
  public Geometry getExpectedIntersection() {
    return expectedIntersection;
  }

  @Override
  public String getExpectedIntersectionMatrix() {
    return expectedIM;
  }

  @Override
  public Geometry getExpectedSymDifference() {
    return expectedSymDifference;
  }

  @Override
  public Geometry getExpectedUnion() {
    return expectedUnion;
  }

  @Override
  public String getFailedMsg() {
    return failedMsg;
  }

  public Geometry[] getGeometries() {
    return geom;
  }

  @Override
  public Geometry getGeometry(final int index) {
    return geom[index];
  }

  @Override
  public IntersectionMatrix getIntersectionMatrix() {
    return im;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getWellKnownText(final int i) {
    if (geom[i] == null) {
      return null;
    }
    return geom[i].toWkt();
  }

  void init(final String name, final String description, final String wkta,
    final String wktb, final String expectedIM,
    final Geometry expectedConvexHull, final Geometry expectedIntersection,
    final Geometry expectedUnion, final Geometry expectedDifference,
    final Geometry expectedSymDifference, final Geometry expectedBoundary) {
    this.name = name;
    this.description = description;
    this.wkta = wkta;
    this.wktb = wktb;
    this.expectedIM = expectedIM;
    this.expectedConvexHull = expectedConvexHull;
    this.expectedBoundary = expectedBoundary;
    this.expectedIntersection = expectedIntersection;
    this.expectedUnion = expectedUnion;
    this.expectedDifference = expectedDifference;
    this.expectedSymDifference = expectedSymDifference;
  }

  @Override
  public void initGeometry() throws ParseException {
    final WKTReader wktRdr = new WKTReader(geometryFactory);
    if (geom[0] != null) {
      return;
    }
    if (wkta != null) {
      geom[0] = wktRdr.read(wkta);
    }
    if (wktb != null) {
      geom[1] = wktRdr.read(wktb);
    }
  }

  @Override
  public boolean isFailed() {
    return failed;
  }

  @Override
  public boolean isPassed() {
    return isRun && !failed;
  }

  public boolean isRun() {
    return isRun;
  }

  IntersectionMatrix relate(final Geometry a, final Geometry b) {
    return a.relate(b);
  }

  @Override
  public void runTest() throws ParseException {
    failed = false;
    isRun = true;
    initGeometry();
    if (expectedIM != null) {
      IntersectionMatrix im = null;
      if (geom[0] != null && geom[1] != null) {
        im = relate(geom[0], geom[1]);
      }
      if (im != null) {
        final String msg = " expected " + expectedIM + ", found "
          + im.toString();
        assertTrue(im.matches(expectedIM), msg);
      }
    }
    if (expectedBoundary != null) {
      final Geometry result = geom[0].getBoundary();
      assertEqualsExact(expectedBoundary, result, " expected boundary "
        + expectedBoundary.toWkt() + " , found " + result.toWkt());
    }
    if (expectedConvexHull != null) {
      final Geometry result = geom[0].convexHull();
      assertEqualsExact(expectedConvexHull, result, " expected convex hull "
        + expectedConvexHull.toWkt() + " , found " + result.toWkt());
    }
    if (expectedIntersection != null) {
      final Geometry result = geom[0].intersection(geom[1]);
      assertEqualsExact(expectedIntersection, result, " expected intersection "
        + expectedIntersection.toWkt() + " , found " + result.toWkt());
    }
    if (expectedUnion != null) {
      final Geometry result = geom[0].union(geom[1]);
      assertEqualsExact(expectedUnion, result, " expected union "
        + expectedUnion.toWkt() + " , found " + result.toWkt());
    }
    if (expectedDifference != null) {
      final Geometry result = geom[0].difference(geom[1]);
      assertEqualsExact(expectedDifference, result, " expected difference "
        + expectedDifference.toWkt() + " , found " + result.toWkt());
    }
    if (expectedSymDifference != null) {
      final Geometry result = geom[0].symDifference(geom[1]);
      assertEqualsExact(expectedSymDifference, result,
        " expected sym difference " + expectedSymDifference.toWkt()
          + " , found " + result.toWkt());
    }
  }

  public TestCase setA(final String wkta) {
    this.wkta = wkta;
    return this;
  }

  public TestCase setB(final String wktb) {
    this.wktb = wktb;
    return this;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public void setExpectedBoundary(final Geometry expectedBoundary) {
    this.expectedBoundary = expectedBoundary;
  }

  public TestCase setExpectedBoundary(final String wkt) {
    try {
      this.expectedBoundary = toNullOrGeometry(wkt);
    } catch (final ParseException e) {
      Assert.shouldNeverReachHere();
    }
    return this;
  }

  @Override
  public void setExpectedCentroid(final Geometry expectedCentroid) {
    this.expectedCentroid = expectedCentroid;
  }

  @Override
  public void setExpectedConvexHull(final Geometry expectedConvexHull) {
    this.expectedConvexHull = expectedConvexHull;
  }

  @Override
  public void setExpectedDifference(final Geometry expectedDifference) {
    this.expectedDifference = expectedDifference;
  }

  @Override
  public void setExpectedIntersection(final Geometry expectedIntersection) {
    this.expectedIntersection = expectedIntersection;
  }

  public TestCase setExpectedIntersection(final String wkt) {
    try {
      this.expectedIntersection = toNullOrGeometry(wkt);
    } catch (final ParseException e) {
      Assert.shouldNeverReachHere();
    }
    return this;
  }

  @Override
  public void setExpectedIntersectionMatrix(
    final String expectedIntersectionMatrix) {
    expectedIM = expectedIntersectionMatrix;
  }

  public TestCase setExpectedRelateMatrix(
    final String expectedIntersectionMatrix) {
    expectedIM = expectedIntersectionMatrix;
    return this;
  }

  @Override
  public void setExpectedSymDifference(final Geometry expectedSymDifference) {
    this.expectedSymDifference = expectedSymDifference;
  }

  @Override
  public void setExpectedUnion(final Geometry expectedUnion) {
    this.expectedUnion = expectedUnion;
  }

  @Override
  public void setGeometry(final int index, final Geometry g) {
    geom[index] = g;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public void setIntersectionMatrix(final IntersectionMatrix im) {
    this.im = im;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  public TestCase setTestName(final String name) {
    this.name = name;
    return this;
  }

  private Geometry toNullOrGeometry(final String wellKnownText)
    throws ParseException {
    if (wellKnownText == null) {
      return null;
    }
    final WKTReader wktRdr = new WKTReader(geometryFactory);
    return wktRdr.read(wellKnownText);
  }

}
