package com.revolsys.swing.parallel;

/**
 * Indicate that instances of this class should only be executed in a maximum number of threads.
 * Any others will be blocked.
 */
public interface MaxThreadsSwingWorker {
  default int getMaxThreads() {
    return 1;
  }

  default String getWorkerKey() {
    return getClass().getName();
  }
}
