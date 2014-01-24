package com.revolsys.transaction;

import org.springframework.transaction.TransactionDefinition;

public enum Propagation {
  /** */
  REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED), /** */
  SUPPORTS(TransactionDefinition.PROPAGATION_SUPPORTS), /** */
  MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY), /** */
  REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW), /** */
  NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED), /** */
  NEVER(TransactionDefinition.PROPAGATION_NEVER), /** */
  NESTED(TransactionDefinition.PROPAGATION_NESTED);

  private final int value;

  Propagation(final int value) {
    this.value = value;
  }

  public int value() {
    return this.value;
  }

}
