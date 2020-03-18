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
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.web.config.Argument;
import com.revolsys.ui.web.config.Attribute;
import com.revolsys.ui.web.config.AttributeLoader;
import com.revolsys.ui.web.config.Config;
import com.revolsys.ui.web.config.InvalidConfigException;
import com.revolsys.ui.web.config.Layout;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.ui.web.config.XmlConfigLoader;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.AuthenticationException;
import com.revolsys.ui.web.exception.FinishRequestException;
import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.ui.web.exception.RedirectException;

public final class IafServlet extends HttpServlet {
  /** The logging category */
  private static final Logger log = LoggerFactory.getLogger(IafServlet.class);

  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -5543695651259069014L;

  /** The defintion of the application */
  private Config applicationConfig;

  private ServletContext servletContext;

  /**
   * Handle the GET request. Calls processRequest to handle the request.
   *
   * @param request the parameters of the client request
   * @param response the response sent back to the client
   * @exception ServletException if there was a problem handling the request
   * @exception IOException if an input output error occurs when handling the
   *              request
   */
  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handle the POST request. Calls processRequest to handle the request.
   *
   * @param request the parameters of the client request
   * @param response the response sent back to the client
   * @exception ServletException if there was a problem handling the request
   * @exception IOException if an input output error occurs when handling the
   *              request
   */
  @Override
  public void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Forward the request to the specified resource.
   *
   * @param path the path to the resource to forward to
   * @param request the parameters of the client request
   * @param response the response sent back to the client
   * @exception ServletException if there was a problem handling the request
   * @exception IOException if an input output error occurs when handling the
   *              request
   */
  public void forward(final String path, final HttpServletRequest request,
    final HttpServletResponse response) throws ServletException, IOException {
    if (!response.isCommitted()) {
      getServletConfig().getServletContext().getRequestDispatcher(path).forward(request, response);
    }
  }

  /**
   * Initialise the servlet. Loads the configuration from the
   * /WEB-INF/nice-config.xml file.
   *
   * @param config The servlet configuration parameters
   * @exception ServletException if there was a problem initialising the servlet
   */
  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    try {
      this.servletContext = config.getServletContext();
      final URL configResource = this.servletContext.getResource("/WEB-INF/iaf-config.xml");
      WebUiContext.setServletContext(this.servletContext);
      final XmlConfigLoader configLoader = new XmlConfigLoader(configResource, this.servletContext);
      this.applicationConfig = configLoader.loadConfig();
      this.servletContext.setAttribute("rsWebUiConfig", this.applicationConfig);
    } catch (final InvalidConfigException ice) {
      ice.printStackTrace();
      log.error(ice.getErrors().toString());
      throw new UnavailableException(ice.getMessage() + ":" + ice.getErrors());
    } catch (final MalformedURLException mue) {
      log.error(mue.getMessage(), mue);
      throw new UnavailableException("Failed to initialise Servlet");
    } catch (final Throwable t) {
      t.printStackTrace();
      log.error(t.getMessage(), t);

    }
  }

  /**
   * @param page
   * @param request
   * @throws PageNotFoundException
   */
  private void processArguments(final Page page, final HttpServletRequest request)
    throws ActionException {
    for (final Object element : page.getArguments()) {
      final Argument argument = (Argument)element;
      final String name = argument.getName();
      Object value = null;
      String stringValue = request.getParameter(name);
      if (stringValue == null) {
        stringValue = argument.getDefault();
      }
      if (stringValue != null) {
        final Class argumentType = argument.getType();
        try {
          value = argument.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          throw new PageNotFoundException("Page argument is not a valid number: " + name);
        }
      }
      if (value != null) {
        request.setAttribute(name, value);
      } else if (argument.isRequired()) {
        throw new PageNotFoundException("Missing page argument: " + name);
      }
    }
  }

  /**
   * @param page
   * @param request
   * @throws PageNotFoundException
   */
  private void processAttributes(final Page page, final HttpServletRequest request)
    throws ActionException {
    for (final Object element : page.getFields()) {
      final Attribute attribute = (Attribute)element;
      final String name = attribute.getName();
      final AttributeLoader loader = attribute.getLoader();
      Object value = null;
      if (loader != null) {
        value = loader.getValue(request);
      } else {
        value = attribute.getValue();
      }
      if (value != null) {
        request.setAttribute(name, value);
      }
    }
  }

  /**
   * Process the service request, uses the full path name of the request to
   * finds the page definition. The component heirarchy, list of request
   * processors and the menu tree from the config are used to handle the
   * request. If a PageNotFoundException was thrown by any of the request
   * processors a 404 response error code is set and no further processing is
   * performed. If a RedirectException is thrown the response is redirected to
   * the url from the exception.
   *
   * @param request the servlet request
   * @param response the servlet response
   * @exception ServletException if any unhandled exceptions occured during the
   *              processing of the request
   * @exception IOException if there was a problem sending data to the client
   * @see PageNotFoundException
   * @see RedirectException
   */
  public void processRequest(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    try {
      String path = request.getServletPath();
      final String pathInfo = request.getPathInfo();
      if (pathInfo != null && !path.endsWith(pathInfo)) {
        path += pathInfo;
      }

      boolean secure = false;
      final String contextPath = request.getContextPath();
      if (path.endsWith(".wp")) {
        path = path.substring(0, path.length() - 3);
      } else if (path.endsWith(".wps")) {
        secure = true;
        path = path.substring(0, path.length() - 4);
      }
      final Page page = this.applicationConfig.getPage(contextPath + path);
      WebUiContext
        .set(new WebUiContext(this.applicationConfig, contextPath, page, request, response));
      if (page.isSecure() && !secure) {
        response.sendRedirect(page.getFullUrl());
        return;
      }
      processArguments(page, request);
      processAttributes(page, request);
      request.setAttribute("niceConfig", this.applicationConfig);
      request.setAttribute("nicePage", page);

      final String menuName = request.getParameter("menuName");
      request.setAttribute("menuSelected", menuName);
      request.setAttribute("title", page.getTitle());
      page.invokeActions(this.servletContext, request, response);

      final Layout layout = page.getLayout();
      if (layout != null) {
        final String file = layout.getFile();
        if (file != null && file.length() > 0) {
          forward(file, request, response);
        }
      }
    } catch (final FinishRequestException fre) {
      // Do nothing as the actions have handled the request
      return;
    } catch (final AuthenticationException pne) {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    } catch (final PageNotFoundException pne) {
      log.error(pne.getMessage(), pne);
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } catch (final RedirectException re) {
      response.sendRedirect(response.encodeRedirectURL(re.getUrl()));
    } catch (final Throwable t) {
      log.error(t.getMessage(), t);
      throw new ServletException(t);
    }
  }
}
