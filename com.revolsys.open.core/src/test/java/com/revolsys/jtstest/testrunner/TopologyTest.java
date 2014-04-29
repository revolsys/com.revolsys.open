package com.revolsys.jtstest.testrunner;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.revolsys.jts.geom.GeometryFactory;

public class TopologyTest {

  private static final Date end = null;

  private static GeometryFactory geometryFactory = GeometryFactory.getFactory();

  private static final boolean running = false;

  private static final Date start = null;

  private static final TestReader testReader = new TestReader();

  private static List<TestFile> testRuns;

  public static void clearParsingProblems() {
    testReader.clearParsingProblems();
  }

  public static Date getEnd() {
    return end;
  }

  public static int getExceptionCount() {
    int exceptionCount = 0;
    for (final GeometryOperationTest test : getTests()) {
      if (test.getException() != null) {
        exceptionCount++;
      }
    }
    return exceptionCount;
  }

  public static int getFailedCount() {
    int failedCount = 0;
    for (final GeometryOperationTest test : getTests()) {
      if ((test.getException() == null) && (!test.isPassed())) {
        failedCount++;
      }
    }
    return failedCount;
  }

  public static GeometryFactory getGeometryFactory() {
    return TopologyTest.geometryFactory;
  }

  public static int getParseExceptionCount() {
    return testReader.getParsingProblems().size();
  }

  public static List getParsingProblems() {
    return Collections.unmodifiableList(testReader.getParsingProblems());
  }

  public static int getPassedCount() {
    int passedCount = 0;
    for (final GeometryOperationTest test : getTests()) {
      if (test.isPassed()) {
        passedCount++;
      }
    }
    return passedCount;
  }

  public static Date getStart() {
    return start;
  }

  public static int getTestCaseCount() {
    int count = 0;
    for (final TestFile testRun : testRuns) {
      count += testRun.getTestCases().size();
    }
    return count;
  }

  /**
   *  Returns the total number of tests.
   */
  public static int getTestCount() {
    int count = 0;
    for (final TestFile testRun : testRuns) {
      count += testRun.getTestCount();
    }
    return count;
  }

  public static TestReader getTestReader() {
    return testReader;
  }

  public static List<TestFile> getTestRuns() {
    return testRuns;
  }

  private static List<GeometryOperationTest> getTests() {
    final Vector<GeometryOperationTest> tests = new Vector<>();
    for (final TestFile testRun : testRuns) {
      tests.addAll(getTests(testRun));
    }
    return tests;
  }

  private static List<GeometryOperationTest> getTests(final TestFile testRun) {
    final Vector<GeometryOperationTest> tests = new Vector<>();
    for (final TestCase testCase : testRun.getTestCases()) {
      tests.addAll(testCase.getTests());
    }
    return tests;
  }

  /**
   *  Returns whether the TestEngine is running any TestCase's.
   */
  public static boolean isRunning() {
    return running;
  }

  public static void setGeometryFactory(final GeometryFactory geometryFactory) {
    TopologyTest.geometryFactory = geometryFactory;
  }

  public static junit.framework.Test suite() {
    return new TestDirectory(new File("src/test/testxml/"), "Topology Tests");
  }

  public TopologyTest() {
  }

  private String report() {
    final SimpleReportWriter reportWriter = new SimpleReportWriter(false);
    return reportWriter.writeReport(this);
  }

  public String summary() {
    String summary = "";
    if (getParseExceptionCount() > 0) {
      summary += getParseExceptionCount() + " parsing exceptions\n";
    }
    summary += getTestCaseCount() + " cases, " + getTestCount() + " tests\n";
    summary += getPassedCount() + " passed, " + getFailedCount() + " failed, "
      + getExceptionCount() + " threw exceptions";
    if (getParseExceptionCount() + getFailedCount() + getExceptionCount() > 0) {
      summary += "*******  ERRORS ENCOUNTERED  ********";
    }
    return summary;
  }

}
