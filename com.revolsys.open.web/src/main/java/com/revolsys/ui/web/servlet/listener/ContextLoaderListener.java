package com.revolsys.ui.web.servlet.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.ContextLoader;

public class ContextLoaderListener implements ServletContextListener {
  private ContextLoader contextLoader;

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    final ContextLoader contextLoader = this.contextLoader;
    this.contextLoader = null;
    final ServletContext servletContext = event.getServletContext();
    if (contextLoader != null) {
      contextLoader.closeWebApplicationContext(servletContext);
    }
    ContextCleanupListener.cleanupAttributes(servletContext);
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    if (this.contextLoader == null) {
      final ContextLoader contextLoader = new ContextLoader();
      final ServletContext servletContext = event.getServletContext();
      contextLoader.initWebApplicationContext(servletContext);
      this.contextLoader = contextLoader;
    }
  }

}
