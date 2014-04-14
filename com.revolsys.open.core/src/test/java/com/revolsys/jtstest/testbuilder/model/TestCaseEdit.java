
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
package com.revolsys.jtstest.testbuilder.model;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jtstest.test.TestCase;
import com.revolsys.jtstest.test.Testable;

/**
 * @version 1.7
 */
public class TestCaseEdit implements Testable {
  private static Geometry cloneGeometry(final Geometry geom) {
    if (geom == null) {
      return null;
    }
    return geom.clone();
  }

  private final Testable testable;

  private String opName = "";

  private Geometry resultGeom = null;

  public TestCaseEdit(final Geometry[] geom) {
    this.testable = new TestCase();
    setGeometry(0, geom[0]);
    setGeometry(1, geom[1]);
  }

  public TestCaseEdit(final Geometry[] geom, final String name) {
    this.testable = new TestCase();
    setGeometry(0, geom[0]);
    setGeometry(1, geom[1]);
    testable.setName(name);
  }

  public TestCaseEdit(final GeometryFactory geometryFactory) {
    final TestCase testCase = new TestCase(geometryFactory);
    testable = testCase;
  }

  public TestCaseEdit(final Testable tc) throws ParseException {
    this.testable = tc;
    testable.initGeometry();
    setGeometry(0, testable.getGeometry(0));
    setGeometry(1, testable.getGeometry(1));
  }

  public TestCaseEdit(final TestCaseEdit tce) {
    this.testable = new TestCase();
    setGeometry(0, tce.getGeometry(0));
    setGeometry(1, tce.getGeometry(1));
  }

  public void exchange() {
    final Geometry temp = testable.getGeometry(0);
    testable.setGeometry(0, testable.getGeometry(1));
    testable.setGeometry(1, temp);
  }

  @Override
  public String getDescription() {
    return testable.getDescription();
  }

  @Override
  public Geometry getExpectedBoundary() {
    return testable.getExpectedBoundary();
  }

  @Override
  public Geometry getExpectedConvexHull() {
    return testable.getExpectedConvexHull();
  }

  @Override
  public Geometry getExpectedDifference() {
    return testable.getExpectedDifference();
  }

  @Override
  public Geometry getExpectedIntersection() {
    return testable.getExpectedIntersection();
  }

  @Override
  public String getExpectedIntersectionMatrix() {
    return testable.getExpectedIntersectionMatrix();
  }

  @Override
  public Geometry getExpectedSymDifference() {
    return testable.getExpectedSymDifference();
  }

  @Override
  public Geometry getExpectedUnion() {
    return testable.getExpectedUnion();
  }

  @Override
  public String getFailedMsg() {
    return testable.getFailedMsg();
  }

  public Geometry[] getGeometries() {
    return new Geometry[] {
      testable.getGeometry(0), testable.getGeometry(1)
    };
  }

  @Override
  public Geometry getGeometry(final int i) {
    // return geom[i];
    return testable.getGeometry(i);
  }

  public IntersectionMatrix getIM() {
    runRelate();
    return testable.getIntersectionMatrix();
  }

  @Override
  public IntersectionMatrix getIntersectionMatrix() {
    return testable.getIntersectionMatrix();
  }

  @Override
  public String getName() {
    return testable.getName();
  }

  public String getOpName() {
    return opName;
  }

  public Geometry getResult() {
    return resultGeom;
  }

  public Testable getTestable() {
    return testable;
  }

  @Override
  public String getWellKnownText(final int i) {
    return testable.getWellKnownText(i);
  }

  @Override
  public void initGeometry() throws ParseException {
    testable.initGeometry();
  }

  @Override
  public boolean isFailed() {
    return testable.isFailed();
  }

  @Override
  public boolean isPassed() {
    return testable.isPassed();
  }

  void runRelate() {
    final Geometry[] geom = getGeometries();
    if (geom[0] == null || geom[1] == null) {
      return;
    }
    testable.setIntersectionMatrix(geom[0].relate(geom[1]));
  }

  @Override
  public void runTest() throws ParseException {
    testable.runTest();
  }

  @Override
  public void setExpectedBoundary(final Geometry expectedBoundary) {
    testable.setExpectedBoundary(expectedBoundary);
  }

  @Override
  public void setExpectedCentroid(final Geometry expectedCentroid) {
    testable.setExpectedCentroid(expectedCentroid);
  }

  @Override
  public void setExpectedConvexHull(final Geometry expectedConvexHull) {
    testable.setExpectedConvexHull(expectedConvexHull);
  }

  @Override
  public void setExpectedDifference(final Geometry expectedDifference) {
    testable.setExpectedDifference(expectedDifference);
  }

  @Override
  public void setExpectedIntersection(final Geometry expectedIntersection) {
    testable.setExpectedIntersection(expectedIntersection);
  }

  @Override
  public void setExpectedIntersectionMatrix(
    final String expectedIntersectionMatrix) {
    testable.setExpectedIntersectionMatrix(expectedIntersectionMatrix);
  }

  @Override
  public void setExpectedSymDifference(final Geometry expectedSymDifference) {
    testable.setExpectedSymDifference(expectedSymDifference);
  }

  @Override
  public void setExpectedUnion(final Geometry expectedUnion) {
    testable.setExpectedUnion(expectedUnion);
  }

  @Override
  public void setGeometry(final int i, final Geometry geom) {
    testable.setGeometry(i, geom);
  }

  @Override
  public void setIntersectionMatrix(final IntersectionMatrix im) {
    testable.setIntersectionMatrix(im);
  }

  @Override
  public void setName(final String name) {
    testable.setName(name);
  }

  public void setOpName(final String name) {
    opName = name;
  }

  public void setResult(final Geometry geom) {
    resultGeom = geom;
  }
}
