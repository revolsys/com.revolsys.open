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
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jtstest.geomop.GeometryOperation;

/**
 * @version 1.7
 */
public class TestRun implements Runnable {
  // default is to run all cases
  private int testCaseIndexToRun = -1;

  private final String description;

  private final List testCases = new ArrayList();

  private final GeometryFactory geometryFactory;

  private GeometryOperation geomOp = null;

  private ResultMatcher resultMatcher = null;

  private final int runIndex;

  private final File testFile;

  private File workspace;

  /**
   * 
   * @param description
   * @param runIndex
   * @param precisionModel
   * @param geomOp a GeometryOperation to use for all tests in this run (may be null)
   * @param testFile
   */
  public TestRun(final String description, final int runIndex,
    final GeometryFactory geometryFactory, final GeometryOperation geomOp,
    final ResultMatcher resultMatcher, final File testFile) {
    this.description = description;
    this.runIndex = runIndex;
    this.geometryFactory = geometryFactory;
    this.geomOp = geomOp;
    this.resultMatcher = resultMatcher;
    this.testFile = testFile;
  }

  public void addTestCase(final TestCase testCase) {
    testCases.add(testCase);
  }

  public String getDescription() {
    return description;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public GeometryOperation getGeometryOperation() {
    // use the main one if it was user-specified or this run does not have an op
    // specified
    if (TopologyTestApp.isGeometryOperationSpecified() || geomOp == null) {
      return TopologyTestApp.getGeometryOperation();
    }

    return geomOp;
  }

  public ResultMatcher getResultMatcher() {
    // use the main one if it was user-specified or this run does not have an op
    // specified
    if (TopologyTestApp.isResultMatcherSpecified() || resultMatcher == null) {
      return TopologyTestApp.getResultMatcher();
    }

    return resultMatcher;
  }

  public int getRunIndex() {
    return runIndex;
  }

  public List getTestCases() {
    return Collections.unmodifiableList(testCases);
  }

  public int getTestCount() {
    int count = 0;
    for (final Iterator i = testCases.iterator(); i.hasNext();) {
      final TestCase testCase = (TestCase)i.next();
      count += testCase.getTestCount();
    }
    return count;
  }

  public File getTestFile() {
    return testFile;
  }

  /**
   * @return null if no workspace set
   */
  public File getWorkspace() {
    return workspace;
  }

  @Override
  public void run() {
    for (final Iterator j = testCases.iterator(); j.hasNext();) {
      final TestCase testCase = (TestCase)j.next();
      if (testCaseIndexToRun < 0
        || testCase.getCaseIndex() == testCaseIndexToRun) {
        testCase.run();
      }
    }
  }

  public void setTestCaseIndexToRun(final int testCaseIndexToRun) {
    this.testCaseIndexToRun = testCaseIndexToRun;
  }

  public void setWorkspace(final File workspace) {
    this.workspace = workspace;
  }

}
