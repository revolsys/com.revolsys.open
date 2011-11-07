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

import org.apache.log4j.Logger;
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
  private static final Logger log = Logger.getLogger(IafServletFilter.class);

  private Config rsWebUiConfig;

  public void init(final FilterConfig filterConfig) throws ServletException {
    String config = filterConfig.getInitParameter("config");
    if (config == null) {
      config = "/WEB-INF/iaf-config.xml";
    }
    ServletContext servletContext = filterConfig.getServletContext();
    WebUiContext.setServletContext(servletContext);
    WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

    try {
      URL configResource = servletContext.getResource(config);
      XmlConfigLoader configLoader = new XmlConfigLoader(configResource,
        servletContext);
      rsWebUiConfig = configLoader.loadConfig();
      servletContext.setAttribute("rsWebUiConfig", rsWebUiConfig);
    } catch (InvalidConfigException e) {
      log.error(e.getErrors(), e);
      throw new UnavailableException("Cannot load a rsWebUiConfig resource from '"
        + config + "' due to " + e.getErrors());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new UnavailableException("Cannot load a rsWebUiConfig resource from '"
        + config + "' due to " + e.getMessage());
    }
  }

  public void doFilter(final ServletRequest request,
    final ServletResponse response, final FilterChain chain)
    throws IOException, ServletException {
    try {
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      HttpServletResponse httpResponse = (HttpServletResponse)response;
      String contextPath = httpRequest.getContextPath();
      WebUiContext.set(new WebUiContext(rsWebUiConfig, contextPath, null, httpRequest,
        httpResponse));
      request.setAttribute("niceConfig", rsWebUiConfig);
      chain.doFilter(request, response);
    } finally {
      WebUiContext.set(null);
    }
  }

  public void destroy() {
    rsWebUiConfig = null;
    WebUiContext.setServletContext(null);
  }
}
