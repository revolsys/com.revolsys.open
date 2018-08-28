package com.revolsys.core.test.geometry.test.old.perf;

/**
 * Base class for test cases which depend on threading.
 * A common example of usage is to test for race conditions.
 *
 * @author Martin Davis
 *
 */
public abstract class ThreadTestCase {
  public abstract Runnable getRunnable(int threadIndex);

  public int getThreadCount() {
    return ThreadTestRunner.DEFAULT_THREAD_COUNT;

  }

  public abstract void setup();

}
