package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.revolsys.transaction.TransactionUtils;

public class TransactionFilter implements Filter {
  private static final Logger log = LoggerFactory.getLogger(TransactionFilter.class);

  private WebApplicationContext applicationContext;

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(final ServletRequest request,
    final ServletResponse response, final FilterChain filterChain)
    throws IOException, ServletException {
    final AbstractPlatformTransactionManager transactionManager = (AbstractPlatformTransactionManager)applicationContext.getBean("transactionManager");
    final TransactionStatus status = TransactionUtils.createDefaultTransaction(transactionManager);
    try {
      filterChain.doFilter(request, response);
      final DefaultTransactionStatus defStatus = (DefaultTransactionStatus)status;
      if (defStatus.isGlobalRollbackOnly()) {
        status.setRollbackOnly();
      }
      transactionManager.commit(status);
    } catch (final Throwable e) {
      HttpServletLogUtil.logRequestException(log, (HttpServletRequest)request,
        e);
      TransactionUtils.handleException(transactionManager, status, e);
    }
  }

  @Override
  public void init(final FilterConfig config) throws ServletException {
    final ServletContext servletContext = config.getServletContext();
    applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
  }
}
