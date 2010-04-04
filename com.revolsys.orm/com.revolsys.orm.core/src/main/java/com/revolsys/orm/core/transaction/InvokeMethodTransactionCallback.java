package com.revolsys.orm.core.transaction;

import org.apache.commons.beanutils.MethodUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodTransactionCallback implements TransactionCallback {

  private Object[] args;

  private String methodName;

  private Object object;

  private boolean rollback;

  public InvokeMethodTransactionCallback(final Object object,
    final String methodName, final Object... args) {
    this(object, methodName, true, args);
  }

  /**
   * @param object
   * @param methodName
   * @param rollback
   * @param args
   */
  public InvokeMethodTransactionCallback(final Object object,
    final String methodName, final boolean rollback, final Object... args) {
    this.object = object;
    this.methodName = methodName;
    this.args = args;
    this.rollback = rollback;
  }

  public Object doInTransaction(final TransactionStatus transaction) {
    try {
      Object result = MethodUtils.invokeMethod(object, methodName, args);
      if (rollback) {
        transaction.setRollbackOnly();
      }
      return result;
    } catch (Throwable e) {
      transaction.setRollbackOnly();
      return ExceptionUtil.throwUncheckedException(e);
    }
  }
}
