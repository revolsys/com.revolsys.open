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

  @Override
  protected void doFilterInternal(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final FilterChain filterChain) throws ServletException, IOException {
    final PlatformTransactionManager transactionManager = lookupTransactionManager(request);

    final TransactionTemplate template = new TransactionTemplate(
      transactionManager);
    final Exception exception = (Exception)template.execute(new TransactionCallback<Object>() {
      public Object doInTransaction(final TransactionStatus status) {
        try {
          filterChain.doFilter(request, response);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        } catch (final ServletException e) {
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

  protected String getTransactionManagerBeanName() {
    return this.transactionManagerBeanName;
  }

  protected PlatformTransactionManager lookupTransactionManager() {
    if (logger.isDebugEnabled()) {
      logger.debug("Using TransactionManager '"
        + getTransactionManagerBeanName() + "' for OpenTransactionInViewFilter");
    }
    final ServletContext servletContext = getServletContext();
    final WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    return wac.getBean(getTransactionManagerBeanName(),
      PlatformTransactionManager.class);
  }

  protected PlatformTransactionManager lookupTransactionManager(
    final HttpServletRequest request) {
    return lookupTransactionManager();
  }

  public void setTransactionManagerBeanName(
    final String transactionManagerBeanName) {
    this.transactionManagerBeanName = transactionManagerBeanName;
  }

}
