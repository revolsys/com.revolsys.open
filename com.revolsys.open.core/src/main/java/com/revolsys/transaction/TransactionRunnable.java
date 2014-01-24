package com.revolsys.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

public class TransactionRunnable implements Runnable {

  private final PlatformTransactionManager transactionManager;

  private final TransactionDefinition transactionDefinition;

  private final Runnable runnable;

  public TransactionRunnable(
    final PlatformTransactionManager transactionManager,
    final TransactionDefinition transactionDefinition, final Runnable runnable) {
    this.transactionManager = transactionManager;
    this.transactionDefinition = transactionDefinition;
    this.runnable = runnable;
  }

  @Override
  public void run() {
    try (
      Transaction transaction = new Transaction(transactionManager,
        transactionDefinition)) {
      try {
        runnable.run();
      } catch (final Throwable e) {
        throw transaction.setRollbackOnly(e);
      }
    }
  }
}
