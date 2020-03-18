package com.revolsys.swing.parallel;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SupplierConsumerMaxThreadsSwingWorker<B> extends SupplierConsumerSwingWorker<B>
  implements MaxThreadsSwingWorker {
  private final String key;

  private int maxThreads = 1;

  public SupplierConsumerMaxThreadsSwingWorker(final String key, final int maxThreads,
    final String description, final Supplier<B> backgroundTask, final Consumer<B> doneTask) {
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
