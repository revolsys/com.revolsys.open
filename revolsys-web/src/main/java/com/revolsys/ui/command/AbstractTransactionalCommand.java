package com.revolsys.ui.command;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.PlatformTransactionManager;

import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;

public abstract class AbstractTransactionalCommand implements Runnable {
  private final BeanFactory beanFactory;

  public AbstractTransactionalCommand(final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public BeanFactory getBeanFactory() {
    return this.beanFactory;
  }

  public void runInTransaction() {
    final PlatformTransactionManager transactionManager = (PlatformTransactionManager)this.beanFactory
      .getBean("transactionManager");
    try (
      Transaction transaction = new Transaction(transactionManager, Propagation.REQUIRES_NEW)) {
      try {
        run();
      } catch (final RuntimeException e) {
        throw transaction.setRollbackOnly(e);
      }
    }
  }
}
