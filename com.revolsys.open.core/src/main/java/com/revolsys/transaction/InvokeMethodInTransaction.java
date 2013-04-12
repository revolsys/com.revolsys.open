package com.revolsys.transaction;

import java.lang.reflect.Method;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodInTransaction {

  @SuppressWarnings("unchecked")
  public static <T> T execute(PlatformTransactionManager transactionManager,
    int propagationBehavior, boolean rollback, final Object object,
    Method method, final Object... parameters) {
    try {
      MethodTransactionCallback<T> callback = new MethodTransactionCallback<T>(
        rollback, object, method, parameters);
      if (transactionManager == null) {
        // TODO rollback only for non transactional
        return callback.invoke();
      } else {
        final TransactionTemplate template = new TransactionTemplate(
          transactionManager);
        if (propagationBehavior > -1) {
          template.setPropagationBehavior(propagationBehavior);
        }
        return template.execute(callback);
      }
    } catch (final Throwable e) {
      return (T)ExceptionUtil.throwUncheckedException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T execute(PlatformTransactionManager transactionManager,
    final Object object, Method method, final Object... parameters) {
    return (T)execute(transactionManager,
      TransactionDefinition.PROPAGATION_REQUIRES_NEW, false, object, method,
      parameters);
  }

  private static final Object[] EMPTY_ARGS = new Object[0];

  private final boolean throwExceptions;

  private int propagationBehavior = -1;

  private final boolean rollback;

  private final PlatformTransactionManager transactionManager;

  public InvokeMethodInTransaction(final BeanFactory beanFactory) {
    this(beanFactory, true);
  }

  /**
   * @param beanFactory
   * @param throwExceptions
   */
  public InvokeMethodInTransaction(final BeanFactory beanFactory,
    final boolean throwExceptions) {
    this(beanFactory, throwExceptions, -1);
  }

  /**
   * @param beanFactory
   * @param throwExceptions
   * @param propagationBehavior
   */
  public InvokeMethodInTransaction(final BeanFactory beanFactory,
    final boolean throwExceptions, final int propagationBehavior) {
    this(beanFactory, throwExceptions, propagationBehavior, false);
  }

  /**
   * @param beanFactory
   * @param throwExceptions
   * @param propagationBehavior
   * @param rollback
   */
  public InvokeMethodInTransaction(final BeanFactory beanFactory,
    final boolean throwExceptions, final int propagationBehavior,
    final boolean rollback) {
    this((PlatformTransactionManager)beanFactory.getBean("transactionManager"),
      throwExceptions, propagationBehavior, rollback);
  }

  public InvokeMethodInTransaction(
    final PlatformTransactionManager transactionManager) {
    this(transactionManager, true);
  }

  /**
   * @param beanFactory
   * @param throwExceptions
   */
  public InvokeMethodInTransaction(
    final PlatformTransactionManager transactionManager,
    final boolean throwExceptions) {
    this(transactionManager, throwExceptions, -1);
  }

  /**
   * @param beanFactory
   * @param throwExceptions
   * @param propagationBehavior
   */
  public InvokeMethodInTransaction(
    final PlatformTransactionManager transactionManager,
    final boolean throwExceptions, final int propagationBehavior) {
    this(transactionManager, throwExceptions, propagationBehavior, false);
  }

  public InvokeMethodInTransaction(
    final PlatformTransactionManager transactionManager,
    final boolean throwExceptions, final int propagationBehavior,
    final boolean rollback) {
    this.transactionManager = transactionManager;
    this.throwExceptions = throwExceptions;
    this.propagationBehavior = propagationBehavior;
    this.rollback = rollback;

  }

  @SuppressWarnings("unchecked")
  public <T> T execute(final Object object, final String methodName) {
    return (T)execute(object, methodName, EMPTY_ARGS);
  }

  @SuppressWarnings("unchecked")
  public <T> T execute(final Object object, final String methodName,
    final Object... args) {
    try {
      final TransactionTemplate template = new TransactionTemplate(
        transactionManager);
      if (propagationBehavior > -1) {
        template.setPropagationBehavior(propagationBehavior);
      }
      return (T)template.execute(new InvokeMethodTransactionCallback(object,
        methodName, rollback, args));
    } catch (final RuntimeException e) {
      if (throwExceptions) {
        throw e;
      } else {
        e.printStackTrace();
        return null;
      }
    }
  }
}
