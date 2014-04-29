package com.revolsys.jtstest.testrunner;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactoryRegistry;

public class TestDirectory extends TestSuite {

  public TestDirectory(final File directory, final String name) {
    super(name);
    final TestReader testReader = TopologyTest.getTestReader();
    int runIndex = 1;
    for (final File file : directory.listFiles()) {
      if (file.isDirectory()) {
        final Test test = new TestDirectory(file, file.getName());
        addTest(test);
      } else if (file.isFile()) {
        TestFile testRun;
        if (FileUtil.getFileNameExtension(file).equals("json")) {
          testRun = MapObjectFactoryRegistry.toObject(file);
        } else {
          testRun = testReader.createTestRun(file, runIndex++);
        }
        addTest(testRun);
      }
    }
  }

}
