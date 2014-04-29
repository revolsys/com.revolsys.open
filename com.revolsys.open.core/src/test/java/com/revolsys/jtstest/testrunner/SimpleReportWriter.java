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
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.revolsys.util.ExceptionUtil;

/**
 * @version 1.7
 */
public class SimpleReportWriter implements ReportWriter {

  private final boolean verbose;

  private StringWriter reportBuf;

  public SimpleReportWriter(final boolean verbose) {
    this.verbose = verbose;
  }

  private boolean areAllTestsPassed(final TestCase testCase) {
    for (final Iterator i = testCase.getTests().iterator(); i.hasNext();) {
      final GeometryOperationTest test = (GeometryOperationTest)i.next();
      if (!test.isPassed()) {
        return false;
      }
    }
    return true;
  }

  private void reportOnParsingProblems(final List parsingProblems) {
    if (parsingProblems.isEmpty()) {
      return;
    }
    reportBuf.write("\n");
    for (final Iterator i = parsingProblems.iterator(); i.hasNext();) {
      final String parsingProblem = (String)i.next();
      reportBuf.write(parsingProblem);
      reportBuf.write("\n");
    }
  }

  public void reportOnTest(final GeometryOperationTest test) {
    String id = test.getGeometryIndex() + " " + test.getOperation();
    for (int i = 0; i < test.getArgumentCount(); i++) {
      id += " " + test.getArgument(i);
    }
    if (test.getExpectedResult() instanceof BooleanResult) {
      id += ", " + test.getExpectedResult().toShortString();
    }
    if (test.getTestDescription().length() > 0) {
      id += ", " + test.getTestDescription();
    }
    if (test.getException() != null) {
      reportBuf.write("Test Threw Exception ("
        + id
        + ")"
        + "     "
        + (verbose ? StringUtil.getStackTrace(test.getException())
          : test.getException().toString()) + "\n");
    } else if (test.isPassed() && verbose) {
      reportBuf.write("  Test Passed (" + id + ")" + "\n");
    } else if (!test.isPassed()) {
      reportBuf.write("  Test Failed (" + id + ")" + "\n");
      reportBuf.write("    Expected: "
        + test.getExpectedResult().toFormattedString() + "\n");
      try {
        reportBuf.write("    Actual:   "
          + test.getActualResult().toFormattedString() + "\n");
      } catch (final Exception e) {
        ExceptionUtil.throwUncheckedException(e);
      }
    }
  }

  private void reportOnTestCase(final TestCase testCase) {
    if (verbose || !areAllTestsPassed(testCase)) {
      reportBuf.write("\n");
      final File file = testCase.getTestRun().getFile();
      reportBuf.write(file.toString());
      reportBuf.write("\n");
      final String description = testCase.getTestDescription();
      if (description.length() > 0) {
        reportBuf.write("  " + description);
        reportBuf.write("\n");
      }
      reportOnTests(testCase.getTests());
    }
  }

  private void reportOnTestCases(final List testCases) {
    for (final Iterator i = testCases.iterator(); i.hasNext();) {
      final TestCase testCase = (TestCase)i.next();
      if (testCase.isRun()) {
        reportOnTestCase(testCase);
      }
    }
  }

  private void reportOnTestRun(final TestFile testRun) {
    reportOnTestCases(testRun.getTestCases());
  }

  private void reportOnTestRuns(final List testRuns) {
    for (final Iterator i = testRuns.iterator(); i.hasNext();) {
      final TestFile testRun = (TestFile)i.next();
      reportOnTestRun(testRun);
    }
  }

  private void reportOnTests(final List<GeometryOperationTest> tests) {
    for (final GeometryOperationTest test : tests) {
      reportOnTest(test);
    }
  }

  public void reportOnTime(final Date start, final Date end) {
    final long elapsedTime = end.getTime() - start.getTime();
    final long days = elapsedTime / (24 * 60 * 60 * 1000);
    final long hours = (elapsedTime - (days * 24 * 60 * 60 * 1000))
      / (60 * 60 * 1000);
    final long minutes = (elapsedTime - (hours * 60 * 60 * 1000)) / (60 * 1000);
    final double seconds = (elapsedTime - (minutes * 60 * 1000)) / (1000d);
    String message = "Elapsed time: ";
    message += days > 0 ? days + " days, " : "";
    message += hours > 0 ? hours + " hours, " : "";
    message += minutes > 0 ? minutes + " minutes, " : "";
    message += seconds > 0 ? seconds + " seconds" : "";
    reportBuf.write(message);
  }

  public void reportSummary(final TopologyTest engine) {
    if (engine.getParseExceptionCount() > 0) {
      reportBuf.write(engine.getParseExceptionCount() + " parsing exceptions\n");
    }
    reportBuf.write(engine.getTestCaseCount() + " cases with "
      + engine.getTestCount() + " tests" + "  --  ");
    reportBuf.write(engine.getPassedCount() + " passed, "
      + engine.getFailedCount() + " failed, " + engine.getExceptionCount()
      + " exceptions");
    if (engine.getParseExceptionCount() + engine.getFailedCount()
      + engine.getExceptionCount() > 0) {
      reportBuf.write("\n\n*******  ERRORS ENCOUNTERED IN RUN  ********\n");
    }
  }

  public String write(final GeometryOperationTest test) {
    reportBuf = new StringWriter();
    reportOnTest(test);
    return reportBuf.toString();
  }

  @Override
  public String writeReport(final TopologyTest engine) {
    reportBuf = new StringWriter();
    reportOnParsingProblems(engine.getParsingProblems());
    reportOnTestRuns(engine.getTestRuns());
    reportBuf.write("\n\n");
    reportSummary(engine);
    reportBuf.write("\n");
    reportOnTime(engine.getStart(), engine.getEnd());
    return reportBuf.toString();
  }
}
