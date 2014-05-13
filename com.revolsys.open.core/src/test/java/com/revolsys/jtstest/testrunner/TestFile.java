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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.springframework.util.StringUtils;

import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jtstest.geomop.GeometryOperation;

/**
 * @version 1.7
 */
public class TestFile extends TestSuite implements MapSerializer {

  private String testDescription;

  private final GeometryFactory geometryFactory;

  private GeometryOperation geometryOperation = TestReader.GEOMETRY_FUNCTION_OPERATION;

  private ResultMatcher resultMatcher = TestReader.EQUALITY_RESULT_MATCHER;

  private final int runIndex;

  private final File file;

  private File workspace;

  private final TestDirectory parent;

  /**
   * 
   * @param testDescription
   * @param runIndex
   * @param precisionModel
   * @param geometryOperation a GeometryOperation to use for all tests in this run (may be null)
   * @param testFile
   */
  public TestFile(final TestDirectory parent, final String description,
    final int runIndex, final GeometryFactory geometryFactory,
    final GeometryOperation geomOp, final ResultMatcher resultMatcher,
    final File testFile) {
    this.parent = parent;
    this.runIndex = runIndex;
    if (StringUtils.hasText(description)) {
      this.testDescription = description.replaceAll("\\s+", " ");
    }
    setName(getId() + "." + FileUtil.getBaseName(testFile));
    this.geometryFactory = geometryFactory;
    this.geometryOperation = geomOp;
    this.resultMatcher = resultMatcher;
    this.file = testFile;
  }

  public File getFile() {
    return file;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public GeometryOperation getGeometryOperation() {
    return geometryOperation;
  }

  public String getId() {
    final String parentId = parent.getId();
    if (StringUtils.hasText(parentId)) {
      return parentId + "." + runIndex;
    } else {
      return String.valueOf(runIndex);
    }
  }

  public Map<String, Object> getProperties() {
    final Map<String, Object> properties = new LinkedHashMap<String, Object>();
    MapSerializerUtil.add(properties, "geometryFactory", geometryFactory);
    return properties;
  }

  public ResultMatcher getResultMatcher() {
    return resultMatcher;
  }

  public int getRunIndex() {
    return runIndex;
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
    return testDescription;
  }

  /**
   * @return null if no workspace set
   */
  public File getWorkspace() {
    return workspace;
  }

  public void setTestCaseIndexToRun(final int testCaseIndexToRun) {
  }

  public void setWorkspace(final File workspace) {
    this.workspace = workspace;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "test");
    MapSerializerUtil.add(map, "testDescription", testDescription);

    final Map<String, Object> properties = getProperties();
    MapSerializerUtil.add(map, "properties", properties, Collections.emptyMap());

    return map;
  }

  @Override
  public String toString() {
    return getName();
  }
}
