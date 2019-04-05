package com.revolsys.transaction;

import org.jeometry.common.exception.Exceptions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.io.BaseCloseable;

public class Transaction implements BaseCloseable {

  private static ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();

  public static void afterCommit(final Runnable runnable) {
    if (runnable != null) {
      if (TransactionSynchronizationManager.isSynchronizationActive()) {
        final RunnableAfterCommit synchronization = new RunnableAfterCommit(runnable);
        TransactionSynchronizationManager.registerSynchronization(synchronization);
      } else {
        runnable.run();
      }
    }
  }

  public static Transaction getCurrentTransaction() {
    return currentTransaction.get();
  }

  public static boolean isHasCurrentTransaction() {
    return getCurrentTransaction() != null;
  }

  public static Runnable runnable(final Runnable runnable,
    final PlatformTransactionManager transactionManager, final Propagation propagation) {
    if (transactionManager == null || propagation == null) {
      return runnable;
    } else {
      final DefaultTransactionDefinition transactionDefinition = Transaction
        .transactionDefinition(propagation);
      return new TransactionRunnable(transactionManager, transactionDefinition, runnable);
    }
  }

  public static void setCurrentRollbackOnly() {
    final Transaction transaction = currentTransaction.get();
    if (transaction != null) {
      transaction.setRollbackOnly();
    }
  }

  public static DefaultTransactionDefinition transactionDefinition(final Propagation propagation) {
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
    final PlatformTransactionManager transactionManager, final Propagation propagation) {
    if (propagation == null || transactionManager == null) {
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
    this(transactionManager, Transaction.transactionStatus(transactionManager, propagation));
  }

  public Transaction(final PlatformTransactionManager transactionManager,
    final TransactionDefinition transactionDefinition) {
    this(transactionManager, transactionStatus(transactionManager, transactionDefinition));
  }

  @Override
  public void close() throws RuntimeException {
    commit();
    currentTransaction.set(this.previousTransaction);
    this.transactionManager = null;
    this.previousTransaction = null;
    this.transactionStatus = null;
  }

  protected void commit() {
    if (this.transactionManager != null && this.transactionStatus != null) {
      if (!this.transactionStatus.isCompleted()) {
        if (this.transactionStatus.isRollbackOnly()) {
          rollback();
        } else {
          try {
            this.transactionManager.commit(this.transactionStatus);
          } catch (final Throwable e) {
            Exceptions.throwUncheckedException(e);
          }
        }
      }
    }
  }

  public PlatformTransactionManager getTransactionManager() {
    return this.transactionManager;
  }

  public DefaultTransactionStatus getTransactionStatus() {
    return this.transactionStatus;
  }

  public boolean isCompleted() {
    if (this.transactionStatus == null) {
      return true;
    } else {
      return this.transactionStatus.isCompleted();
    }
  }

  public boolean isRollbackOnly() {
    return this.transactionStatus.isRollbackOnly();
  }

  protected void rollback() {
    if (this.transactionManager != null && this.transactionStatus != null) {
      this.transactionManager.rollback(this.transactionStatus);
    }
  }

  public void setRollbackOnly() {
    if (this.transactionStatus != null) {
      this.transactionStatus.setRollbackOnly();
    }
  }

  public RuntimeException setRollbackOnly(final Throwable e) {
    setRollbackOnly();
    return Exceptions.throwUncheckedException(e);
  }
}
