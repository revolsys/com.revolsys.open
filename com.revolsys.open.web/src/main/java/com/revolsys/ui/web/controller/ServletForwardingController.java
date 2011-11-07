/*
 * Copyright 2002-2007 the original author or authors.
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

package com.revolsys.ui.web.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.util.WebUtils;

public class ServletForwardingController extends AbstractController implements
  BeanNameAware {

  private String servletName;

  private String beanName;

  public void setServletName(
    String servletName) {
    this.servletName = servletName;
  }

  public void setBeanName(
    String name) {
    this.beanName = name;
    if (this.servletName == null) {
      this.servletName = name;
    }
  }

  @Override
  protected ModelAndView handleRequestInternal(
    HttpServletRequest request,
    HttpServletResponse response)
    throws Exception {

    final ServletContext servletContext = getServletContext();
    RequestDispatcher rd = servletContext.getNamedDispatcher(this.servletName);
    if (rd == null) {
      throw new ServletException("No servlet with name '" + this.servletName
        + "' defined in web.xml");
    }
    final String dispatcherRequestPath = (String)request.getAttribute("org.apache.catalina.core.DISPATCHER_REQUEST_PATH");
    if (dispatcherRequestPath != null) {
      String servletPath = request.getServletPath();
      String pathInfo = request.getPathInfo();
      if (servletPath.equals("") && !pathInfo.equals(dispatcherRequestPath)) {
        request = new HttpServletRequestWrapper(request) {
          @Override
          public String getRequestURI() {
            return getContextPath() + getServletPath() + getPathInfo();
          }

          @Override
          public String getPathInfo() {
            return dispatcherRequestPath;
          }
        };
      }
    } else {
      request = new HttpServletRequestWrapper(request);
    }

    // If already included, include again, else forward.
    if (useInclude(request, response)) {
      rd.include(request, response);
    } else {
      rd.forward(request, response);
    }
    return null;
  }

  /**
   * Determine whether to use RequestDispatcher's <code>include</code> or
   * <code>forward</code> method.
   * <p>
   * Performs a check whether an include URI attribute is found in the request,
   * indicating an include request, and whether the response has already been
   * committed. In both cases, an include will be performed, as a forward is not
   * possible anymore.
   * 
   * @param request current HTTP request
   * @param response current HTTP response
   * @return <code>true</code> for include, <code>false</code> for forward
   * @see javax.servlet.RequestDispatcher#forward
   * @see javax.servlet.RequestDispatcher#include
   * @see javax.servlet.ServletResponse#isCommitted
   * @see org.springframework.web.util.WebUtils#isIncludeRequest
   */
  protected boolean useInclude(
    HttpServletRequest request,
    HttpServletResponse response) {
    return (WebUtils.isIncludeRequest(request) || response.isCommitted());
  }

}
