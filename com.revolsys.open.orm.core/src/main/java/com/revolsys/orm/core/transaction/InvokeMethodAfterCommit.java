package com.revolsys.orm.core.transaction;

import java.util.Arrays;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class InvokeMethodAfterCommit extends TransactionSynchronizationAdapter {
  public static <V> void invoke(
    final Object object,
    final String methodName,
    final Object... args) {
    InvokeMethodAfterCommit synchronization = new InvokeMethodAfterCommit(
      object, methodName, args);
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(synchronization);
    } else {
      synchronization.afterCommit();
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(InvokeMethodAfterCommit.class);

  private final Object[] args;

  private final String methodName;

  private final Object object;

  public InvokeMethodAfterCommit(final Object object, final String methodName,
    final Object... args) {
    this.object = object;
    this.methodName = methodName;
    this.args = args;

  }

  @Override
  public void afterCommit() {
    try {
      MethodUtils.invokeMethod(object, methodName, args);
    } catch (final Throwable e) {
      LOG.error("Error invoking " + object.getClass() + "." + methodName
        + Arrays.toString(args));
    }
  }
}
