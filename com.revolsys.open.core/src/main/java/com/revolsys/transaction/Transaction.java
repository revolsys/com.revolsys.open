package com.revolsys.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.revolsys.util.ExceptionUtil;

public class Transaction implements AutoCloseable {

  private static ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();

  public static Transaction getCurrentTransaction() {
    return currentTransaction.get();
  }

  public static Runnable runnable(final Runnable runnable,
    final PlatformTransactionManager transactionManager,
    final Propagation propagation) {
    if (transactionManager == null || propagation == null) {
      return runnable;
    } else {
      final DefaultTransactionDefinition transactionDefinition = Transaction.transactionDefinition(propagation);
      return new TransactionRunnable(transactionManager, transactionDefinition,
        runnable);
    }
  }

  public static void setCurrentRollbackOnly() {
    final Transaction transaction = currentTransaction.get();
    if (transaction != null) {
      transaction.setRollbackOnly();
    }
  }

  public static DefaultTransactionDefinition transactionDefinition(
    final Propagation propagation) {
    if (propagation == null) {
      return null;
    } else {
      final int propagationValue = propagation.value();
      final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(
        propagationValue);
      return transactionDefinition;
    }
  }

  public static DefaultTransactionStatus transactionStatus(
    final PlatformTransactionManager transactionManager,
    final Propagation propagation) {
    if (propagation == null) {
      return null;
    } else {
      final DefaultTransactionDefinition transactionDefinition = transactionDefinition(propagation);
      return transactionStatus(transactionManager, transactionDefinition);
    }
  }

  public static DefaultTransactionStatus transactionStatus(
    final PlatformTransactionManager transactionManager,
    final TransactionDefinition transactionDefinition) {
    if (transactionManager == null || transactionDefinition == null) {
      return null;
    } else {
      return (DefaultTransactionStatus)transactionManager.getTransaction(transactionDefinition);
    }
  }

  private Transaction previousTransaction;

  private PlatformTransactionManager transactionManager;

  private DefaultTransactionStatus transactionStatus;

  public Transaction(final PlatformTransactionManager transactionManager) {
    this(transactionManager, Propagation.REQUIRES_NEW);
  }

  public Transaction(final PlatformTransactionManager transactionManager,
    final DefaultTransactionStatus transactionStatus) {
    this.transactionManager = transactionManager;
    if (transactionManager == null) {
      this.transactionStatus = null;
    } else {
      this.transactionStatus = transactionStatus;
    }
    this.previousTransaction = getCurrentTransaction();
    currentTransaction.set(this);
  }

  public Transaction(final PlatformTransactionManager transactionManager,
    final Propagation propagation) {
    this(transactionManager, Transaction.transactionStatus(transactionManager,
      propagation));
  }

  public Transaction(final PlatformTransactionManager transactionManager,
    final TransactionDefinition transactionDefinition) {
    this(transactionManager, transactionStatus(transactionManager,
      transactionDefinition));
  }

  @Override
  public void close() throws RuntimeException {
    commit();
    currentTransaction.set(previousTransaction);
    this.transactionManager = null;
    this.previousTransaction = null;
    this.transactionStatus = null;
  }

  protected void commit() {
    if (transactionManager != null && transactionStatus != null) {
      if (!transactionStatus.isCompleted()) {
        if (transactionStatus.isRollbackOnly()) {
          rollback();
        } else {
          try {
            transactionManager.commit(transactionStatus);
          } catch (final Throwable e) {
            e.printStackTrace();
            ExceptionUtil.throwUncheckedException(e);
          }
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

  public RuntimeException setRollbackOnly(final Throwable e) {
    setRollbackOnly();
    return ExceptionUtil.throwUncheckedException(e);
  }
}
