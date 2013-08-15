package com.revolsys.transaction;

import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.util.ExceptionUtil;

public class TransactionUtils {
  public static Runnable createRunnable(
    final PlatformTransactionManager transactionManager,
    final int propagationBehavior, final Runnable runnable) {
    return new InvokeMethodRunnable(TransactionUtils.class, "invoke",
      transactionManager, propagationBehavior, runnable);
  }

  public static <V> V invoke(
    final PlatformTransactionManager transactionManager,
    final int propagationBehavior, final boolean rollback, final Object object,
    final Method method, final Object... parameters) {
    final MethodTransactionCallback<V> callback = new MethodTransactionCallback<V>(
      rollback, object, method, parameters);
    return invoke(transactionManager, propagationBehavior,
      (TransactionCallback<V>)callback);
  }

  public static void invoke(
    final PlatformTransactionManager transactionManager,
    final int propagationBehavior, final Runnable runnable) {
    final TransactionCallback<Void> callback = new RunnableTransactionCallback(
      runnable);
    invoke(transactionManager, propagationBehavior, callback);
  }

  public static <V> V invoke(
    final PlatformTransactionManager transactionManager,
    final int propagationBehavior, final TransactionCallback<V> callback) {
    if (transactionManager == null) {
      return callback.doInTransaction(null);
    } else {
      try {
        final TransactionTemplate template = new TransactionTemplate(
          transactionManager);
        if (propagationBehavior > -1) {
          template.setPropagationBehavior(propagationBehavior);
        }
        return template.execute(callback);
      } catch (final RuntimeException e) {
        ExceptionUtil.throwUncheckedException(e);
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T invoke(
    final PlatformTransactionManager transactionManager, final Object object,
    final Method method, final Object... parameters) {
    return (T)invoke(transactionManager,
      TransactionDefinition.PROPAGATION_REQUIRES_NEW, false, object, method,
      parameters);
  }
}
