package com.revolsys.orm.core.servlet.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class OpenTransactionInViewFilter extends OncePerRequestFilter {

  public static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";

  private String transactionManagerBeanName = DEFAULT_TRANSACTION_MANAGER_BEAN_NAME;

  public void setTransactionManagerBeanName(
    String transactionManagerBeanName) {
    this.transactionManagerBeanName = transactionManagerBeanName;
  }

  protected String getTransactionManagerBeanName() {
    return this.transactionManagerBeanName;
  }

  protected void doFilterInternal(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final FilterChain filterChain)
    throws ServletException,
    IOException {
    PlatformTransactionManager transactionManager = lookupTransactionManager(request);

    TransactionTemplate template = new TransactionTemplate(transactionManager);
    Exception exception = (Exception)template.execute(new TransactionCallback<Object>() {
      public Object doInTransaction(
        TransactionStatus status) {
        try {
          filterChain.doFilter(request, response);
        } catch (IOException e) {
          throw new RuntimeException(e);
        } catch (ServletException e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    });
    if (exception != null) {
      if (exception instanceof IOException) {
        throw (IOException)exception;
      } else if (exception instanceof ServletException) {
        throw (ServletException)exception;
      }
    }
  }

  protected PlatformTransactionManager lookupTransactionManager(
    HttpServletRequest request) {
    return lookupTransactionManager();
  }

  protected PlatformTransactionManager lookupTransactionManager() {
    if (logger.isDebugEnabled()) {
      logger.debug("Using TransactionManager '"
        + getTransactionManagerBeanName() + "' for OpenTransactionInViewFilter");
    }
    ServletContext servletContext = getServletContext();
    WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    return (PlatformTransactionManager)wac.getBean(
      getTransactionManagerBeanName(), PlatformTransactionManager.class);
  }

}
