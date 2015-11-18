package com.revolsys.swing.parallel;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class CallableMaxThreadsSwingWorker<B> extends CallableSwingWorker<B>
  implements MaxThreadsSwingWorker {
  private final String key;

  private int maxThreads = 1;

  public CallableMaxThreadsSwingWorker(final String key, final int maxThreads,
    final String description, final Callable<B> backgroundTask, final Consumer<B> doneTask) {
    super(description, backgroundTask, doneTask);
    this.key = key;
    this.maxThreads = maxThreads;
  }

  @Override
  public int getMaxThreads() {
    return this.maxThreads;
  }

  @Override
  public String getWorkerKey() {
    return this.key;
  }
}
