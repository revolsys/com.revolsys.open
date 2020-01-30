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
package com.revolsys.core.test.geometry.test.testrunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.core.test.geometry.test.geomop.GeometryOperation;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.util.Property;

import junit.framework.Assert;

/**
 *  A test for two geometries.
 *
 * @version 1.7
 */
public class GeometryOperationTest extends junit.framework.TestCase implements MapSerializer {
  private Result actualResult = null;

  private List<String> arguments = new ArrayList<>();

  private final Exception exception = null;

  private Result expectedResult;

  private final String geometryIndex;

  private boolean isRun = false;

  private final String operation;

  private Object[] operationArgs;

  private boolean passed;

  // cache for actual computed result
  private Geometry targetGeometry;

  private final TestCase testCase;

  private String testDescription;

  private final int testIndex;

  private final double tolerance;

  /**
   *  Creates a Test with the given description. The given operation (e.g.
   *  "equals") will be performed, the expected result of which is <tt>expectedResult</tt>.
   */
  public GeometryOperationTest(final TestCase testCase, final int testIndex,
    final String description, final String operation, final String geometryIndex,
    final List<String> arguments, final Result expectedResult, final double tolerance) {
    this.tolerance = tolerance;
    this.testDescription = StringUtils.trimWhitespace(description);
    if (!Property.hasValue(description)) {
      this.testDescription = operation;
    }
    setName(testCase.getId() + "." + testIndex + "." + this.testDescription);
    this.operation = operation;
    this.expectedResult = expectedResult;
    this.testIndex = testIndex;
    this.geometryIndex = geometryIndex;
    this.arguments = new ArrayList<>(arguments);
    this.testCase = testCase;
  }

  // @SuppressWarnings("unchecked")
  // public GeometryOperationTest(final TestCase testCase, final int testIndex,
  // final Map<String, Object> map) {
  // this.testCase = testCase;
  // this.testIndex = testIndex;
  // this.testDescription = (String)map.get("description");
  // this.operation = (String)map.get("operation");
  // this.geometryIndex = (String)map.get("geometryIndex");
  // this.arguments = (List<String>)map.get("arguments");
  // }

  public boolean computePassed() throws Exception {
    final Result actualResult = getActualResult();
    final ResultMatcher matcher = this.testCase.getTestRun().getResultMatcher();

    // check that provided expected result geometry is valid
    // MD - disable except for testing
    // if (! isExpectedResultGeometryValid()) return false;

    return matcher.isMatch(this.targetGeometry, this.operation, this.operationArgs, actualResult,
      this.expectedResult, this.tolerance);
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
      return this.testCase.getGeometryA();
    }
    if (argStr.equalsIgnoreCase("B")) {
      return this.testCase.getGeometryB();
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
    if (this.isRun) {
      return this.actualResult;
    }

    this.isRun = true;
    this.targetGeometry = this.geometryIndex.equalsIgnoreCase("A") ? this.testCase.getGeometryA()
      : this.testCase.getGeometryB();

    this.operationArgs = convertArgs(this.arguments);
    final GeometryOperation op = getGeometryOperation();
    this.actualResult = op.invoke(this.operation, this.targetGeometry, this.operationArgs);
    return this.actualResult;
  }

  public String getArgument(final int i) {
    return this.arguments.get(i);
  }

  public int getArgumentCount() {
    return this.arguments.size();
  }

  public Exception getException() {
    return this.exception;
  }

  public Result getExpectedResult() {
    return this.expectedResult;
  }

  public String getGeometryIndex() {
    return this.geometryIndex;
  }

  private GeometryOperation getGeometryOperation() {
    return this.testCase.getTestRun().getGeometryOperation();
  }

  public String getOperation() {
    return this.operation;
  }

  public TestCase getTestCase() {
    return this.testCase;
  }

  public String getTestDescription() {
    return this.testDescription;
  }

  public int getTestIndex() {
    return this.testIndex;
  }

  /**
   *  Returns whether the Test is passed.
   */
  public boolean isPassed() {
    return this.passed;
  }

  public boolean isRun() {
    return this.isRun;
  }

  public void removeArgument(final int i) {
    this.arguments.remove(i);
  }

  @Override
  protected void runTest() throws Throwable {
    try {
      this.passed = computePassed();
    } catch (final Throwable e) {
      System.err.println(this.testCase.getTestRun() + "\t" + this.testCase + "\t" + getName());
      e.printStackTrace();
      throw e;
    }
  }

  public void setArgument(final int i, final String value) {
    this.arguments.set(i, value);
  }

  public void setResult(final Result result) {
    this.expectedResult = result;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    map.put("type", "test");
    addToMap(map, "description", this.testDescription);

    map.put("propertyName", this.geometryIndex.toLowerCase());
    map.put("methodName", this.operation);
    final List<Object> arguments = new ArrayList<>();

    for (int i = 0; i < this.arguments.size(); i++) {
      final Object argument = this.arguments.get(i);
      if (argument instanceof String) {
        final String string = (String)argument;
        if ("a".equalsIgnoreCase(string)) {
          arguments.add(Collections.singletonMap("reference", "a"));
        } else if ("b".equalsIgnoreCase(string)) {
          arguments.add(Collections.singletonMap("reference", "b"));
        } else {
          arguments.add(argument);
        }

      } else {
        arguments.add(argument);
      }
    }
    addToMap(map, "arguments", arguments, Collections.emptyList());
    return map;
  }

  @Override
  public String toString() {
    return getName() + "\n";
  }

  public String toXml() {
    String xml = "";
    xml += "<test>" + StringUtil.newLine;
    if (this.testDescription != null && this.testDescription.length() > 0) {
      xml += "  <desc>" + StringUtil.escapeHTML(this.testDescription) + "</desc>"
        + StringUtil.newLine;
    }
    xml += "  <op name=\"" + this.operation + "\"";
    xml += " arg1=\"" + this.geometryIndex + "\"";
    int j = 2;
    for (final Object element : this.arguments) {
      final String argument = (String)element;
      Assert.assertTrue(argument != null);
      xml += " arg" + j + "=\"" + argument + "\"";
      j++;
    }

    xml += ">" + StringUtil.newLine;
    xml += StringUtil.indent(this.expectedResult.toFormattedString(), 4) + StringUtil.newLine;
    xml += "  </op>" + StringUtil.newLine;
    xml += "</test>" + StringUtil.newLine;
    return xml;
  }
}
