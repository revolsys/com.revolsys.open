package com.revolsys.transaction;

import java.lang.reflect.Method;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.revolsys.beans.MethodInvoker;
import com.revolsys.util.ExceptionUtil;

public class MethodTransactionCallback<T> extends MethodInvoker implements
  TransactionCallback<T> {
  private final boolean rollback;

  public MethodTransactionCallback(final boolean rollback, final Object object,
    final Method method, final Object... parameters) {
    super(method, object, parameters);
    this.rollback = rollback;
  }

  public MethodTransactionCallback(final Object object, final Method method,
    final Object... args) {
    this(true, object, method, args);
  }

  @Override
  public T doInTransaction(final TransactionStatus transaction) {
    try {
      final T result = invoke();
      if (rollback && transaction != null) {
        transaction.setRollbackOnly();
      }
      return result;
    } catch (final Throwable e) {
      if (transaction != null) {
        transaction.setRollbackOnly();
      }
      return ExceptionUtil.throwUncheckedException(e);
    }
  }
}
