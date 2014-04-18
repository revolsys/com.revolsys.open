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

import java.util.Arrays;
import java.util.Iterator;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTWriter;
import com.revolsys.jts.util.Assert;
import com.revolsys.jtstest.geomop.GeometryMethodOperation;
import com.revolsys.jtstest.test.Testable;
import com.revolsys.jtstest.testrunner.BooleanResult;
import com.revolsys.jtstest.testrunner.GeometryResult;
import com.revolsys.jtstest.testrunner.Result;
import com.revolsys.jtstest.testrunner.SimpleReportWriter;
import com.revolsys.jtstest.testrunner.Test;
import com.revolsys.jtstest.testrunner.TestCase;

/**
 * @version 1.7
 */
public class TestRunnerTestCaseAdapter implements Testable {
  private final TestCase testCase;

  private boolean ranAtLeastOnce = false;

  private final WKTWriter wktWriter = new WKTWriter();

  public TestRunnerTestCaseAdapter(final TestCase testCase) {
    this.testCase = testCase;
  }

  private Test getABTest(final String opName) {
    Assert.isTrue(GeometryMethodOperation.isBooleanFunction(opName)
      || GeometryMethodOperation.isGeometryFunction(opName));
    for (final Iterator i = testCase.getTests().iterator(); i.hasNext();) {
      final Test test = (Test)i.next();
      if (test.getOperation().equalsIgnoreCase(opName)
        && ((!opName.equalsIgnoreCase("relate")) || test.getExpectedResult()
          .equals(new BooleanResult(true)))
        && (test.getGeometryIndex().equalsIgnoreCase("A"))
        && ((test.getArgumentCount() == 0) || (test.getArgument(0) != null && (test.getArgument(0).equalsIgnoreCase("B"))))) {
        return test;
      }
    }
    return null;
  }

