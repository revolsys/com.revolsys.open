package com.revolsys.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.parallel.process.InvokeMethodRunnable;

public class InvokeMethodAfterCommit extends TransactionSynchronizationAdapter {
  private static final Logger LOG = LoggerFactory.getLogger(InvokeMethodAfterCommit.class);

  public static <V> void invoke(final Object object, final String methodName,
    final Object... args) {
    if (object != null) {
      final InvokeMethodAfterCommit synchronization = new InvokeMethodAfterCommit(
        object, methodName, args);
      if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(synchronization);
      } else {
        synchronization.afterCommit();
      }
    }
  }

  private final Runnable runnable;

  public InvokeMethodAfterCommit(final Class<?> clazz, final String methodName,
    final Object... args) {
    runnable = new InvokeMethodRunnable(clazz, methodName, args);
  }

  public InvokeMethodAfterCommit(final Object object, final String methodName,
    final Object... args) {
    runnable = new InvokeMethodRunnable(object, methodName, args);
  }

  @Override
  public void afterCommit() {
    try {
      runnable.run();
    } catch (final Throwable e) {
      LOG.error("Error invoking " + runnable, e);
    }
  }
}
