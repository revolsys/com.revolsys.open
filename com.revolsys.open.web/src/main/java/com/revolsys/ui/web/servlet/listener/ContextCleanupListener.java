package com.revolsys.ui.web.servlet.listener;

import java.beans.Introspector;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.ClearCachedIntrospectionResults;
import org.springframework.beans.factory.DisposableBean;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.jdbc.io.JdbcFactoryRegistry;

public class ContextCleanupListener implements ServletContextListener {

  public void contextInitialized(ServletContextEvent event) {
    CachedIntrospectionResults.acceptClassLoader(Thread.currentThread()
      .getContextClassLoader());
  }

  public void contextDestroyed(ServletContextEvent event) {
    ClassLoader contextClassLoader = Thread.currentThread()
      .getContextClassLoader();
    IoFactoryRegistry.clearInstance();
    JdbcFactoryRegistry.clearInstance();
    StringConverterRegistry.clearInstance();
    GeometryFactory.clear();
    EpsgCoordinateSystems.clear();
    cleanupAttributes(event.getServletContext());
    
    CachedIntrospectionResults.clearClassLoader(contextClassLoader);
    CachedIntrospectionResults.clearClassLoader(CachedIntrospectionResults.class.getClassLoader());
    CachedIntrospectionResults.clearClassLoader(ClassLoader.getSystemClassLoader());
    ClearCachedIntrospectionResults.clearCache();
    Introspector.flushCaches();
  }

  static void cleanupAttributes(ServletContext servletContext) {
    @SuppressWarnings("rawtypes")
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
