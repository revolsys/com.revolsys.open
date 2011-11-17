package com.revolsys.orm.core.web.interceptor;

import java.lang.reflect.UndeclaredThrowableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

public class OpenTransactionInViewInterceptor extends
  DefaultTransactionDefinition implements WebRequestInterceptor {
  /**
   * 
   */
  private static final long serialVersionUID = -9132327865691554649L;

  private static final String ATTRIBUTE = OpenTransactionInViewInterceptor.class.getName();

  private static final String LEVEL = OpenTransactionInViewInterceptor.class.getName()
    + ".level";

  private static final Logger LOG = LoggerFactory.getLogger(OpenTransactionInViewInterceptor.class);

  private PlatformTransactionManager transactionManager;

  public OpenTransactionInViewInterceptor() {
  }

  public void afterCompletion(
    final WebRequest request,
    final Exception exception)
    throws Exception {
    Integer level = (Integer)request.getAttribute(LEVEL,
      RequestAttributes.SCOPE_REQUEST);
    if (level == null || level == 0) {
      final TransactionStatus status = (TransactionStatus)request.getAttribute(
        ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
      if (status != null) {
        if (exception == null) {
          this.transactionManager.commit(status);
        } else {
          rollbackOnException(status, exception);
          if (exception instanceof RuntimeException) {
            throw exception;
          } else {
            throw new UndeclaredThrowableException(exception,
              "TransactionCallback threw undeclared checked exception");
          }
        }
      }
      request.setAttribute(ATTRIBUTE, null, RequestAttributes.SCOPE_REQUEST);
      request.setAttribute(LEVEL, null, RequestAttributes.SCOPE_REQUEST);
    } else {
      request.setAttribute(LEVEL, 0, RequestAttributes.SCOPE_REQUEST);
    }
  }

  public PlatformTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void postHandle(
    final WebRequest request,
    final ModelMap model)
    throws DataAccessException {

  }

  public void preHandle(
    final WebRequest request) {
    Integer level = (Integer)request.getAttribute(LEVEL,
      RequestAttributes.SCOPE_REQUEST);
    if (level == null) {
      final TransactionStatus status = this.transactionManager.getTransaction(this);
      request.setAttribute(ATTRIBUTE, status, RequestAttributes.SCOPE_REQUEST);
      request.setAttribute(LEVEL, 0, RequestAttributes.SCOPE_REQUEST);
    } else {
      request.setAttribute(LEVEL, level++, RequestAttributes.SCOPE_REQUEST);
    }

  }

  private void rollbackOnException(
    final TransactionStatus status,
    final Throwable exception)
    throws TransactionException {
    LOG.debug("Initiating transaction rollback on application exception",
      exception);
    try {
      this.transactionManager.rollback(status);
    } catch (final TransactionSystemException e) {
      LOG.error("Application exception overridden by rollback exception",
        exception);
      e.initApplicationException(exception);
      throw e;
    } catch (final RuntimeException e) {
      LOG.error("Application exception overridden by rollback exception",
        exception);
      throw e;
    } catch (final Error e) {
      LOG.error("Application exception overridden by rollback error", exception);
      throw e;
    }
  }

  @Required
  public void setTransactionManager(
    final PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

}
