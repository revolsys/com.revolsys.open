package com.revolsys.orm.core.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AbstractTransactionCallbackRunnable extends
  DefaultTransactionDefinition implements Runnable, TransactionCallback<Void> {
  /**
   * 
   */
  private static final long serialVersionUID = -2139776082029553417L;
  private final PlatformTransactionManager transactionManager;

  public AbstractTransactionCallbackRunnable(
    final PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
    setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
  }

  public void run() {
    final TransactionTemplate transactionTemplate = new TransactionTemplate(
      transactionManager, this);
    transactionTemplate.execute(this);
  }

}
