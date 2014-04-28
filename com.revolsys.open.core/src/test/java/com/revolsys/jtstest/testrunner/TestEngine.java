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
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.jts.geom.GeometryFactory;

/**
 *  Converts test File's to TestCase's and runs them.
 *
 * @version 1.7
 */
public class TestEngine implements Runnable {
  public static GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public static void setGeometryFactory(final GeometryFactory geometryFactory) {
    TestEngine.geometryFactory = geometryFactory;
  }

  private List<File> testFiles = new ArrayList<>();

  // default is to run all tests
  private int testCaseIndexToRun = -1;

  private boolean running = false;

  private List<TestRun> testRuns = new Vector<>();

  private final TestReader testReader = new TestReader();

  private Date start = null;

  private Date end = null;

  static {
    MapObjectFactoryRegistry.addFactory(TestRun.FACTORY);
    MapObjectFactoryRegistry.addFactory(TestCase.FACTORY);
    MapObjectFactoryRegistry.addFactory(com.revolsys.jtstest.test.Test.FACTORY);
  }

  private static GeometryFactory geometryFactory = GeometryFactory.getFactory();

  /**
   *  Creates a TestEngine.
   */
  public TestEngine() {
  }

  public void addFile(final File file) {
    this.testFiles.add(file);
  }

  public void clearParsingProblems() {
    testReader.clearParsingProblems();
  }

  /**
   *  Creates TestRun's, one for each test File.
   */
  private List<TestRun> createTestRuns() {
    final Vector<TestRun> testRuns = new Vector<>();
    int runIndex = 0;
    for (final File testFile : testFiles) {
      runIndex++;
      final TestRun testRun;
      if (FileUtil.getFileNameExtension(testFile).equals("json")) {
        testRun = MapObjectFactoryRegistry.toObject(testFile);
      } else {
        testRun = testReader.createTestRun(testFile, runIndex);
      }
      if (testRun != null) {
        testRuns.add(testRun);
      }
    }
    return testRuns;
  }

  public Date getEnd() {
    return end;
  }

  public int getExceptionCount() {
    int exceptionCount = 0;
    for (final Test test : getTests()) {
      if (test.getException() != null) {
        exceptionCount++;
      }
    }
    return exceptionCount;
  }

  public int getFailedCount() {
    int failedCount = 0;
    for (final Test test : getTests()) {
      if ((test.getException() == null) && (!test.isPassed())) {
        failedCount++;
      }
    }
    return failedCount;
  }

  public int getParseExceptionCount() {
    return testReader.getParsingProblems().size();
  }

  public List getParsingProblems() {
    return Collections.unmodifiableList(testReader.getParsingProblems());
  }

  public int getPassedCount() {
    int passedCount = 0;
    for (final Test test : getTests()) {
      if (test.isPassed()) {
        passedCount++;
      }
    }
    return passedCount;
  }

  public Date getStart() {
    return start;
  }

  public int getTestCaseCount() {
    int count = 0;
    for (final TestRun testRun : testRuns) {
      count += testRun.getTestCases().size();
    }
    return count;
  }

  /**
   *  Returns the total number of tests.
   */
  public int getTestCount() {
    int count = 0;
    for (final TestRun testRun : testRuns) {
      count += testRun.getTestCount();
    }
    return count;
  }

  public List<TestRun> getTestRuns() {
    return testRuns;
  }

  private List<Test> getTests() {
    final Vector<Test> tests = new Vector<Test>();
    for (final TestRun testRun : testRuns) {
      tests.addAll(getTests(testRun));
    }
    return tests;
  }

  private List<Test> getTests(final TestRun testRun) {
    final Vector<Test> tests = new Vector<Test>();
    for (final TestCase testCase : testRun.getTestCases()) {
      tests.addAll(testCase.getTests());
    }
    return tests;
  }

  /**
   *  Returns whether the TestEngine is running any TestCase's.
   */
  public boolean isRunning() {
    return running;
  }

  @Override
  public void run() {
    running = true;
    start = new Date();
    clearParsingProblems();
    testRuns = createTestRuns();
    for (final TestRun testRun : testRuns) {
      if (testCaseIndexToRun >= 0) {
        testRun.setTestCaseIndexToRun(testCaseIndexToRun);
      }
      testRun.run();

      // final File file = FileUtil.getFile(testRun.getTestFile());
      // final String fileExtension = FileUtil.getFileNameExtension(file);
      // if (fileExtension.equals("xml")) {
      // final File directory = FileUtil.getDirectory(file.toString()
      // .replaceAll("testxml", "json")
      // .replaceAll(".xml", ""));
      // for (final TestCase testCase : testRun.getTestCases()) {
      // final File newFile = new File(directory, testCase.getCaseIndex()
      // + ".json");
      // JsonMapIoFactory.write(testCase.toMap(), newFile);
      // final com.revolsys.jtstest.test.Test test =
      // MapObjectFactoryRegistry.toObject(newFile);
      // System.out.println(test);
      //
      // }
      // }
    }
    end = new Date();
    running = false;
  }

  public void setTestCaseIndexToRun(final int testCaseIndexToRun) {
    this.testCaseIndexToRun = testCaseIndexToRun;
  }

  /**
   *  Sets the File's that contain the tests.
   */
  public void setTestFiles(final List<File> testFiles) {
    this.testFiles = testFiles;
  }
}
