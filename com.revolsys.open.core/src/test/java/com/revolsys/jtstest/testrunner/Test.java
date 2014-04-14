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
package com.revolsys.jtstest.testrunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.geomop.GeometryOperation;

/**
 *  A test for two geometries.
 *
 * @version 1.7
 */
public class Test implements Runnable {
  private final String description;

  private final String operation;

  private Result expectedResult;

  private final int testIndex;

  private final String geometryIndex;

  private final ArrayList arguments;

  private final TestCase testCase;

  private boolean passed;

  private final double tolerance;

  // cache for actual computed result
  private Geometry targetGeometry;

  private Object[] operationArgs;

  private boolean isRun = false;

  private Result actualResult = null;

  private Exception exception = null;

  /**
   *  Creates a Test with the given description. The given operation (e.g.
   *  "equals") will be performed, the expected result of which is <tt>expectedResult</tt>.
   */
  public Test(final TestCase testCase, final int testIndex,
    final String description, final String operation,
    final String geometryIndex, final List arguments,
    final Result expectedResult, final double tolerance) {
    this.tolerance = tolerance;
    this.description = description;
    this.operation = operation;
    this.expectedResult = expectedResult;
    this.testIndex = testIndex;
    this.geometryIndex = geometryIndex;
    this.arguments = new ArrayList(arguments);
    this.testCase = testCase;
  }

  public boolean computePassed() throws Exception {
    final Result actualResult = getActualResult();
    final ResultMatcher matcher = testCase.getTestRun().getResultMatcher();

    // check that provided expected result geometry is valid
    // MD - disable except for testing
    // if (! isExpectedResultGeometryValid()) return false;

    return matcher.isMatch(targetGeometry, operation, operationArgs,
      actualResult, expectedResult, tolerance);
    // return expectedResult.equals(actualResult, tolerance);
  }

  private Object[] convertArgs(final List argStr) {
    final Object[] args = new Object[argStr.size()];
    for (int i = 0; i < args.length; i++) {
      args[i] = convertArgToGeomOrString((String)argStr.get(i));
    }
    return args;
  }

  private Object convertArgToGeomOrString(final String argStr) {
    if (argStr.equalsIgnoreCase("null")) {
      return null;
    }
    if (argStr.equalsIgnoreCase("A")) {
      return testCase.getGeometryA();
    }
    if (argStr.equalsIgnoreCase("B")) {
      return testCase.getGeometryB();
    }
    return argStr;
  }

  /**
   * Computes the actual result and caches the result value.
   * 
   * @return the actual result computed
   * @throws Exception if the operation fails
   */
  public Result getActualResult() throws Exception {
    if (isRun) {
      return actualResult;
    }

    isRun = true;
    targetGeometry = geometryIndex.equalsIgnoreCase("A") ? testCase.getGeometryA()
      : testCase.getGeometryB();

    operationArgs = convertArgs(arguments);
    final GeometryOperation op = getGeometryOperation();
    actualResult = op.invoke(operation, targetGeometry, operationArgs);
    return actualResult;
  }

  public String getArgument(final int i) {
    return (String)arguments.get(i);
  }

  public int getArgumentCount() {
    return arguments.size();
  }

  public String getDescription() {
    return description;
  }

  public Exception getException() {
    return exception;
  }

  public Result getExpectedResult() {
    return expectedResult;
  }

  public String getGeometryIndex() {
    return geometryIndex;
  }

  private GeometryOperation getGeometryOperation() {
    return testCase.getTestRun().getGeometryOperation();
  }

  public String getOperation() {
    return operation;
  }

  public TestCase getTestCase() {
    return testCase;
  }

  public int getTestIndex() {
    return testIndex;
  }

  private boolean isExpectedResultGeometryValid() {
    if (expectedResult instanceof GeometryResult) {
      final Geometry expectedGeom = ((GeometryResult)expectedResult).getGeometry();
      return expectedGeom.isValid();
    }
    return true;
  }

  /**
   *  Returns whether the Test is passed.
   */
  public boolean isPassed() {
    return passed;
  }

  public boolean isRun() {
    return isRun;
  }

  public void removeArgument(final int i) {
    arguments.remove(i);
  }

  @Override
  public void run() {
    try {
      exception = null;
      passed = computePassed();
    } catch (final Exception e) {
      exception = e;
    }
  }

  public void setArgument(final int i, final String value) {
    arguments.set(i, value);
  }

  public void setResult(final Result result) {
    this.expectedResult = result;
  }

  public String toXml() {
    String xml = "";
    xml += "<test>" + StringUtil.newLine;
    if (description != null && description.length() > 0) {
      xml += "  <desc>" + StringUtil.escapeHTML(description) + "</desc>"
        + StringUtil.newLine;
    }
    xml += "  <op name=\"" + operation + "\"";
    xml += " arg1=\"" + geometryIndex + "\"";
    int j = 2;
    for (final Iterator i = arguments.iterator(); i.hasNext();) {
      final String argument = (String)i.next();
      Assert.assertTrue(argument != null);
      xml += " arg" + j + "=\"" + argument + "\"";
      j++;
    }

    xml += ">" + StringUtil.newLine;
    xml += StringUtil.indent(expectedResult.toFormattedString(), 4)
      + StringUtil.newLine;
    xml += "  </op>" + StringUtil.newLine;
    xml += "</test>" + StringUtil.newLine;
    return xml;
  }

}
