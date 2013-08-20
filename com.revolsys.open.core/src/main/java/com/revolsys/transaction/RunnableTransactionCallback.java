package com.revolsys.transaction;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.revolsys.util.ExceptionUtil;

public class RunnableTransactionCallback implements TransactionCallback<Void> {

  private final Runnable runnable;

  private final boolean rollback;

  public RunnableTransactionCallback(final Runnable runnable) {
    this(runnable, false);
  }

  public RunnableTransactionCallback(final Runnable runnable,
    final boolean rollback) {
    this.runnable = runnable;
    this.rollback = rollback;
  }

  @Override
  public Void doInTransaction(final TransactionStatus transaction) {
    try {
      runnable.run();
      if (transaction != null) {
        if (rollback) {
          transaction.setRollbackOnly();
        }
      }
      return null;
    } catch (final Throwable e) {
      if (transaction != null) {
        transaction.setRollbackOnly();
      }
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public String toString() {
    return runnable.toString();
  }
}
