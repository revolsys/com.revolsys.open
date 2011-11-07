package com.revolsys.parallel.tools;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.revolsys.parallel.process.ProcessNetwork;

public class ProcessNetworkServletContextListener implements
  ServletContextListener {

  private ProcessNetwork processNetwork;

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    if (processNetwork != null) {
      processNetwork.stop();
    }
  }

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.getServletContext();
    WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    this.processNetwork = (ProcessNetwork)applicationContext.getBean("com.revolsys.parallel.process.ProcessNetwork");
    processNetwork.start();
  }
}
