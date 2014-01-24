package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.revolsys.transaction.Propagation;
import com.revolsys.transaction.Transaction;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class TransactionFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(TransactionFilter.class);

  private WebApplicationContext applicationContext;

  @Override
  public void destroy() {
    applicationContext = null;
  }

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
    final HttpServletResponse response, final FilterChain filterChain)
    throws ServletException, IOException {

    final HttpServletRequest savedRequest = HttpServletUtils.getRequest();
    final HttpServletResponse savedResponse = HttpServletUtils.getResponse();

    try {
      HttpServletUtils.setRequestAndResponse(request, response);
      final AbstractPlatformTransactionManager transactionManager = (AbstractPlatformTransactionManager)applicationContext.getBean("transactionManager");
      if (transactionManager == null) {
        filterChain.doFilter(request, response);
      } else {
        try (
          Transaction transaction = new Transaction(transactionManager,
            Propagation.REQUIRED)) {
          try {
            filterChain.doFilter(request, response);
          } catch (final ServletException e) {
            HttpServletLogUtil.logRequestException(log, request, e);
            transaction.setRollbackOnly();
            throw e;
          } catch (final IOException e) {
            HttpServletLogUtil.logRequestException(log, request, e);
            transaction.setRollbackOnly();
            throw e;
          } catch (final Throwable e) {
            HttpServletLogUtil.logRequestException(log, request, e);
            throw transaction.setRollbackOnly(e);
          }
        } catch (final ServletException e) {
          HttpServletLogUtil.logRequestException(log, request, e);
          throw e;
        } catch (final IOException e) {
          HttpServletLogUtil.logRequestException(log, request, e);
          throw e;
        } catch (final RuntimeException e) {
          HttpServletLogUtil.logRequestException(log, request, e);
          throw e;
        } catch (final Error e) {
          HttpServletLogUtil.logRequestException(log, request, e);
          throw e;
        }
      }
    } finally {
      if (savedRequest == null) {
        HttpServletUtils.clearRequestAndResponse();
      } else {
        HttpServletUtils.setRequestAndResponse(savedRequest, savedResponse);
      }
    }
  }

  @Override
  protected void initFilterBean() throws ServletException {
    final ServletContext servletContext = getServletContext();
    applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
  }
}
