package com.revolsys.ui.web.servlet.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.ContextLoader;

public class ContextLoaderListener implements ServletContextListener {

  private ContextLoader contextLoader;

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    final ContextLoader contextLoader = this.contextLoader;
    this.contextLoader = null;
    if (contextLoader != null) {
      contextLoader.closeWebApplicationContext(event.getServletContext());
    }
    ContextCleanupListener.cleanupAttributes(event.getServletContext());
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    if (this.contextLoader == null) {
      final ContextLoader contextLoader = new ContextLoader();
      contextLoader.initWebApplicationContext(event.getServletContext());
      this.contextLoader = contextLoader;
    }
  }

}
