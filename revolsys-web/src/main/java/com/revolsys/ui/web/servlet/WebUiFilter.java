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

import org.apache.xbean.spring.context.ResourceXmlApplicationContext;
import org.jeometry.common.logging.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.revolsys.ui.web.config.Config;
import com.revolsys.ui.web.config.InvalidConfigException;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.Site;
import com.revolsys.ui.web.config.SiteNodeController;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.ui.web.config.XmlConfigLoader;
import com.revolsys.ui.web.exception.PageNotFoundException;

public class WebUiFilter implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(WebUiFilter.class);

  private ApplicationContext applicationContext;

  private Config rsWebUiConfig;

  private ServletContext servletContext;

  private Site site;

  @Override
  public void destroy() {
    this.site = null;
    this.rsWebUiConfig = null;
    WebUiContext.setServletContext(null);
  }

  public void doFilter(final HttpServletRequest request, final HttpServletResponse response,
    final FilterChain chain) throws IOException, ServletException {
    if (this.rsWebUiConfig != null) {
      try {
        final HttpServletRequest httpRequest = request;
        final HttpServletResponse httpResponse = response;
        final String contextPath = httpRequest.getContextPath();
        Page page;
        try {
          page = this.rsWebUiConfig.getPage(request.getServletPath() + request.getPathInfo());
        } catch (final PageNotFoundException e) {
          page = new Page(null, null, "/", false);
        }
        WebUiContext
          .set(new WebUiContext(this.rsWebUiConfig, contextPath, page, httpRequest, httpResponse));
        request.setAttribute("rsWebUiConfig", this.rsWebUiConfig);
        chain.doFilter(request, response);
      } finally {
        WebUiContext.set(null);
      }
    } else {
      try {
        final String path = request.getServletPath();
        final String serverName = request.getServerName();

        if (this.applicationContext.containsBean(serverName)) {
          this.site = (Site)this.applicationContext.getBean(serverName);
        } else {
          LOG.info("using default site");

          this.site = (Site)this.applicationContext.getBean("default");
        }
        if (this.site != null) {
          final SiteNodeController controller = this.site.getController(path);
          LOG.debug(path + "=" + controller);
          request.setAttribute("site", this.site);
          request.setAttribute("rsWebController", controller);

          if (controller != null) {
            controller.process(this.servletContext, request, response);
            return;
          }
        }
        chain.doFilter(request, response);
      } catch (final RuntimeException e) {
        LOG.error(e.getMessage(), e);
        throw e;

      } catch (final ServletException e) {
        LOG.error(e.getMessage(), e);
        throw e;
      }
    }

  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
    final FilterChain chain) throws IOException, ServletException {
    doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);

  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    String config = filterConfig.getInitParameter("config");
    if (config == null) {
      config = "/WEB-INF/iaf-config.xml";
    }
    if (config.indexOf("iaf-config.xml") != -1) {
      loadIafConfig(config, filterConfig);
    } else {
      try {
        LOG.debug("Loading config");
        this.servletContext = filterConfig.getServletContext();
        this.applicationContext = new ResourceXmlApplicationContext(
          new ServletContextResource(this.servletContext, "/WEB-INF/web-config.xml"));
        LOG.debug("Config loaded");

      } catch (final Throwable e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  private void loadIafConfig(final String config, final FilterConfig filterConfig)
    throws UnavailableException {
    final ServletContext servletContext = filterConfig.getServletContext();
    WebUiContext.setServletContext(servletContext);
    this.applicationContext = WebApplicationContextUtils
      .getRequiredWebApplicationContext(servletContext);

    try {
      final URL configResource = servletContext.getResource(config);
      final XmlConfigLoader configLoader = new XmlConfigLoader(configResource, servletContext);
      this.rsWebUiConfig = configLoader.loadConfig();
      servletContext.setAttribute("rsWebUiConfig", this.rsWebUiConfig);
    } catch (final InvalidConfigException e) {
      Logs.error(this, e.getErrors().toString(), e);
      throw new UnavailableException(
        "Cannot load a rsWebUiConfig resource from '" + config + "' due to " + e.getErrors());
    } catch (final Exception e) {
      Logs.error(this, e.getMessage(), e);
      throw new UnavailableException(
        "Cannot load a rsWebUiConfig resource from '" + config + "' due to " + e.getMessage());
    }
  }
}
