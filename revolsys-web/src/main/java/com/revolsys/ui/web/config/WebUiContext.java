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

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUiContext {
  private static final ThreadLocal<WebUiContext> local = new ThreadLocal<>();

  private static final Logger log = LoggerFactory.getLogger(WebUiContext.class);

  private static ServletContext servletContext;

  public static WebUiContext get() {
    final WebUiContext context = local.get();
    return context;
  }

  public static ServletContext getServletContext() {
    return servletContext;
  }

  public static void set(final WebUiContext context) {
    local.set(context);
  }

  public static void setServletContext(final ServletContext servletContext) {
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

  public WebUiContext(final Config config, final String contextPath, final Page page,
    final HttpServletRequest request, final HttpServletResponse response) {
    this.config = config;
    this.contextPath = contextPath;
    this.page = page;
    if (page != null) {
      pushLayout(page.getLayout());
    }
    this.request = request;
    this.response = response;
  }

  public Object evaluateExpression(final JexlExpression expression) {
    final JexlContext jexlContext = new JexlHttpServletRequestContext(this.request);
    try {
      return expression.evaluate(jexlContext);
    } catch (final Exception e) {
      log.error(
        "Unable to evaluate expression " + expression.getSourceText() + ": " + e.getMessage(), e);
      return null;
    }
  }

  public Object evaluateExpression(final String expression) {
    try {
      return evaluateExpression(new JexlBuilder().create().createExpression(expression));
    } catch (final Exception e) {
      log.error("Unable to create expression " + expression + ": " + e.getMessage(), e);
      return null;
    }
  }

  public Config getConfig() {
    return this.config;
  }

  public String getContextPath() {
    return this.contextPath;
  }

  public Layout getCurrentLayout() {
    return (Layout)this.layouts.peek();
  }

  public Menu getMenu() {
    return this.menu;
  }

  public Menu getMenu(final String name) {
    return this.config.getMenu(name);
  }

  public Page getPage() {
    return this.page;
  }

  public HttpServletRequest getRequest() {
    return this.request;
  }

  public HttpServletResponse getResponse() {
    return this.response;
  }

  public Layout popLayout() {
    return (Layout)this.layouts.pop();
  }

  public void pushLayout(final Layout layout) {
    this.layouts.push(layout);
  }

  public void setPage(final Page page) {
    this.page = page;
  }
}
