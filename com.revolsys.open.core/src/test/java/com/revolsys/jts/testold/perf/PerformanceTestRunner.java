package com.revolsys.jts.testold.perf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.util.Stopwatch;

/**
 * Runs {@link PerformanceTestCase} classes which contain performance tests.
 * 
 * 
 * 
 * @author Martin Davis
 *
 */
public class PerformanceTestRunner {
  private static final String RUN_PREFIX = "run";

  private static Method[] findMethods(final Class clz, final String methodPrefix) {
    final List runMeths = new ArrayList();
    final Method meth[] = clz.getDeclaredMethods();
    for (final Method element : meth) {
      if (element.getName().startsWith(RUN_PREFIX)) {
        runMeths.add(element);
      }
    }
    return (Method[])runMeths.toArray(new Method[0]);
  }

  public static void run(final Class clz) {
    final PerformanceTestRunner runner = new PerformanceTestRunner();
    runner.runInternal(clz);
  }

  private PerformanceTestRunner() {

  }

  private void runInternal(final Class clz) {
    try {
      final Constructor ctor = clz.getConstructor(String.class);
      final PerformanceTestCase test = (PerformanceTestCase)ctor.newInstance("Name");
      final int[] runSize = test.getRunSize();
      final int runIter = test.getRunIterations();
      final Method[] runMethod = findMethods(clz, RUN_PREFIX);

      // do the run
      test.setUp();
      for (final int size : runSize) {

        test.startRun(size);
        for (final Method element : runMethod) {
          final Stopwatch sw = new Stopwatch();
          for (int iter = 0; iter < runIter; iter++) {
            element.invoke(test);
          }
          System.out.println(element.getName() + " : " + sw.getTimeString());
        }
        test.endRun();
      }
      test.tearDown();
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
