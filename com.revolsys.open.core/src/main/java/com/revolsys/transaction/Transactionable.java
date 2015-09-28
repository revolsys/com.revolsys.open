package com.revolsys.transaction;

import org.springframework.transaction.PlatformTransactionManager;

public interface Transactionable {
  PlatformTransactionManager getTransactionManager();

  /**
   * Construct a new {@link Transaction} with {@link Propagation#REQUIRES_NEW}.
   * @return The transaction.
   */
  default Transaction newTransaction() {
    final PlatformTransactionManager transactionManager = getTransactionManager();
    return new Transaction(transactionManager, Propagation.REQUIRES_NEW);
  }

  /**
   * Construct a new {@link Transaction} with the specified {@link Propagation}.
   *
   * @param propagation The transaction propagation.
   * @return The transaction.
   */
  default Transaction newTransaction(final Propagation propagation) {
    final PlatformTransactionManager transactionManager = getTransactionManager();
    return new Transaction(transactionManager, propagation);
  }
}
