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

import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.xml.DOMConfigurator;

import com.revolsys.logging.log4j.ContextClassLoaderRepositorySelector;

/**
 * The Log4jInitializationServlet class uses the
 * {@link ContextClassLoaderRepositorySelector} to maintain a separate Log4j
 * configuration for a servlet context and to load the logging configuration
 * from the log4j.xml file specified by the log4jXmlLocation context-param (or
 * /WEB-INF/log4j.xml if not specified).
 * 
 * @author Paul Austin
 * @version 1.0
 */
public class Log4jInitializationServlet extends HttpServlet {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -1619067817259518008L;

  /** The default location for the log4j.xml file. */
  public static final String DEFAULT_LOG4J_XML_LOCATION = "/WEB-INF/log4j.xml";

  /**
   * Clean up the servlet by removing the logging configuration for the current
   * context.
   */
  @Override
  public void destroy() {
    ContextClassLoaderRepositorySelector.remove();
  }

  /**
   * Initialize the logging for context by creating a new heirarchy for the
   * current thread context class context and loading the configuration from the
   * log4jXmlLocation context-param.
   * 
   * @param config The servlet configuration.
   * @throws ServletException If there was a problem initializing the servlet.
   */
  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    final Hierarchy hierarchy = ContextClassLoaderRepositorySelector.add();
    final ServletContext context = config.getServletContext();
    String log4jXml = context.getInitParameter("log4jXmlLocation");
    if (log4jXml == null) {
      log4jXml = DEFAULT_LOG4J_XML_LOCATION;
    }
    try {
      final InputStream log4JConfig = context.getResourceAsStream(log4jXml);
      if (log4JConfig != null) {
        final DOMConfigurator conf = new DOMConfigurator();
        conf.doConfigure(log4JConfig, hierarchy);
      }
    } catch (final Exception e) {
      throw new ServletException(e);
    }
  }

}