  private Result getDefaultResult(final String opName) {
    if (GeometryMethodOperation.isBooleanFunction(opName)) {
      return new BooleanResult(true);
    }
    if (GeometryMethodOperation.isGeometryFunction(opName)) {
      return new GeometryResult(testCase.getTestRun()
        .getGeometryFactory()
        .geometryCollection((Geometry[])null));
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  @Override
  public String getDescription() {
    return testCase.getDescription();
  }

  @Override
  public Geometry getExpectedBoundary() {
    return toGeometry(getABTest("getboundary"));
  }

  @Override
  public Geometry getExpectedConvexHull() {
    return toGeometry(getABTest("convexhull"));
  }

  @Override
  public Geometry getExpectedDifference() {
    return toGeometry(getABTest("difference"));
  }

  @Override
  public Geometry getExpectedIntersection() {
    return toGeometry(getABTest("intersection"));
  }

  @Override
  public String getExpectedIntersectionMatrix() {
    final Test test = getABTest("relate");
    if (test == null) {
      return null;
    }
    return test.getArgument(1);
  }

  @Override
  public Geometry getExpectedSymDifference() {
    return toGeometry(getABTest("symdifference"));
  }

  @Override
  public Geometry getExpectedUnion() {
    return toGeometry(getABTest("union"));
  }

  @Override
  public String getFailedMsg() {
    if (!ranAtLeastOnce) {
      return "";
    }
    for (final Iterator i = testCase.getTests().iterator(); i.hasNext();) {
      final Test test = (Test)i.next();
      if (!test.isPassed()) {
        final SimpleReportWriter reportWriter = new SimpleReportWriter(false);
        return reportWriter.write(test);
      }
    }
    return "";
  }

  @Override
  public Geometry getGeometry(final int index) {
    if (index == 0) {
      return testCase.getGeometryA();
    } else if (index == 1) {
      return testCase.getGeometryB();
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  @Override
  public IntersectionMatrix getIntersectionMatrix() {
    return testCase.getGeometryA().relate(testCase.getGeometryB());
  }

  @Override
  public String getName() {
    return testCase.getDescription();
  }

  private Test getOrCreateABTest(final String opName) {
    Test testToReturn = getABTest(opName);
    if (testToReturn == null) {
      testToReturn = new Test(testCase, maxTestIndex(testCase) + 1, null,
        opName, "A", Arrays.asList(new String[] {
          "B"
        }), getDefaultResult(opName), 0);
      testCase.add(testToReturn);
    }
    return testToReturn;
  }

  public TestCase getTestRunnerTestCase() {
    return testCase;
  }

  @Override
  public String getWellKnownText(final int index) {
    if (index == 0) {
      if (testCase.getGeometryA() == null) {
        return null;
      }
      return wktWriter.write(testCase.getGeometryA());
    } else if (index == 1) {
      if (testCase.getGeometryB() == null) {
        return null;
      }
      return wktWriter.write(testCase.getGeometryB());
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  @Override
  public void initGeometry() throws ParseException {
  }

  @Override
  public boolean isFailed() {
    if (!ranAtLeastOnce) {
      return false;
    }
    for (final Iterator i = testCase.getTests().iterator(); i.hasNext();) {
      final Test test = (Test)i.next();
      if (!test.isPassed()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isPassed() {
    if (!ranAtLeastOnce) {
      return false;
    }
    for (final Iterator i = testCase.getTests().iterator(); i.hasNext();) {
      final Test test = (Test)i.next();
      if (!test.isPassed()) {
        return false;
      }
    }
    return true;
  }

  private int maxTestIndex(final TestCase testCase) {
    int maxTestIndex = -1;
    for (final Iterator i = testCase.getTests().iterator(); i.hasNext();) {
      final Test test = (Test)i.next();
      maxTestIndex = Math.max(maxTestIndex, test.getTestIndex());
    }
    return maxTestIndex;
  }

  @Override
  public void runTest() throws ParseException {
    ranAtLeastOnce = true;
    testCase.run();
  }

  @Override
  public void setExpectedBoundary(final Geometry expectedBoundary) {
    setExpectedSpatialFunction("getboundary", expectedBoundary);
  }

  @Override
  public void setExpectedCentroid(final Geometry expected) {
    setExpectedSpatialFunction("centroid", expected);
  }

  @Override
  public void setExpectedConvexHull(final Geometry expectedConvexHull) {
    setExpectedSpatialFunction("convexhull", expectedConvexHull);
  }

  @Override
  public void setExpectedDifference(final Geometry expectedDifference) {
    setExpectedSpatialFunction("difference", expectedDifference);
  }

  @Override
  public void setExpectedIntersection(final Geometry expectedIntersection) {
    setExpectedSpatialFunction("intersection", expectedIntersection);
  }

  @Override
  public void setExpectedIntersectionMatrix(
    final String expectedIntersectionMatrix) {
    getOrCreateABTest("relate").setArgument(1, expectedIntersectionMatrix);
  }

  private void setExpectedSpatialFunction(final String opName,
    final Geometry expectedGeometry) {
    if (expectedGeometry == null) {
      getOrCreateABTest(opName).getTestCase().remove(getOrCreateABTest(opName));
      return;
    }
    getOrCreateABTest(opName).setResult(new GeometryResult(expectedGeometry));
  }

  @Override
  public void setExpectedSymDifference(final Geometry expectedSymDifference) {
    setExpectedSpatialFunction("symdifference", expectedSymDifference);
  }

  @Override
  public void setExpectedUnion(final Geometry expectedUnion) {
    setExpectedSpatialFunction("union", expectedUnion);
  }

  @Override
  public void setGeometry(final int index, final Geometry g) {
    if (index == 0) {
      testCase.setGeometryA(g);
    } else if (index == 1) {
      testCase.setGeometryB(g);
    } else {
      Assert.shouldNeverReachHere();
    }
  }

  @Override
  public void setIntersectionMatrix(final IntersectionMatrix im) {
  }

  @Override
  public void setName(final String name) {
    testCase.setDescription(name);
  }

  public Geometry toGeometry(final Test test) {
    if (test == null) {
      return null;
    }
    Assert.isTrue(test.getExpectedResult() instanceof GeometryResult);
    return ((GeometryResult)test.getExpectedResult()).getGeometry();
  }
}
