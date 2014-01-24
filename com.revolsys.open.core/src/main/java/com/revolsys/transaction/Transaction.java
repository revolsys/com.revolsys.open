package com.revolsys.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class Transaction implements AutoCloseable {

  private final PlatformTransactionManager transactionManager;

  private final DefaultTransactionStatus transactionStatus;

  public Transaction(final PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
    if (transactionManager == null) {
      this.transactionStatus = null;
    } else {
      this.transactionStatus = TransactionUtils.createNewTransaction(transactionManager);
    }
  }

  public Transaction(final PlatformTransactionManager transactionManager,
    final DefaultTransactionStatus transactionStatus) {
    this.transactionManager = transactionManager;
    if (transactionManager == null) {
      this.transactionStatus = null;
    } else {
      this.transactionStatus = transactionStatus;
    }
  }

  public Transaction(final PlatformTransactionManager transactionManager,
    final Propagation propagation) {
    this(transactionManager, TransactionUtils.createTransaction(
      transactionManager, propagation));
  }

  @Override
  public void close() throws RuntimeException {
    commit();
  }

  protected void commit() {
    if (transactionManager != null && transactionStatus != null) {
      if (!transactionStatus.isCompleted()) {
        if (transactionStatus.isRollbackOnly()) {
          rollback();
        } else {
          transactionManager.commit(transactionStatus);
        }
      }
    }
  }

  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public DefaultTransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  protected void rollback() {
    if (transactionManager != null && transactionStatus != null) {
      transactionManager.rollback(transactionStatus);
    }
  }

  public void setRollbackOnly() {
    if (transactionStatus != null) {
      transactionStatus.setRollbackOnly();
    }
  }
}
