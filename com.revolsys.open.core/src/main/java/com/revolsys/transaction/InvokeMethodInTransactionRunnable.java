package com.revolsys.transaction;

import java.util.Arrays;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class InvokeMethodInTransactionRunnable implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(InvokeMethodInTransactionRunnable.class);

  private final Object[] args;

  private final String methodName;

  private final Object object;

  private int propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW;

  private final boolean rollback;

  private final PlatformTransactionManager transactionManager;

  /**
   * @param beanFactory
   * @param throwExceptions
   * @param propagationBehavior
   * @param rollback
   */
  public InvokeMethodInTransactionRunnable(final BeanFactory beanFactory,
    final int propagationBehavior, final boolean rollback, final Object object,
    final String methodName, final Object... args) {
    this((PlatformTransactionManager)beanFactory.getBean("transactionManager"),
      propagationBehavior, rollback, object, methodName, args);
  }

  /**
   * @param beanFactory
   * @param throwExceptions
   * @param propagationBehavior
   */
  public InvokeMethodInTransactionRunnable(final BeanFactory beanFactory,
    final int propagationBehavior, final Object object,
    final String methodName, final Object... args) {
    this(beanFactory, propagationBehavior, false, object, methodName, args);
  }

  /**
   * @param beanFactory
   * @param throwExceptions
   */
  public InvokeMethodInTransactionRunnable(final BeanFactory beanFactory,
    final Object object, final String methodName, final Object... args) {
    this(beanFactory, -1, object, methodName, args);
  }

  public InvokeMethodInTransactionRunnable(
    final PlatformTransactionManager transactionManager,
    final int propagationBehavior, final boolean rollback, final Object object,
    final String methodName, final Object... args) {
    this.transactionManager = transactionManager;
    this.propagationBehavior = propagationBehavior;
    this.rollback = rollback;
    this.object = object;
    this.methodName = methodName;
    this.args = args;

  }

  /**
   * @param beanFactory
   * @param throwExceptions
   * @param propagationBehavior
   */
  public InvokeMethodInTransactionRunnable(
    final PlatformTransactionManager transactionManager,
    final int propagationBehavior, final Object object,
    final String methodName, final Object... args) {
    this(transactionManager, propagationBehavior, false, object, methodName,
      args);
  }

  /**
   * @param beanFactory
   * @param throwExceptions
   */
  public InvokeMethodInTransactionRunnable(
    final PlatformTransactionManager transactionManager, final Object object,
    final String methodName, final Object... args) {
    this(transactionManager, -1, object, methodName, args);
  }

  @Override
  public void run() {
    try {
      final TransactionTemplate template = new TransactionTemplate(
        transactionManager);
      if (propagationBehavior > -1) {
        template.setPropagationBehavior(propagationBehavior);
      }
      template.execute(new InvokeMethodTransactionCallback(object, methodName,
        rollback, args));
    } catch (final RuntimeException e) {
      LOG.error("Error invoking " + object.getClass() + "." + methodName
        + Arrays.toString(args));
    }
  }
}
