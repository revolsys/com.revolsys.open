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
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.revolsys.ui.web.config.Config;
import com.revolsys.ui.web.config.InvalidConfigException;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.ui.web.config.XmlConfigLoader;

/**
 * @author paustin
 * @version 1.0
 */
public class IafServletFilter implements Filter {
  private static final Logger log = LoggerFactory.getLogger(IafServletFilter.class);

  private Config rsWebUiConfig;

  @Override
  public void destroy() {
    this.rsWebUiConfig = null;
    WebUiContext.setServletContext(null);
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
    final FilterChain chain) throws IOException, ServletException {
    try {
      final HttpServletRequest httpRequest = (HttpServletRequest)request;
      final HttpServletResponse httpResponse = (HttpServletResponse)response;
      final String contextPath = httpRequest.getContextPath();
      WebUiContext
        .set(new WebUiContext(this.rsWebUiConfig, contextPath, null, httpRequest, httpResponse));
      request.setAttribute("niceConfig", this.rsWebUiConfig);
      chain.doFilter(request, response);
    } finally {
      WebUiContext.set(null);
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    String config = filterConfig.getInitParameter("config");
    if (config == null) {
      config = "/WEB-INF/iaf-config.xml";
    }
    final ServletContext servletContext = filterConfig.getServletContext();
    WebUiContext.setServletContext(servletContext);
    final WebApplicationContext applicationContext = WebApplicationContextUtils
      .getRequiredWebApplicationContext(servletContext);

    try {
      final URL configResource = servletContext.getResource(config);
      final XmlConfigLoader configLoader = new XmlConfigLoader(configResource, servletContext);
      this.rsWebUiConfig = configLoader.loadConfig();
      servletContext.setAttribute("rsWebUiConfig", this.rsWebUiConfig);
    } catch (final InvalidConfigException e) {
      log.error(e.getErrors().toString(), e);
      throw new UnavailableException(
        "Cannot load a rsWebUiConfig resource from '" + config + "' due to " + e.getErrors());
    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      throw new UnavailableException(
        "Cannot load a rsWebUiConfig resource from '" + config + "' due to " + e.getMessage());
    }
  }
}
