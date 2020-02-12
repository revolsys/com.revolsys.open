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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.revolsys.core.test.geometry.test.geomop.GeometryOperation;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.util.Property;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @version 1.7
 */
public class TestFile extends TestSuite implements MapSerializer {

  private final File file;

  private final GeometryFactory geometryFactory;

  private GeometryOperation geometryOperation = TestReader.GEOMETRY_FUNCTION_OPERATION;

  private final TestDirectory parent;

  private ResultMatcher resultMatcher = TestReader.EQUALITY_RESULT_MATCHER;

  private final int runIndex;

  private String testDescription;

  private File workspace;

  /**
   *
   * @param testDescription
   * @param runIndex
   * @param precisionModel
   * @param geometryOperation a GeometryOperation to use for all tests in this run (may be null)
   * @param testFile
   */
  public TestFile(final TestDirectory parent, final String description, final int runIndex,
    final GeometryFactory geometryFactory, final GeometryOperation geomOp,
    final ResultMatcher resultMatcher, final File testFile) {
    this.parent = parent;
    this.runIndex = runIndex;
    if (Property.hasValue(description)) {
      this.testDescription = description.replaceAll("\\s+", " ");
    }
    setName(getId() + "." + FileUtil.getBaseName(testFile));
    this.geometryFactory = geometryFactory;
    this.geometryOperation = geomOp;
    this.resultMatcher = resultMatcher;
    this.file = testFile;
  }

  public File getFile() {
    return this.file;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public GeometryOperation getGeometryOperation() {
    return this.geometryOperation;
  }

  public String getId() {
    final String parentId = this.parent.getId();
    if (Property.hasValue(parentId)) {
      return parentId + "." + this.runIndex;
    } else {
      return String.valueOf(this.runIndex);
    }
  }

  public Map<String, Object> getProperties() {
    final JsonObject properties = JsonObject.hash();
    addToMap(properties, "geometryFactory", this.geometryFactory);
    return properties;
  }

  public ResultMatcher getResultMatcher() {
    return this.resultMatcher;
  }

  public int getRunIndex() {
    return this.runIndex;
  }

  public List<TestCase> getTestCases() {
    final List<TestCase> testList = new ArrayList<>();
    final Enumeration<Test> tests = tests();
    while (tests.hasMoreElements()) {
      testList.add((TestCase)tests.nextElement());
    }
    return testList;
  }

  public int getTestCount() {
    return testCount();
  }

  public String getTestDescription() {
    return this.testDescription;
  }

  /**
   * @return null if no workspace set
   */
  public File getWorkspace() {
    return this.workspace;
  }

  public void setTestCaseIndexToRun(final int testCaseIndexToRun) {
  }

  public void setWorkspace(final File workspace) {
    this.workspace = workspace;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    map.put("type", "test");
    addToMap(map, "testDescription", this.testDescription);

    final Map<String, Object> properties = getProperties();
    addToMap(map, "properties", properties, Collections.emptyMap());

    return map;
  }

  @Override
  public String toString() {
    return getName();
  }
}
