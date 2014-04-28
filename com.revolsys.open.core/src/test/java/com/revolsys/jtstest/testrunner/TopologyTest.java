package com.revolsys.jtstest.testrunner;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TopologyTest {

  private final TestEngine engine = new TestEngine();

  public TopologyTest() {
    addFiles(Arrays.asList("src/test/testxml/"));
  }

  public void addFile(final File file) {
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      if (files != null) {
        for (final File childFile : files) {
          addFile(childFile);
        }
      }
    } else if (file.exists()) {
      this.engine.addFile(file);
    }
  }

  public void addFiles(final List<String> testFiles) {
    for (final String fileName : testFiles) {
      final File file = new File(fileName);
      addFile(file);
    }
  }

  public String oneLineSummary() {
    return StringUtil.replaceAll(summary(), "\n", "; ");
  }

  private String report() {
    final SimpleReportWriter reportWriter = new SimpleReportWriter(false);
    return reportWriter.writeReport(engine);
  }

  public String summary() {
    String summary = "";
    if (engine.getParseExceptionCount() > 0) {
      summary += engine.getParseExceptionCount() + " parsing exceptions\n";
    }
    summary += engine.getTestCaseCount() + " cases, " + engine.getTestCount()
      + " tests\n";
    summary += engine.getPassedCount() + " passed, " + engine.getFailedCount()
      + " failed, " + engine.getExceptionCount() + " threw exceptions";
    if (engine.getParseExceptionCount() + engine.getFailedCount()
      + engine.getExceptionCount() > 0) {
      summary += "*******  ERRORS ENCOUNTERED  ********";
    }
    return summary;
  }

  @Test
  public void test() {
    engine.run();
    System.out.println(report());
  }
}
