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
package com.revolsys.ui.web.config;

import java.util.Stack;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.log4j.Logger;

public class WebUiContext {
  private static final ThreadLocal<WebUiContext> local = new ThreadLocal<WebUiContext>();

  private static final Logger log = Logger.getLogger(WebUiContext.class);

  private static ServletContext servletContext;

  public static WebUiContext get() {
    return local.get();
  }

  public static ServletContext getServletContext() {
    return servletContext;
  }

  public static void set(
    final WebUiContext context) {
    local.set(context);
  }

  public static void setServletContext(
    final ServletContext servletContext) {
    WebUiContext.servletContext = servletContext;
  }

  private Config config;

  private String contextPath;

  private final Stack layouts = new Stack();

  private Menu menu;

  private Page page;

  private HttpServletRequest request;

  private HttpServletResponse response;

  public WebUiContext() {
  }

  public WebUiContext(
    final Config config,
    final String contextPath,
    final Page page,
    final HttpServletRequest request,
    final HttpServletResponse response) {
    this.config = config;
    this.contextPath = contextPath;
    this.page = page;
    if (page != null) {
      pushLayout(page.getLayout());
    }
    this.request = request;
    this.response = response;
  }

  public Object evaluateExpression(
    final Expression expression) {
    final JexlContext jexlContext = new JexlHttpServletRequestContext(request);
    try {
      return expression.evaluate(jexlContext);
    } catch (final Exception e) {
      log.error("Unable to evaluate expression " + expression.getExpression()
        + ": " + e.getMessage(), e);
      return null;
    }
  }

  public Object evaluateExpression(
    final String expression) {
    try {
      return evaluateExpression(ExpressionFactory.createExpression(expression));
    } catch (final Exception e) {
      log.error("Unable to create expression " + expression + ": "
        + e.getMessage(), e);
      return null;
    }
  }

  public Config getConfig() {
    return config;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Layout getCurrentLayout() {
    return (Layout)layouts.peek();
  }

  public Menu getMenu() {
    return menu;
  }

  public Menu getMenu(
    final String name) {
    return config.getMenu(name);
  }

  public Page getPage() {
    return page;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public Layout popLayout() {
    return (Layout)layouts.pop();
  }

  public void pushLayout(
    final Layout layout) {
    layouts.push(layout);
  }

  public void setPage(
    final Page page) {
    this.page = page;
  }
}
