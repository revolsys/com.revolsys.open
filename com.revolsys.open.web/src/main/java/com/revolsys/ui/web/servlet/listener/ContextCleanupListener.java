package com.revolsys.ui.web.servlet.listener;

import java.beans.Introspector;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.DisposableBean;

public class ContextCleanupListener implements ServletContextListener {

  public void contextInitialized(ServletContextEvent event) {
    CachedIntrospectionResults.acceptClassLoader(Thread.currentThread()
      .getContextClassLoader());
  }

  public void contextDestroyed(ServletContextEvent event) {
    cleanupAttributes(event.getServletContext());
       CachedIntrospectionResults.clearClassLoader(Thread.currentThread()
      .getContextClassLoader());
    Introspector.flushCaches();
  }

  static void cleanupAttributes(ServletContext servletContext) {
    Enumeration attrNames = servletContext.getAttributeNames();
    while (attrNames.hasMoreElements()) {
      String attrName = (String)attrNames.nextElement();
      if (attrName.startsWith("org.springframework.")) {
        Object attrValue = servletContext.getAttribute(attrName);
        if (attrValue instanceof DisposableBean) {
          try {
            ((DisposableBean)attrValue).destroy();
          } catch (Throwable e) {
            System.err.println("Couldn't invoke destroy method of attribute with name '"
              + attrName + "'");
          }
        } else {
          servletContext.removeAttribute(attrName);
        }
      }
    }
  }
}
