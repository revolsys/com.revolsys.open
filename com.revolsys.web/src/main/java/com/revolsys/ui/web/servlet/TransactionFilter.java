/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * @author paustin
 * @version 1.0
 */
public class TransactionFilter implements Filter {
  private static final Logger log = Logger.getLogger(TransactionFilter.class);

  private WebApplicationContext applicationContext;

  public void init(final FilterConfig config) throws ServletException {
    ServletContext servletContext = config.getServletContext();
    applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
  }
  
  public void doFilter(final ServletRequest request,
    final ServletResponse response, final FilterChain filterChain)
    throws IOException, ServletException {
    AbstractPlatformTransactionManager transactionManager = (AbstractPlatformTransactionManager)applicationContext.getBean("transactionManager");
    TransactionTemplate template = new TransactionTemplate(transactionManager);

    try {
      template.execute(new TransactionCallback() {
        public Object doInTransaction(final TransactionStatus transaction) {
          try {
            filterChain.doFilter(request, response);
            DefaultTransactionStatus defStatus = (DefaultTransactionStatus)transaction;
            if (defStatus.isGlobalRollbackOnly()) {
              transaction.setRollbackOnly();
            }
          } catch (Throwable e) {
            transaction.setRollbackOnly();
            throw new RuntimeException(e.getMessage(), e);
          }
          return null;
        }
      });
    } catch (RuntimeException e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        if (cause instanceof ServletException) {
          ServletException se = (ServletException)cause;
          Throwable servletCause = se.getRootCause();
          if (servletCause != null) {
            cause = servletCause;
          }
        }
        Log4jJ2eeUtil.logRequestException(log, (HttpServletRequest)request,
          cause);
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        } else if (cause instanceof Error) {
          throw (Error)cause;
        } else if (cause instanceof IOException) {
          throw (IOException)cause;
        } else if (cause instanceof ServletException) {
          throw (ServletException)cause;
        } else {
          throw new ServletException(cause.getMessage(), cause);
        }
      } else {
        Log4jJ2eeUtil.logRequestException(log, (HttpServletRequest)request,
          cause);
        throw e;
      }
    }
  }

  public void destroy() {
  }
}
